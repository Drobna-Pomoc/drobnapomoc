package com.drobnapomoc.drobnapomoc.servicerequest.adapter.out.llm;

import com.drobnapomoc.drobnapomoc.servicerequest.application.port.out.PriceEstimatorPort;
import com.drobnapomoc.drobnapomoc.servicerequest.domain.PriceEstimate;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Asks the local LLM (Ollama, through Spring AI) for a price range. Never throws: a slow, unreachable or nonsensical
 * model gives no estimate, and the reason is logged. Active when {@code drobnapomoc.price-estimate.enabled=true}.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "drobnapomoc.price-estimate", name = "enabled", havingValue = "true")
class OllamaPriceEstimator implements PriceEstimatorPort {

    private static final String SYSTEM_PROMPT = """
            Jesteś ekspertem od cen drobnych usług domowych w Polsce (naprawy, montaż, pomoc w domu).
            Na podstawie kategorii i opisu zlecenia oszacuj realny przedział ceny brutto w złotych za robociznę
            razem z typowymi drobnymi materiałami. Odpowiedz wyłącznie przedziałem: minPln i maxPln jako liczby całkowite.
            """;
    private static final int MIN_PLN = 1;
    private static final int MAX_PLN = 50_000;
    private static final long GROSZE_PER_PLN = 100L;
    private static final Executor VIRTUAL_THREADS = task -> Thread.ofVirtual().start(task);

    private final ChatClient chatClient;
    private final Duration timeout;

    OllamaPriceEstimator(final ChatClient.Builder chatClientBuilder, final PriceEstimateProperties properties) {
        this.chatClient = chatClientBuilder.defaultSystem(SYSTEM_PROMPT).build();
        this.timeout = properties.timeout();
    }

    @Override
    public Optional<PriceEstimate> estimate(final String categoryName, final String description) {
        final var call = CompletableFuture.supplyAsync(() -> ask(categoryName, description), VIRTUAL_THREADS);
        try {
            return toEstimate(call.get(timeout.toMillis(), TimeUnit.MILLISECONDS));
        } catch (final TimeoutException e) {
            call.cancel(true);
            log.warn("No price estimate: the model did not answer within {}", timeout);
        } catch (final ExecutionException e) {
            log.warn("No price estimate: the model call failed: {}", String.valueOf(e.getCause()));
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("No price estimate: interrupted while waiting for the model");
        }
        return Optional.empty();
    }

    private @Nullable LlmPriceRange ask(final String categoryName, final String description) {
        return chatClient.prompt()
                .user("Kategoria: " + categoryName + "\nOpis zlecenia: " + description)
                .call()
                .entity(LlmPriceRange.class);
    }

    private static Optional<PriceEstimate> toEstimate(final @Nullable LlmPriceRange range) {
        if (range == null) {
            log.warn("No price estimate: the model gave an empty answer");
            return Optional.empty();
        }
        if (range.minPln() < MIN_PLN || range.maxPln() < range.minPln() || range.maxPln() > MAX_PLN) {
            log.warn("No price estimate: the model answered an invalid range {}", range);
            return Optional.empty();
        }
        return Optional.of(new PriceEstimate(range.minPln() * GROSZE_PER_PLN, range.maxPln() * GROSZE_PER_PLN));
    }
}
