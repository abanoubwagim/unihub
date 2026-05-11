package com.unihub.shared.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    public static final String USER_DELETED_EXCHANGE = "unihub.user.deleted";
    public static final String USER_DELETED_DLX = "unihub.user.deleted.dlx";
    public static final String USER_DELETED_STUDENT_QUEUE = "unihub.user.deleted.student";
    public static final String USER_DELETED_STUDENT_DL_QUEUE = "unihub.user.deleted.student.dl";

    @Bean
    public FanoutExchange userDeletedExchange() {
        return new FanoutExchange(USER_DELETED_EXCHANGE, true, false);
    }

    @Bean
    public FanoutExchange userDeletedDlx() {
        return new FanoutExchange(USER_DELETED_DLX, true, false);
    }

    @Bean
    public Queue userDeletedStudentQueue() {
        return QueueBuilder.durable(USER_DELETED_STUDENT_QUEUE)
                .withArgument("x-dead-letter-exchange", USER_DELETED_DLX)
                .build();
    }

    @Bean
    public Queue userDeletedStudentDlQueue() {
        return QueueBuilder.durable(USER_DELETED_STUDENT_DL_QUEUE).build();
    }

    @Bean
    public Binding studentDeletedBinding(Queue userDeletedStudentQueue,
            FanoutExchange userDeletedExchange) {
        return BindingBuilder.bind(userDeletedStudentQueue).to(userDeletedExchange);
    }

    @Bean
    public Binding studentDeletedDlBinding(Queue userDeletedStudentDlQueue,
            FanoutExchange userDeletedDlx) {
        return BindingBuilder.bind(userDeletedStudentDlQueue).to(userDeletedDlx);
    }

    // USER REGISTERED

    public static final String USER_REGISTERED_EXCHANGE = "unihub.user.registered";
    public static final String USER_REGISTERED_DLX = "unihub.user.registered.dlx";
    public static final String USER_REGISTERED_STUDENT_QUEUE = "unihub.user.registered.student";
    public static final String USER_REGISTERED_STUDENT_DL_QUEUE = "unihub.user.registered.student.dl";
    public static final String USER_REGISTERED_COMPANY_QUEUE = "unihub.user.registered.company";
    public static final String USER_REGISTERED_COMPANY_DL_QUEUE = "unihub.user.registered.company.dl";
    public static final String USER_REGISTERED_UNIVERSITY_QUEUE = "unihub.user.registered.university";
    public static final String USER_REGISTERED_UNIVERSITY_DL_QUEUE = "unihub.user.registered.university.dl";

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

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}