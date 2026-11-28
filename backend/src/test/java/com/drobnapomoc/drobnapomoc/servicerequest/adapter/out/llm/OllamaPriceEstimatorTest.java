package com.drobnapomoc.drobnapomoc.servicerequest.adapter.out.llm;

import com.drobnapomoc.drobnapomoc.servicerequest.domain.PriceEstimate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OllamaPriceEstimatorTest {

    private static final String CATEGORY = "Hydraulik";
    private static final String DESCRIPTION = "Cieknie kran w kuchni, trzeba wymienić uszczelkę.";
    private static final Duration TIMEOUT = Duration.ofSeconds(2);
    private static final Duration SHORT_TIMEOUT = Duration.ofMillis(300);

    @Mock
    private ChatModel chatModel;

    @Test
    void testEstimate_givenValidRangeInPln_shouldReturnItInGrosze() {
        // given
        givenModelAnswers("{\"minPln\": 80, \"maxPln\": 150}");

        // when & then
        assertThat(estimator().estimate(CATEGORY, DESCRIPTION)).contains(new PriceEstimate(8000L, 15000L));
    }

    @Test
    void testEstimate_givenMinimumBelowOneZloty_shouldReturnEmpty() {
        // given
        givenModelAnswers("{\"minPln\": 0, \"maxPln\": 150}");

        // when & then
        assertThat(estimator().estimate(CATEGORY, DESCRIPTION)).isEmpty();
    }

    @Test
    void testEstimate_givenMaximumBelowMinimum_shouldReturnEmpty() {
        // given
        givenModelAnswers("{\"minPln\": 300, \"maxPln\": 100}");

        // when & then
        assertThat(estimator().estimate(CATEGORY, DESCRIPTION)).isEmpty();
    }

    @Test
    void testEstimate_givenMaximumAboveLimit_shouldReturnEmpty() {
        // given
        givenModelAnswers("{\"minPln\": 100, \"maxPln\": 50001}");

        // when & then
        assertThat(estimator().estimate(CATEGORY, DESCRIPTION)).isEmpty();
    }

    @Test
    void testEstimate_givenAnswerThatIsNotJson_shouldReturnEmpty() {
        // given
        givenModelAnswers("Trudno powiedzieć, zależy od fachowca.");

        // when & then
        assertThat(estimator().estimate(CATEGORY, DESCRIPTION)).isEmpty();
    }

    @Test
    void testEstimate_givenModelFailure_shouldReturnEmpty() {
        // given
        given(chatModel.call(any(Prompt.class))).willThrow(new IllegalStateException("Connection refused"));

        // when & then
        assertThat(estimator().estimate(CATEGORY, DESCRIPTION)).isEmpty();
    }

    @Test
    void testEstimate_givenModelSlowerThanTimeout_shouldReturnEmptyWithinTimeout() {
        // given
        given(chatModel.call(any(Prompt.class))).willAnswer(invocation -> {
            Thread.sleep(5_000);
            return response("{\"minPln\": 80, \"maxPln\": 150}");
        });

        // when
        final var start = System.nanoTime();
        final var estimate = estimator(SHORT_TIMEOUT).estimate(CATEGORY, DESCRIPTION);
        final var elapsed = Duration.ofNanos(System.nanoTime() - start);

        // then
        assertThat(estimate).isEmpty();
        assertThat(elapsed).isLessThan(Duration.ofSeconds(2));
    }

    private OllamaPriceEstimator estimator() {
        return estimator(TIMEOUT);
    }

    private OllamaPriceEstimator estimator(final Duration timeout) {
        given(chatModel.getOptions()).willReturn(ChatOptions.builder().build());
        return new OllamaPriceEstimator(ChatClient.builder(chatModel), new PriceEstimateProperties(timeout));
    }

    private void givenModelAnswers(final String text) {
        given(chatModel.call(any(Prompt.class))).willReturn(response(text));
    }

    private static ChatResponse response(final String text) {
        return new ChatResponse(List.of(new Generation(new AssistantMessage(text))));
    }
}
