package com.unihub.shared.config;

import org.springframework.amqp.core.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.unihub.shared.config.RabbitMqConstants.*;

@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true")
public class UserRegisteredRabbitConfig {

    // Exchanges

    @Bean
    public FanoutExchange userRegisteredExchange() {
        return new FanoutExchange(USER_REGISTERED_EXCHANGE, true, false);
    }

    @Bean
    public FanoutExchange userRegisteredDlx() {
        return new FanoutExchange(USER_REGISTERED_DLX, true, false);
    }

    // Student

    @Bean
    public Queue userRegisteredStudentQueue() {
        return QueueBuilder.durable(USER_REGISTERED_STUDENT_QUEUE)
                .withArgument("x-dead-letter-exchange", USER_REGISTERED_DLX)
                .build();
    }

    @Bean
    public Queue userRegisteredStudentDlQueue() {
        return QueueBuilder.durable(USER_REGISTERED_STUDENT_DL_QUEUE).build();
    }

    @Bean
    public Binding studentRegisteredBinding(Queue userRegisteredStudentQueue,
                                            FanoutExchange userRegisteredExchange) {
        return BindingBuilder.bind(userRegisteredStudentQueue).to(userRegisteredExchange);
    }

    @Bean
    public Binding studentRegisteredDlBinding(Queue userRegisteredStudentDlQueue,
                                              FanoutExchange userRegisteredDlx) {
        return BindingBuilder.bind(userRegisteredStudentDlQueue).to(userRegisteredDlx);
    }

    // Company

    @Bean
    public Queue userRegisteredCompanyQueue() {
        return QueueBuilder.durable(USER_REGISTERED_COMPANY_QUEUE)
                .withArgument("x-dead-letter-exchange", USER_REGISTERED_DLX)
                .build();
    }

    @Bean
    public Queue userRegisteredCompanyDlQueue() {
        return QueueBuilder.durable(USER_REGISTERED_COMPANY_DL_QUEUE).build();
    }

    @Bean
    public Binding companyRegisteredBinding(Queue userRegisteredCompanyQueue,
                                            FanoutExchange userRegisteredExchange) {
        return BindingBuilder.bind(userRegisteredCompanyQueue).to(userRegisteredExchange);
    }

    @Bean
    public Binding companyRegisteredDlBinding(Queue userRegisteredCompanyDlQueue,
                                              FanoutExchange userRegisteredDlx) {
        return BindingBuilder.bind(userRegisteredCompanyDlQueue).to(userRegisteredDlx);
    }

    // University

    @Bean
    public Queue userRegisteredUniversityQueue() {
        return QueueBuilder.durable(USER_REGISTERED_UNIVERSITY_QUEUE)
                .withArgument("x-dead-letter-exchange", USER_REGISTERED_DLX)
                .build();
    }

    @Bean
    public Queue userRegisteredUniversityDlQueue() {
        return QueueBuilder.durable(USER_REGISTERED_UNIVERSITY_DL_QUEUE).build();
    }

    @Bean
    public Binding universityRegisteredBinding(Queue userRegisteredUniversityQueue,
                                               FanoutExchange userRegisteredExchange) {
        return BindingBuilder.bind(userRegisteredUniversityQueue).to(userRegisteredExchange);
    }

    @Bean
    public Binding universityRegisteredDlBinding(Queue userRegisteredUniversityDlQueue,
                                                 FanoutExchange userRegisteredDlx) {
        return BindingBuilder.bind(userRegisteredUniversityDlQueue).to(userRegisteredDlx);
    }
}