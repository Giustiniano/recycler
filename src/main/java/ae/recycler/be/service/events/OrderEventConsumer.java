package ae.recycler.be.service.events;

import ae.recycler.be.service.OrderService;
import ae.recycler.be.service.events.serializers.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.reactive.ReactiveKafkaConsumerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

@Slf4j

public class OrderEventConsumer {
    @Autowired
    private final ReactiveKafkaConsumerTemplate<String, OrderEvent> reactiveKafkaConsumerTemplate;

    @Autowired
    private OrderService orderService;

    public OrderEventConsumer(ReactiveKafkaConsumerTemplate<String, OrderEvent> reactiveKafkaConsumerTemplate) {
        this.reactiveKafkaConsumerTemplate = reactiveKafkaConsumerTemplate;
    }


    @EventListener(ApplicationStartedEvent.class)
    public Flux<OrderEvent> startKafkaConsumer() {
        return reactiveKafkaConsumerTemplate
                .receiveAutoAck()
                // .delayElements(Duration.ofSeconds(2L)) // BACKPRESSURE
                .doOnNext(consumerRecord -> log.info("received key={}, value={} from topic={}, offset={}",
                        consumerRecord.key(),
                        consumerRecord.value(),
                        consumerRecord.topic(),
                        consumerRecord.offset())
                )
                .map(ConsumerRecord::value)
                .doOnNext(orderEvent -> {
                    orderService.assignNewOrderToVehicle(orderEvent);
                    log.info("successfully consumed {}={}", OrderEvent.class.getSimpleName(), orderEvent);
                })
                .doOnError(throwable -> log.error("something bad happened while consuming event : {}", throwable.getMessage()));
    }

}
