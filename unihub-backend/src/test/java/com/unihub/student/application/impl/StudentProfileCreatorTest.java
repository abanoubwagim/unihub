package com.unihub.student.application.impl;

import com.unihub.identity.application.event.EmailVerifiedEvent;
import com.unihub.identity.domain.enums.Role;
import com.unihub.shared.events.UserDeletedEvent;
import com.unihub.student.application.listener.StudentProfileCreationListener;
import com.unihub.student.application.listener.StudentUserDeletedListener;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentProfileCreator Tests")
class StudentProfileCreatorTest {

    private final UUID userId = UUID.randomUUID();

    @Mock
    private StudentProfileRepository studentProfileRepository;

    @InjectMocks
    private StudentProfileCreatorImpl profileCreator;

    @Test
    @DisplayName("should create empty profile when user has no profile yet")
    void shouldCreateEmptyProfile() {
        when(studentProfileRepository.existsByUserId(userId)).thenReturn(false);
        when(studentProfileRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertThatNoException().isThrownBy(() -> profileCreator.createEmptyProfile(userId));

        verify(studentProfileRepository).save(argThat(p -> p.getUserId().equals(userId)));
    }

    @Test
    @DisplayName("should skip creation when profile already exists")
    void shouldSkipWhenProfileAlreadyExists() {
        when(studentProfileRepository.existsByUserId(userId)).thenReturn(true);

        profileCreator.createEmptyProfile(userId);

        verify(studentProfileRepository, never()).save(any());
    }

    @Test
    @DisplayName("should swallow DataIntegrityViolationException on concurrent profile creation")
    void shouldSwallowConcurrencyException() {
        when(studentProfileRepository.existsByUserId(userId)).thenReturn(false);
        when(studentProfileRepository.save(any())).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatNoException().isThrownBy(() -> profileCreator.createEmptyProfile(userId));
    }
}


@ExtendWith(MockitoExtension.class)
@DisplayName("StudentProfileCreationListener Tests")
class StudentProfileCreationListenerTest {

    @Mock
    private com.unihub.student.application.StudentProfileCreator studentProfileCreator;

    @InjectMocks
    private StudentProfileCreationListener listener;

    @Test
    @DisplayName("should create student profile when STUDENT email is verified")
    void shouldCreateProfileForStudent() {
        UUID userId = UUID.randomUUID();
        EmailVerifiedEvent event = new EmailVerifiedEvent(userId, Role.STUDENT);

        listener.onEmailVerified(event);

        verify(studentProfileCreator).createEmptyProfile(userId);
    }

    @Test
    @DisplayName("should NOT create student profile for non-STUDENT roles (COMPANY, UNIVERSITY)")
    void shouldNotCreateProfileForNonStudentRoles() {
        UUID userId = UUID.randomUUID();

        listener.onEmailVerified(new EmailVerifiedEvent(userId, Role.COMPANY));
        listener.onEmailVerified(new EmailVerifiedEvent(userId, Role.UNIVERSITY));

        verify(studentProfileCreator, never()).createEmptyProfile(any());
    }
}


@ExtendWith(MockitoExtension.class)
@DisplayName("StudentUserDeletedListener Tests")
class StudentUserDeletedListenerTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID profileId = UUID.randomUUID();

    @Mock
    private StudentProfileRepository profileRepository;

    @Mock
    private StudentExperienceRepository experienceRepository;

    @Mock
    private StudentProjectRepository projectRepository;

    @Mock
    private StudentCertificationRepository certificationRepository;

    @Mock
    private GraduationCertificateRepository gradCertRepository;

    @InjectMocks
    private StudentUserDeletedListener listener;

    @Test
    @DisplayName("should delete all student data when user is deleted")
    void shouldDeleteAllStudentDataOnUserDeleted() {
        StudentProfile profile = new StudentProfile();
        ReflectionTestUtils.setField(profile, "id", profileId);
        ReflectionTestUtils.setField(profile, "userId", userId);

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        listener.onUserDeleted(new UserDeletedEvent(userId));

        verify(certificationRepository).deleteAllByStudent_Id(profileId);
        verify(projectRepository).deleteAllByStudent_Id(profileId);
        verify(experienceRepository).deleteAllByStudent_Id(profileId);
        verify(gradCertRepository).deleteAllByStudentId(profileId);
        verify(profileRepository).delete(profile);
    }

    @Test
    @DisplayName("should do nothing when no student profile found for deleted user")
    void shouldDoNothingWhenNoProfileFound() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThatNoException().isThrownBy(() -> listener.onUserDeleted(new UserDeletedEvent(userId)));

        verify(certificationRepository, never()).deleteAllByStudent_Id(any());
        verify(projectRepository, never()).deleteAllByStudent_Id(any());
        verify(experienceRepository, never()).deleteAllByStudent_Id(any());
        verify(gradCertRepository, never()).deleteAllByStudentId(any());
        verify(profileRepository, never()).delete(any());
    }

    @Test
    @DisplayName("should delete certifications BEFORE profile to avoid FK violations")
    void shouldDeleteChildrenBeforeProfile() {
        StudentProfile profile = new StudentProfile();
        ReflectionTestUtils.setField(profile, "id", profileId);
        ReflectionTestUtils.setField(profile, "userId", userId);

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));

        org.mockito.InOrder inOrder = inOrder(certificationRepository, projectRepository,
                experienceRepository, gradCertRepository, profileRepository);

        listener.onUserDeleted(new UserDeletedEvent(userId));

        inOrder.verify(certificationRepository).deleteAllByStudent_Id(profileId);
        inOrder.verify(projectRepository).deleteAllByStudent_Id(profileId);
        inOrder.verify(experienceRepository).deleteAllByStudent_Id(profileId);
        inOrder.verify(gradCertRepository).deleteAllByStudentId(profileId);
        inOrder.verify(profileRepository).delete(profile);
    }
}