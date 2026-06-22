package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.dto.PageResponse;
import com.ezra.internshiptracker.dto.internship.CreateInternshipRequest;
import com.ezra.internshiptracker.dto.internship.InternshipResponse;
import com.ezra.internshiptracker.dto.internship.UpdateInternshipRequest;
import com.ezra.internshiptracker.entity.Internship;
import com.ezra.internshiptracker.entity.InternshipStatus;
import com.ezra.internshiptracker.entity.User;
import com.ezra.internshiptracker.exception.InternshipNotFoundException;
import com.ezra.internshiptracker.repository.InternshipRepository;
import com.ezra.internshiptracker.repository.UserRepository;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternshipServiceTest {

    @Mock
    private InternshipRepository internshipRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InternshipService internshipService;

    @Test
    void createInternshipAttachesCurrentUser() {
        User user = new User();
        user.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(internshipRepository.save(any(Internship.class))).thenAnswer(invocation -> {
            Internship internship = invocation.getArgument(0);
            internship.setId(10L);
            return internship;
        });

        InternshipResponse response = internshipService.createInternship(createRequest(), 1L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getCompany()).isEqualTo("OpenAI");
        verify(internshipRepository).save(any(Internship.class));
    }

    @Test
    void getInternshipByIdUsesCurrentUserScope() {
        Internship internship = internship();

        when(internshipRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(internship));

        InternshipResponse response = internshipService.getMyInternshipById(10L, 1L);

        assertThat(response.getId()).isEqualTo(10L);
        assertThat(response.getCompany()).isEqualTo("OpenAI");
        verify(internshipRepository).findByIdAndUserId(10L, 1L);
    }

    @Test
    void getInternshipByIdThrowsWhenInternshipDoesNotBelongToCurrentUser() {
        when(internshipRepository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internshipService.getMyInternshipById(10L, 2L))
                .isInstanceOf(InternshipNotFoundException.class)
                .hasMessage("Internship not found");
    }

    @Test
    void getMyInternshipsOnlyReadsCurrentUsersRows() {
        when(internshipRepository.searchMyInternships(eq(1L), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(internship())));

        PageResponse<InternshipResponse> internships =
                internshipService.getMyInternships(1L, 0, 10, null, null, "createdAt,desc");

        assertThat(internships.getContent()).hasSize(1);
        assertThat(internships.getContent().get(0).getId()).isEqualTo(10L);
        assertThat(internships.getPage()).isEqualTo(0);
        verify(internshipRepository).searchMyInternships(eq(1L), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void getMyInternshipsSupportsStatusAndKeywordFilters() {
        when(internshipRepository.searchMyInternships(eq(1L), eq(InternshipStatus.APPLIED), eq("open"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(internship())));

        PageResponse<InternshipResponse> internships =
                internshipService.getMyInternships(1L, 0, 10, InternshipStatus.APPLIED, " open ", "company,asc");

        assertThat(internships.getContent()).hasSize(1);
        verify(internshipRepository)
                .searchMyInternships(eq(1L), eq(InternshipStatus.APPLIED), eq("open"), any(Pageable.class));
    }

    @Test
    void getMyInternshipsClampsInvalidPageAndSizeValues() {
        when(internshipRepository.searchMyInternships(eq(1L), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(internship())));

        internshipService.getMyInternships(1L, -5, 500, null, null, "createdAt,desc");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(internshipRepository)
                .searchMyInternships(eq(1L), eq(null), eq(null), pageableCaptor.capture());

        Pageable pageable = pageableCaptor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(100);
    }

    @Test
    void getMyInternshipsDefaultsInvalidSortFieldToCreatedAt() {
        when(internshipRepository.searchMyInternships(eq(1L), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(internship())));

        internshipService.getMyInternships(1L, 0, 10, null, null, "password,asc");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(internshipRepository)
                .searchMyInternships(eq(1L), eq(null), eq(null), pageableCaptor.capture());

        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void getMyInternshipsDefaultsInvalidSortDirectionToDescending() {
        when(internshipRepository.searchMyInternships(eq(1L), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(internship())));

        internshipService.getMyInternships(1L, 0, 10, null, null, "company,sideways");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(internshipRepository)
                .searchMyInternships(eq(1L), eq(null), eq(null), pageableCaptor.capture());

        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("company");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    @Test
    void getMyInternshipsTreatsBlankKeywordAsNoSearch() {
        when(internshipRepository.searchMyInternships(eq(1L), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(internship())));

        internshipService.getMyInternships(1L, 0, 10, null, "   ", "createdAt,desc");

        verify(internshipRepository)
                .searchMyInternships(eq(1L), eq(null), eq(null), any(Pageable.class));
    }

    @Test
    void updateInternshipRequiresOwnership() {
        Internship internship = internship();

        when(internshipRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(internship));
        when(internshipRepository.save(internship)).thenReturn(internship);

        InternshipResponse response = internshipService.updateInternship(10L, updateRequest(), 1L);

        assertThat(response.getCompany()).isEqualTo("Anthropic");
        assertThat(response.getPosition()).isEqualTo("Backend Intern");
        verify(internshipRepository).findByIdAndUserId(10L, 1L);
    }

    @Test
    void deleteInternshipRequiresOwnership() {
        Internship internship = internship();

        when(internshipRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(internship));

        internshipService.deleteInternship(10L, 1L);

        verify(internshipRepository).delete(internship);
    }

    private Internship internship() {
        Internship internship = new Internship();
        internship.setId(10L);
        internship.setCompany("OpenAI");
        internship.setPosition("Software Engineer Intern");
        internship.setLocation("Sydney");
        internship.setStatus(InternshipStatus.APPLIED);
        internship.setApplicationUrl("https://example.com");
        return internship;
    }

    private CreateInternshipRequest createRequest() {
        CreateInternshipRequest request = new CreateInternshipRequest();
        request.setCompany("OpenAI");
        request.setPosition("Software Engineer Intern");
        request.setLocation("Sydney");
        request.setStatus(InternshipStatus.APPLIED);
        request.setApplicationUrl("https://example.com");
        return request;
    }

    private UpdateInternshipRequest updateRequest() {
        UpdateInternshipRequest request = new UpdateInternshipRequest();
        request.setCompany("Anthropic");
        request.setPosition("Backend Intern");
        request.setLocation("Sydney");
        request.setStatus(InternshipStatus.INTERVIEW);
        request.setApplicationUrl("https://example.com/updated");
        return request;
    }
}
