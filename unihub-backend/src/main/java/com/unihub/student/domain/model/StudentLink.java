package com.unihub.student.domain.model;

import java.util.UUID;

import com.unihub.student.domain.enums.LinkType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student_links")
@Getter
@Setter
@NoArgsConstructor
public class StudentLink {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private StudentProfile student;

    @Enumerated(EnumType.STRING)
    @Column(name = "link_type")
    private LinkType linkType;

    @Column(name = "label")
    private String label;

    @Column(nullable = false)
    private String url;

}
