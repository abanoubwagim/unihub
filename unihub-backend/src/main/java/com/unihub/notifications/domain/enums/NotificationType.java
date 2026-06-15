package com.unihub.notifications.domain.enums;

public enum NotificationType {

    // Student-facing

    // A company accepted the student's job application.
    JOB_APPLICATION_ACCEPTED,

    // A company rejected the student's job application.
    JOB_APPLICATION_REJECTED,

    // The student's graduation certificate was approved by their university.
    CERTIFICATE_APPROVED,

    // The student's graduation certificate was rejected by their university.
    CERTIFICATE_REJECTED,

    // The student has been linked to a university after certificate approval.
    UNIVERSITY_LINKED,


    // Company-facing

    // A new student has applied to one of the company's job postings.
    JOB_APPLICATION_RECEIVED,

    // A university approved the company's partnership request.
    PARTNERSHIP_ACCEPTED,

    // A university rejected the company's partnership request.
    PARTNERSHIP_REJECTED,


    // University-facing

    // A student submitted a graduation certificate awaiting review.
    CERTIFICATE_SUBMITTED,

    // A company sent a partnership request to this university.
    PARTNERSHIP_REQUESTED,


    // Cross-cutting

    // A new chat message was received (sender → recipient).
    CHAT_MESSAGE_RECEIVED,

    // Sent once, immediately after a new user verifies their email address.
    WELCOME
}