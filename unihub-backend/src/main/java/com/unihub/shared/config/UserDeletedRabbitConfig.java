package com.unihub.shared.config;

import org.springframework.amqp.core.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.unihub.shared.config.RabbitMqConstants.*;

@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true")
public class UserDeletedRabbitConfig {

    // Exchanges

    @Bean
    public FanoutExchange userDeletedExchange() {
        return new FanoutExchange(USER_DELETED_EXCHANGE, true, false);
    }

    @Bean
    public FanoutExchange userDeletedDlx() {
        return new FanoutExchange(USER_DELETED_DLX, true, false);
    }

    @Bean
    public Queue userDeletedQueue() {
        return QueueBuilder.durable(USER_DELETED_QUEUE)
                .withArgument("x-dead-letter-exchange", USER_DELETED_DLX)
                .build();
    }

    @Bean
    public Queue userDeletedDlQueue() {
        return QueueBuilder.durable(USER_DELETED_DL_QUEUE).build();
    }


    @Bean
    public Binding userDeletedBinding(Queue userDeletedQueue,
                                      FanoutExchange userDeletedExchange) {
        return BindingBuilder.bind(userDeletedQueue).to(userDeletedExchange);
    }

    @Bean
    public Binding userDeletedDlBinding(Queue userDeletedDlQueue,
                                        FanoutExchange userDeletedDlx) {
        return BindingBuilder.bind(userDeletedDlQueue).to(userDeletedDlx);
    }


}