package com.ezra.internshiptracker.service;

import com.ezra.internshiptracker.dto.PageResponse;
import com.ezra.internshiptracker.dto.internship.CreateInternshipRequest;
import com.ezra.internshiptracker.dto.internship.InternshipResponse;
import com.ezra.internshiptracker.dto.internship.InternshipStatusHistoryResponse;
import com.ezra.internshiptracker.dto.internship.UpdateInternshipRequest;
import com.ezra.internshiptracker.entity.Internship;
import com.ezra.internshiptracker.entity.InternshipStatus;
import com.ezra.internshiptracker.entity.InternshipStatusHistory;
import com.ezra.internshiptracker.entity.User;
import com.ezra.internshiptracker.exception.InternshipNotFoundException;
import com.ezra.internshiptracker.exception.InvalidInternshipStatusTransitionException;
import com.ezra.internshiptracker.repository.InternshipRepository;
import com.ezra.internshiptracker.repository.InternshipStatusHistoryRepository;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternshipServiceTest {

    @Mock
    private InternshipRepository internshipRepository;

    @Mock
    private InternshipStatusHistoryRepository statusHistoryRepository;

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
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isEqualTo(response.getCreatedAt());
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
        LocalDateTime previousUpdatedAt = LocalDateTime.of(2026, 1, 1, 12, 0);
        internship.setUpdatedAt(previousUpdatedAt);
        User user = user();

        when(internshipRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(internship));
        when(internshipRepository.save(internship)).thenReturn(internship);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UpdateInternshipRequest request = updateRequest();
        request.setStatusNote("Received OA");

        InternshipResponse response = internshipService.updateInternship(10L, request, 1L);

        assertThat(response.getCompany()).isEqualTo("Anthropic");
        assertThat(response.getPosition()).isEqualTo("Backend Intern");
        assertThat(response.getStatus()).isEqualTo(InternshipStatus.ONLINE_ASSESSMENT);
        assertThat(response.getUpdatedAt()).isAfter(previousUpdatedAt);
        verify(internshipRepository).findByIdAndUserId(10L, 1L);

        ArgumentCaptor<InternshipStatusHistory> historyCaptor =
                ArgumentCaptor.forClass(InternshipStatusHistory.class);
        verify(statusHistoryRepository).save(historyCaptor.capture());

        InternshipStatusHistory history = historyCaptor.getValue();
        assertThat(history.getInternship()).isSameAs(internship);
        assertThat(history.getFromStatus()).isEqualTo(InternshipStatus.APPLIED);
        assertThat(history.getToStatus()).isEqualTo(InternshipStatus.ONLINE_ASSESSMENT);
        assertThat(history.getOperator()).isSameAs(user);
        assertThat(history.getNote()).isEqualTo("Received OA");
        assertThat(history.getCreatedAt()).isNotNull();
    }

    @Test
    void getMyInternshipStatusHistoryRequiresOwnershipAndMapsRows() {
        Internship internship = internship();
        User user = user();
        internship.setUser(user);

        InternshipStatusHistory history = new InternshipStatusHistory();
        history.setId(99L);
        history.setInternship(internship);
        history.setFromStatus(InternshipStatus.APPLIED);
        history.setToStatus(InternshipStatus.ONLINE_ASSESSMENT);
        history.setOperator(user);
        history.setNote("Received OA");
        history.setCreatedAt(LocalDateTime.of(2026, 1, 2, 9, 0));

        when(internshipRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(internship));
        when(statusHistoryRepository.findByInternshipIdAndInternshipUserIdOrderByCreatedAtAsc(10L, 1L))
                .thenReturn(List.of(history));

        List<InternshipStatusHistoryResponse> response =
                internshipService.getMyInternshipStatusHistory(10L, 1L);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getId()).isEqualTo(99L);
        assertThat(response.get(0).getInternshipId()).isEqualTo(10L);
        assertThat(response.get(0).getFromStatus()).isEqualTo(InternshipStatus.APPLIED);
        assertThat(response.get(0).getToStatus()).isEqualTo(InternshipStatus.ONLINE_ASSESSMENT);
        assertThat(response.get(0).getOperatorUserId()).isEqualTo(1L);
        assertThat(response.get(0).getOperatorUsername()).isEqualTo("ezra");
        assertThat(response.get(0).getNote()).isEqualTo("Received OA");
    }

    @Test
    void getMyInternshipStatusHistoryThrowsWhenInternshipDoesNotBelongToCurrentUser() {
        when(internshipRepository.findByIdAndUserId(10L, 2L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> internshipService.getMyInternshipStatusHistory(10L, 2L))
                .isInstanceOf(InternshipNotFoundException.class)
                .hasMessage("Internship not found");
    }

    @Test
    void updateInternshipRejectsInvalidStatusTransition() {
        Internship internship = internship();
        internship.setStatus(InternshipStatus.DRAFT);

        UpdateInternshipRequest request = updateRequest();
        request.setStatus(InternshipStatus.OFFER);

        when(internshipRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(internship));

        assertThatThrownBy(() -> internshipService.updateInternship(10L, request, 1L))
                .isInstanceOf(InvalidInternshipStatusTransitionException.class)
                .hasMessage("Cannot change internship status from DRAFT to OFFER");
    }

    @Test
    void updateInternshipAllowsSameStatusWhenEditingOtherFields() {
        Internship internship = internship();

        UpdateInternshipRequest request = updateRequest();
        request.setStatus(InternshipStatus.APPLIED);

        when(internshipRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(internship));
        when(internshipRepository.save(internship)).thenReturn(internship);

        InternshipResponse response = internshipService.updateInternship(10L, request, 1L);

        assertThat(response.getStatus()).isEqualTo(InternshipStatus.APPLIED);
        assertThat(response.getCompany()).isEqualTo("Anthropic");
        verify(statusHistoryRepository, never()).save(any(InternshipStatusHistory.class));
    }

    @Test
    void updateInternshipRejectsTransitionsFromTerminalStatus() {
        Internship internship = internship();
        internship.setStatus(InternshipStatus.REJECTED);

        UpdateInternshipRequest request = updateRequest();
        request.setStatus(InternshipStatus.TECH_INTERVIEW);

        when(internshipRepository.findByIdAndUserId(10L, 1L)).thenReturn(Optional.of(internship));

        assertThatThrownBy(() -> internshipService.updateInternship(10L, request, 1L))
                .isInstanceOf(InvalidInternshipStatusTransitionException.class)
                .hasMessage("Cannot change internship status from REJECTED to TECH_INTERVIEW");
    }

    @Test
    void getMyInternshipsAllowsUpdatedAtSortField() {
        when(internshipRepository.searchMyInternships(eq(1L), eq(null), eq(null), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(internship())));

        internshipService.getMyInternships(1L, 0, 10, null, null, "updatedAt,asc");

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(internshipRepository)
                .searchMyInternships(eq(1L), eq(null), eq(null), pageableCaptor.capture());

        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("updatedAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.ASC);
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
        internship.setCreatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        internship.setUpdatedAt(LocalDateTime.of(2026, 1, 1, 10, 0));
        internship.setUser(user());
        return internship;
    }

    private User user() {
        User user = new User();
        user.setId(1L);
        user.setUsername("ezra");
        return user;
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
        request.setStatus(InternshipStatus.ONLINE_ASSESSMENT);
        request.setApplicationUrl("https://example.com/updated");
        return request;
    }
}
