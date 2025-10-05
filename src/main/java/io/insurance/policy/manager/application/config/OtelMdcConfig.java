package io.insurance.policy.manager.application.config;

import io.micrometer.observation.Observation;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;

import org.slf4j.MDC;
import org.springframework.boot.actuate.autoconfigure.observation.ObservationRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;


@Configuration
public class OtelMdcConfig {

    @Bean
    public ObservationHandler<io.micrometer.observation.Observation.Context> mdcHandler() {
        return new ObservationHandler<>() {
            @Override
            public boolean supportsContext(Observation.Context context) {
                return true;
            }

            @Override
            public void onStart(io.micrometer.observation.Observation.Context context) {
                Span span = Span.current();
                SpanContext spanContext = span.getSpanContext();
                if (spanContext.isValid()) {
                    MDC.put("traceId", spanContext.getTraceId());
                    MDC.put("spanId", spanContext.getSpanId());
                }
            }

            @Override
            public void onStop(io.micrometer.observation.Observation.Context context) {
                MDC.clear();
            }
        };
    }

    @Bean
    public ObservationRegistryCustomizer<ObservationRegistry> registryCustomizer(
            ObservationHandler<io.micrometer.observation.Observation.Context> mdcHandler) {
        return registry -> registry.observationConfig().observationHandler(mdcHandler);
    }
}