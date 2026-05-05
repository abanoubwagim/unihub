package com.unihub.student.domain.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.unihub.shared.exception.UniversityAlreadySetException;
import com.unihub.student.domain.enums.AcademicStatus;
import com.unihub.student.domain.enums.StudentLevel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "student_profile")
@Getter
@Setter
@NoArgsConstructor
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
    private boolean verified = false;

    @Column(name = "cert_attempts", nullable = false)
    private int certAttempts = 0;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "student_skills", joinColumns = @JoinColumn(name = "student_id"), inverseJoinColumns = @JoinColumn(name = "skill_id"))
    private Set<Skill> skills = new HashSet<>();

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentLink> links = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void setUniversityOnce(UUID universityId, UUID majorId) {
        if(this.universityId != null) {
           throw new UniversityAlreadySetException("University and major can only be set once.");
        }

        this.universityId = universityId;
        this.majorId = majorId;
    }

}
