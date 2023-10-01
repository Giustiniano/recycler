package ae.recycler.be.service.events;


import ae.recycler.be.service.events.serializers.OrderEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.reactive.ReactiveKafkaProducerTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.SenderResult;


@Component
public class OrderEventProducer {
    private static final Logger logger = LoggerFactory.getLogger(OrderEventProducer.class);
    private static final String TOPIC = "orders";

    @Autowired
    private ReactiveKafkaProducerTemplate<String, OrderEvent> reactiveKafkaProducerTemplate;

    public Mono<SenderResult<Void>> sendOrderEvent(OrderEvent orderEvent) {
        logger.info(String.format("#### -> Producing message -> %s", orderEvent));
        return this.reactiveKafkaProducerTemplate.send(TOPIC, orderEvent);
    }
}
