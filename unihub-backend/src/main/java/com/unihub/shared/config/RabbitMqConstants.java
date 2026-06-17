package com.unihub.shared.config;

public final class RabbitMqConstants {

    // User Deleted
    public static final String USER_DELETED_EXCHANGE = "unihub.user.deleted";
    public static final String USER_DELETED_DLX = "unihub.user.deleted.dlx";
    public static final String USER_DELETED_QUEUE = "unihub.user.deleted.queue";
    public static final String USER_DELETED_DL_QUEUE = "unihub.user.deleted.queue.dl";
    
    // User Registered
    public static final String USER_REGISTERED_EXCHANGE = "unihub.user.registered";
    public static final String USER_REGISTERED_DLX = "unihub.user.registered.dlx";
    public static final String USER_REGISTERED_STUDENT_QUEUE = "unihub.user.registered.student";
    public static final String USER_REGISTERED_STUDENT_DL_QUEUE = "unihub.user.registered.student.dl";
    public static final String USER_REGISTERED_COMPANY_QUEUE = "unihub.user.registered.company";
    public static final String USER_REGISTERED_COMPANY_DL_QUEUE = "unihub.user.registered.company.dl";
    public static final String USER_REGISTERED_UNIVERSITY_QUEUE = "unihub.user.registered.university";
    public static final String USER_REGISTERED_UNIVERSITY_DL_QUEUE = "unihub.user.registered.university.dl";

    private RabbitMqConstants() {
    }
}