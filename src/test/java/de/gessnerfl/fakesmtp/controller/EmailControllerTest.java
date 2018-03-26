package de.gessnerfl.fakesmtp.controller;

import de.gessnerfl.fakesmtp.model.Email;
import de.gessnerfl.fakesmtp.repository.EmailRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class EmailControllerTest {
    @Mock
    private Model model;
    @Mock
    private EmailRepository emailRepository;
    @InjectMocks
    private EmailController sut;

    @Test
    public void shouldReturnEmailsPaged() {
        final Page<Email> page = createFirstPageEmail();
        when(emailRepository.findAll(any(Pageable.class))).thenReturn(page);

        String result = sut.getAll(0, 5, model);

        assertEquals(EmailController.EMAIL_LIST_VIEW, result);

        verify(emailRepository).findAll(argThat(matchPageable(0, 5)));
        verifyNoMoreInteractions(emailRepository);
        verify(model).addAttribute(EmailController.EMAIL_LIST_MODEL_NAME, page);
    }

    @Test
    public void shouldReturnRedirectToFirstPageWhenRequestedPageIsOutOfRange() {
        final Page<Email> page = mock(Page.class);
        when(page.getTotalPages()).thenReturn(2);
        when(page.getNumber()).thenReturn(3);
        when(emailRepository.findAll(any(Pageable.class))).thenReturn(page);

        String result = sut.getAll(3, 5, model);

        assertEquals(EmailController.REDIRECT_EMAIL_LIST_VIEW, result);

        verify(emailRepository).findAll(argThat(matchPageable(3, 5)));
        verifyNoMoreInteractions(emailRepository);
    }

    @Test
    public void shouldNotRedirectToFirstPageWhenNoDataIsAvailable() {
        final Page<Email> page = mock(Page.class);
        when(page.getNumber()).thenReturn(0);
        when(emailRepository.findAll(any(Pageable.class))).thenReturn(page);

        String result = sut.getAll(0, 5, model);

        assertEquals(EmailController.EMAIL_LIST_VIEW, result);

        verify(emailRepository).findAll(argThat(matchPageable(0, 5)));
        verifyNoMoreInteractions(emailRepository);
    }

    @Test
    public void shouldRedirectToFirstPageWhenPageNumberIsBelowNull() {
        String result = sut.getAll(-1, 5, model);

        assertEquals(EmailController.REDIRECT_EMAIL_LIST_VIEW, result);
        verifyZeroInteractions(emailRepository);
    }

    @Test
    public void shouldRedirectToFirstPageWhenPageSizeIsNull() {
        String result = sut.getAll(0, 0, model);

        assertEquals(EmailController.REDIRECT_EMAIL_LIST_VIEW, result);
        verifyZeroInteractions(emailRepository);
    }

    @Test
    public void shouldRedirectToFirstPageWhenPageSizeIsBelowNull() {
        String result = sut.getAll(0, -1, model);

        assertEquals(EmailController.REDIRECT_EMAIL_LIST_VIEW, result);
        verifyZeroInteractions(emailRepository);
    }

    @Test
    public void shouldReturnSingleEmailWhenIdIsValid() {
        final long id = 12L;
        final Email mail = mock(Email.class);
        when(emailRepository.findById(id)).thenReturn(Optional.of(mail));

        String result = sut.getEmailById(id, model);

        assertEquals(EmailController.SINGLE_EMAIL_VIEW, result);

        verify(emailRepository).findById(id);
        verify(model).addAttribute(EmailController.SINGLE_EMAIL_MODEL_NAME, mail);
    }

    @Test
    public void shouldReturnRedirectToListPageWhenIdIsNotValid() {
        final long id = 12L;
        when(emailRepository.findById(id)).thenReturn(Optional.empty());

        String result = sut.getEmailById(id, model);

        assertEquals(EmailController.REDIRECT_EMAIL_LIST_VIEW, result);

        verify(emailRepository).findById(id);
    }

    private Page<Email> createFirstPageEmail() {
        Page<Email> page = mock(Page.class);
        when(page.getNumber()).thenReturn(0);
        return page;
    }


    private ArgumentMatcher<Pageable> matchPageable(int page, int size) {
        return (item) ->  item.getPageNumber() == page && item.getPageSize() == size;
    }
}