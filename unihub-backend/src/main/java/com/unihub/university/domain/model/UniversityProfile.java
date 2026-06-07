package com.unihub.university.domain.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "university_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniversityProfile {

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

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "address")
    private String address;

    @Column(name = "country_id")
    private Integer countryId;

    @Column(name = "student_count", nullable = false)
    private int studentCount = 0;

    @Column(name = "graduate_count", nullable = false)
    private int graduateCount = 0;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "university_majors",
            joinColumns = @JoinColumn(name = "university_id"),
            inverseJoinColumns = @JoinColumn(name = "major_id")
    )
    private Set<Major> majors = new HashSet<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}