package com.drobnapomoc.drobnapomoc.servicerequest.adapter.out.llm;

import com.drobnapomoc.drobnapomoc.servicerequest.application.port.out.PriceEstimatorPort;
import org.junit.jupiter.api.Test;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class PriceEstimatorSelectionTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(OllamaPriceEstimator.class, DisabledPriceEstimator.class)
            .withBean(ChatClient.Builder.class, () -> ChatClient.builder(mock(ChatModel.class)))
            .withBean(PriceEstimateProperties.class, () -> new PriceEstimateProperties(Duration.ofSeconds(5)));

    @Test
    void testPriceEstimator_givenEstimatesEnabled_shouldUseOllama() {
        contextRunner.withPropertyValues("drobnapomoc.price-estimate.enabled=true")
                .run(context -> assertThat(context.getBean(PriceEstimatorPort.class)).isInstanceOf(OllamaPriceEstimator.class));
    }

    @Test
    void testPriceEstimator_givenEstimatesDisabled_shouldUseNoEstimates() {
        contextRunner.withPropertyValues("drobnapomoc.price-estimate.enabled=false")
                .run(context -> assertThat(context.getBean(PriceEstimatorPort.class)).isInstanceOf(DisabledPriceEstimator.class));
    }

    @Test
    void testPriceEstimator_givenNoSetting_shouldUseNoEstimates() {
        contextRunner.run(context -> assertThat(context.getBean(PriceEstimatorPort.class)).isInstanceOf(DisabledPriceEstimator.class));
    }

    @Test
    void testEstimate_givenEstimatesDisabled_shouldReturnEmpty() {
        assertThat(new DisabledPriceEstimator().estimate("Hydraulik", "Cieknie kran.")).isEmpty();
    }
}
