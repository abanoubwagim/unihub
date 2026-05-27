package com.unihub.student.domain.model;

import com.unihub.student.domain.enums.AcademicStatus;
import com.unihub.student.domain.enums.StudentLevel;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "student_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false, unique = true)
    private UUID userId;

    @Column(name = "name")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "profile_photo_url")
    private String profilePhotoUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_status", nullable = false)
    private AcademicStatus academicStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "level")
    private StudentLevel level;

    @Column(name = "university_id")
    private UUID universityId;

    @Column(name = "major_id")
    private UUID majorId;

    @Column(name = "country_id")
    private Integer countryId;

    @Column(name = "looking_for", columnDefinition = "TEXT")
    private String lookingFor;

    @Column(name = "graduation_year")
    private Integer graduationYear;

    @Column(name = "is_verified", nullable = false)
    private boolean certVerified = false;

    @Column(name = "is_locked", nullable = false)
    private boolean certificateLocked = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "student_skills", joinColumns = @JoinColumn(name = "student_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> skills = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentLink> links = new ArrayList<>();
    
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentCertification> certifications = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentExperience> experiences = new ArrayList<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentProject> projects = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


}
