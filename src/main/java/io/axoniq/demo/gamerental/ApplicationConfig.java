package io.axoniq.demo.gamerental;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.distributed.AnnotationRoutingStrategy;
import org.axonframework.commandhandling.distributed.RoutingStrategy;
import org.axonframework.commandhandling.distributed.UnresolvedRoutingKeyPolicy;
import org.axonframework.common.caching.Cache;
import org.axonframework.common.caching.WeakReferenceCache;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.eventhandling.EventBus;
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.SnapshotTriggerDefinition;
import org.axonframework.eventsourcing.Snapshotter;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.interceptors.LoggingInterceptor;
import org.axonframework.queryhandling.QueryBus;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {

    @Bean
    @Qualifier("messageSerializer")
    public Serializer messageSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule())
                    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
                    .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        return JacksonSerializer.builder()
                                .objectMapper(objectMapper)
                                .lenientDeserialization()
                                .build();
    }

    @Bean
    public RoutingStrategy routingStrategy() {
        return AnnotationRoutingStrategy.builder()
                                        .fallbackRoutingStrategy(UnresolvedRoutingKeyPolicy.RANDOM_KEY)
                                        .build();
    }

    @Bean
    public LoggingInterceptor<Message<?>> loggingInterceptor() {
        return new LoggingInterceptor<>();
    }

    @Autowired
    public void configureLoggingInterceptorFor(CommandBus commandBus,
                                               LoggingInterceptor<Message<?>> loggingInterceptor) {
        commandBus.registerDispatchInterceptor(loggingInterceptor);
        commandBus.registerHandlerInterceptor(loggingInterceptor);
    }

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public void configureLoggingInterceptorFor(EventBus eventBus, LoggingInterceptor<Message<?>> loggingInterceptor) {
        eventBus.registerDispatchInterceptor(loggingInterceptor);
    }

    @Autowired
    public void configureLoggingInterceptorFor(EventProcessingConfigurer eventProcessingConfigurer,
                                               LoggingInterceptor<Message<?>> loggingInterceptor) {
        eventProcessingConfigurer.registerDefaultHandlerInterceptor((config, processorName) -> loggingInterceptor);
    }

    @Autowired
    @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
    public void configureLoggingInterceptorFor(QueryBus queryBus, LoggingInterceptor<Message<?>> loggingInterceptor) {
        queryBus.registerDispatchInterceptor(loggingInterceptor);
        queryBus.registerHandlerInterceptor(loggingInterceptor);
    }

    @Bean
    public SnapshotTriggerDefinition gameSnapshotTrigger(Snapshotter snapshotter) {
        return new EventCountSnapshotTriggerDefinition(snapshotter, 50);
    }

    @Bean
    public Cache gameCache() {
        return new WeakReferenceCache();
    }
}
