package com.unihub.student.application.impl;

import com.unihub.shared.exception.NotFoundException;
import com.unihub.student.api.dto.req.LinkRequest;
import com.unihub.student.api.dto.res.LinkResponse;
import com.unihub.student.domain.enums.LinkType;
import com.unihub.student.domain.model.StudentLink;
import com.unihub.student.domain.model.StudentProfile;
import com.unihub.student.domain.repository.StudentLinkRepository;
import com.unihub.student.domain.repository.StudentProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentLinkUseCase Tests")
class StudentLinkUseCaseTest {

    private final UUID userId = UUID.randomUUID();
    private final UUID linkId = UUID.randomUUID();

    @Mock
    private StudentProfileRepository profileRepository;

    @Mock
    private StudentLinkRepository linkRepository;

    @InjectMocks
    private StudentLinkUseCaseImpl linkUseCase;

    private StudentProfile profile;

    @BeforeEach
    void setUp() {
        profile = new StudentProfile();
        ReflectionTestUtils.setField(profile, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(profile, "userId", userId);
    }

    @Test
    @DisplayName("should add a link and return correct response")
    void shouldAddLinkSuccessfully() {
        LinkRequest request = new LinkRequest(LinkType.GITHUB, "My GitHub", "https://github.com/abanoub");

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(linkRepository.save(any())).thenAnswer(inv -> {
            StudentLink l = inv.getArgument(0);
            ReflectionTestUtils.setField(l, "id", linkId);
            return l;
        });

        LinkResponse response = linkUseCase.add(userId, request);

        assertThat(response).isNotNull();
        assertThat(response.type()).isEqualTo(LinkType.GITHUB);
        assertThat(response.label()).isEqualTo("My GitHub");
        assertThat(response.url()).isEqualTo("https://github.com/abanoub");
    }

    @Test
    @DisplayName("should save link linked to the correct student profile")
    void shouldSaveLinkLinkedToProfile() {
        LinkRequest request = new LinkRequest(LinkType.LINKEDIN, null, "https://linkedin.com/in/abanoub");

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(linkRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        linkUseCase.add(userId, request);

        ArgumentCaptor<StudentLink> captor = ArgumentCaptor.forClass(StudentLink.class);
        verify(linkRepository).save(captor.capture());
        assertThat(captor.getValue().getStudent()).isSameAs(profile);
    }

    @Test
    @DisplayName("should throw NotFoundException when profile not found on add")
    void shouldThrowWhenProfileNotFoundOnAdd() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        LinkRequest request = new LinkRequest(LinkType.GITHUB, null, "https://github.com");

        assertThatThrownBy(() -> linkUseCase.add(userId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Student profile not found");

        verify(linkRepository, never()).save(any());
    }

    @Test
    @DisplayName("should support all link types (GITHUB, LINKEDIN, PORTFOLIO, OTHER)")
    void shouldSupportAllLinkTypes() {
        for (LinkType type : LinkType.values()) {
            LinkRequest request = new LinkRequest(type, "Label", "https://example.com");

            when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
            when(linkRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            LinkResponse response = linkUseCase.add(userId, request);
            assertThat(response.type()).isEqualTo(type);
        }
    }

    @Test
    @DisplayName("should update link fields successfully")
    void shouldUpdateLinkSuccessfully() {
        StudentLink existing = buildLink(LinkType.GITHUB, "Old Label", "https://github.com/old");
        LinkRequest updateReq = new LinkRequest(LinkType.PORTFOLIO, "Portfolio", "https://portfolio.com");

        when(linkRepository.findByIdAndStudent_UserId(linkId, userId)).thenReturn(Optional.of(existing));
        when(linkRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LinkResponse response = linkUseCase.update(userId, linkId, updateReq);

        assertThat(response.type()).isEqualTo(LinkType.PORTFOLIO);
        assertThat(response.label()).isEqualTo("Portfolio");
        assertThat(response.url()).isEqualTo("https://portfolio.com");
    }

    @Test
    @DisplayName("should throw NotFoundException when link not found on update")
    void shouldThrowWhenLinkNotFoundOnUpdate() {
        when(linkRepository.findByIdAndStudent_UserId(linkId, userId)).thenReturn(Optional.empty());
        LinkRequest request = new LinkRequest(LinkType.GITHUB, null, "https://github.com");

        assertThatThrownBy(() -> linkUseCase.update(userId, linkId, request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Link not found");

        verify(linkRepository, never()).save(any());
    }

    @Test
    @DisplayName("should delete link successfully")
    void shouldDeleteLinkSuccessfully() {
        StudentLink existing = buildLink(LinkType.GITHUB, "GitHub", "https://github.com");
        when(linkRepository.findByIdAndStudent_UserId(linkId, userId)).thenReturn(Optional.of(existing));

        assertThatNoException().isThrownBy(() -> linkUseCase.delete(userId, linkId));

        verify(linkRepository).delete(existing);
    }

    @Test
    @DisplayName("should throw NotFoundException when link not found on delete")
    void shouldThrowWhenLinkNotFoundOnDelete() {
        when(linkRepository.findByIdAndStudent_UserId(linkId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> linkUseCase.delete(userId, linkId))
                .isInstanceOf(NotFoundException.class);

        verify(linkRepository, never()).delete(any());
    }

    @Test
    @DisplayName("should return all links for the user")
    void shouldReturnAllLinksForUser() {
        StudentLink link1 = buildLink(LinkType.GITHUB, "GitHub", "https://github.com");
        StudentLink link2 = buildLink(LinkType.LINKEDIN, "LinkedIn", "https://linkedin.com");

        when(linkRepository.findAllByStudent_UserId(userId)).thenReturn(List.of(link1, link2));

        List<LinkResponse> response = linkUseCase.getAll(userId);

        assertThat(response).hasSize(2);
        assertThat(response).extracting(LinkResponse::type)
                .containsExactlyInAnyOrder(LinkType.GITHUB, LinkType.LINKEDIN);
    }

    @Test
    @DisplayName("should return empty list when user has no links")
    void shouldReturnEmptyListWhenNoLinks() {
        when(linkRepository.findAllByStudent_UserId(userId)).thenReturn(List.of());

        List<LinkResponse> response = linkUseCase.getAll(userId);

        assertThat(response).isEmpty();
    }

    @Test
    @DisplayName("should return single link by id")
    void shouldReturnSingleLinkById() {
        StudentLink link = buildLink(LinkType.PORTFOLIO, "Portfolio", "https://my-portfolio.com");
        when(linkRepository.findByIdAndStudent_UserId(linkId, userId)).thenReturn(Optional.of(link));

        LinkResponse response = linkUseCase.getOne(userId, linkId);

        assertThat(response.type()).isEqualTo(LinkType.PORTFOLIO);
        assertThat(response.url()).isEqualTo("https://my-portfolio.com");
    }

    @Test
    @DisplayName("should throw NotFoundException when link not found by id")
    void shouldThrowWhenLinkNotFoundById() {
        when(linkRepository.findByIdAndStudent_UserId(linkId, userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> linkUseCase.getOne(userId, linkId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Link not found");
    }

    private StudentLink buildLink(LinkType type, String label, String url) {
        StudentLink link = StudentLink.builder()
                .student(profile)
                .linkType(type)
                .label(label)
                .url(url)
                .build();
        ReflectionTestUtils.setField(link, "id", linkId);
        return link;
    }
}