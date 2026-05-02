package com.unihub.student.api.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.unihub.student.api.dto.CertificationResponse;
import com.unihub.student.api.dto.ExperienceResponse;
import com.unihub.student.api.dto.GraduationCertResponse;
import com.unihub.student.api.dto.ProjectResponse;
import com.unihub.student.api.dto.StudentProfileResponse;
import com.unihub.student.api.dto.UpdateProfileRequest;
import com.unihub.student.api.dto.UpdateSkillsRequest;
import com.unihub.student.api.dto.SetUniversityRequest;

import com.unihub.student.application.usecase.StudentProfileUseCase;
import com.unihub.student.application.usecase.StudentQueryUseCase;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentProfileUseCase studentProfileUseCase;
    private final StudentQueryUseCase studentQueryUseCase;

    // My Profile 

    @GetMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfileResponse> getMyProfile(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(studentQueryUseCase.getMyProfile(userId));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<StudentProfileResponse> updateProfile(
            Authentication authentication,
            @RequestBody @Valid UpdateProfileRequest request) {

        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(studentProfileUseCase.updateProfile(userId, request));
    }

    @PutMapping("/me/university")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> setUniversity(
            Authentication authentication,
            @RequestBody @Valid SetUniversityRequest request) {

        UUID userId = UUID.fromString(authentication.getName());
        studentProfileUseCase.setUniversity(userId, request.universityId(), request.majorId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/me/skills")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<Void> updateSkills(
            Authentication authentication,
            @RequestBody @Valid UpdateSkillsRequest request) {

        UUID userId = UUID.fromString(authentication.getName());
        studentProfileUseCase.updateSkills(userId, request.skillIds());
        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/me/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<String> uploadPhoto(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {

        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(studentProfileUseCase.uploadPhoto(userId, file));
    }

    @PostMapping(value = "/me/graduation-certificate", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<GraduationCertResponse> uploadGradCert(
            Authentication authentication,
            @RequestParam("file") MultipartFile file) {

        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(studentProfileUseCase.uploadGraduationCertificate(userId, file));
    }

    @GetMapping("/me/graduation-certificate")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<GraduationCertResponse> getGradCertStatus(Authentication authentication) {

        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.ok(studentQueryUseCase.getGradCertStatus(userId));
    }

    // Public Profile 

    @GetMapping("/{studentId}")
    public ResponseEntity<StudentProfileResponse> getPublicProfile(@PathVariable UUID studentId) {
        return ResponseEntity.ok(studentQueryUseCase.getPublicProfile(studentId));
    }

    @GetMapping("/{studentId}/experiences")
    public ResponseEntity<List<ExperienceResponse>> getExperiences(@PathVariable UUID studentId) {
        return ResponseEntity.ok(studentQueryUseCase.getExperiences(studentId));
    }

    @GetMapping("/{studentId}/experiences/{experienceId}")
    public ResponseEntity<ExperienceResponse> getExperience(
            @PathVariable UUID studentId,
            @PathVariable UUID experienceId) {
        return ResponseEntity.ok(studentQueryUseCase.getExperience(studentId, experienceId));
    }

    @GetMapping("/{studentId}/projects")
    public ResponseEntity<List<ProjectResponse>> getProjects(@PathVariable UUID studentId) {
        return ResponseEntity.ok(studentQueryUseCase.getProjects(studentId));
    }

    @GetMapping("/{studentId}/projects/{projectId}")
    public ResponseEntity<ProjectResponse> getProject(
            @PathVariable UUID studentId,
            @PathVariable UUID projectId) {
        return ResponseEntity.ok(studentQueryUseCase.getProject(studentId, projectId));
    }

    @GetMapping("/{studentId}/certifications")
    public ResponseEntity<List<CertificationResponse>> getCertifications(@PathVariable UUID studentId) {
        return ResponseEntity.ok(studentQueryUseCase.getCertifications(studentId));
    }

    @GetMapping("/{studentId}/certifications/{certId}")
    public ResponseEntity<CertificationResponse> getCertification(
            @PathVariable UUID studentId,
            @PathVariable UUID certId) {
        return ResponseEntity.ok(studentQueryUseCase.getCertification(studentId, certId));
    }
}