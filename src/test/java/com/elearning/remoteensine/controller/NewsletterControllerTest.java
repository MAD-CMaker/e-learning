package com.elearning.remoteensine.controller;

import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.NewsLetterInscription;
import com.elearning.remoteensine.model.enums.UserType;
import com.elearning.remoteensine.service.NewsletterService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsletterControllerTest {

    @Mock
    private NewsletterService newsletterService;

    @Mock
    private HttpSession session;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private HttpServletRequest request;

    private NewsletterController newsletterController;
    private User studentUser;
    private final String REFERER_URL = "http://localhost:8080/cursos";

    @BeforeEach
    void setUp() {
        newsletterController = new NewsletterController(newsletterService);
        
        // Setup a student user for testing
        studentUser = new User("Test Student", "student@example.com", "password", UserType.STUDENT);
        studentUser.setIdUser(1);
        
        // Setup common request behavior
        when(request.getHeader("Referer")).thenReturn(REFERER_URL);
    }

    @Test
    void inscreverNaNewsletter_withLoggedInStudent_shouldSubscribeSuccessfully() throws SQLException {
        // Arrange
        when(session.getAttribute("usuarioLogado")).thenReturn(studentUser);
        NewsLetterInscription inscription = new NewsLetterInscription(studentUser.getEmail());
        when(newsletterService.createEmail(studentUser.getEmail())).thenReturn(inscription);

        // Act
        String result = newsletterController.inscreverNaNewsletter(session, redirectAttributes, request);

        // Assert
        assertEquals("redirect:" + REFERER_URL, result);
        verify(newsletterService).createEmail(studentUser.getEmail());
        verify(redirectAttributes).addFlashAttribute(
            eq("sucesso_global"), 
            contains("Inscrição na newsletter realizada/confirmada com sucesso")
        );
    }

    @Test
    void inscreverNaNewsletter_withExistingSubscription_shouldReactivateSuccessfully() throws SQLException {
        // Arrange
        when(session.getAttribute("usuarioLogado")).thenReturn(studentUser);
        NewsLetterInscription existingInscription = new NewsLetterInscription(studentUser.getEmail());
        existingInscription.setActive(true);
        when(newsletterService.createEmail(studentUser.getEmail())).thenReturn(existingInscription);

        // Act
        String result = newsletterController.inscreverNaNewsletter(session, redirectAttributes, request);

        // Assert
        assertEquals("redirect:" + REFERER_URL, result);
        verify(newsletterService).createEmail(studentUser.getEmail());
        verify(redirectAttributes).addFlashAttribute(
            eq("sucesso_global"), 
            contains("Inscrição na newsletter realizada/confirmada com sucesso")
        );
    }

    @Test
    void cancelarInscricaoNewsletter_withLoggedInStudent_shouldCancelSuccessfully() throws SQLException {
        // Arrange
        when(session.getAttribute("usuarioLogado")).thenReturn(studentUser);
        when(newsletterService.cancelInscription(studentUser.getEmail())).thenReturn(true);

        // Act
        String result = newsletterController.cancelarInscricaoNewsletter(session, redirectAttributes, request);

        // Assert
        assertEquals("redirect:" + REFERER_URL, result);
        verify(newsletterService).cancelInscription(studentUser.getEmail());
        verify(redirectAttributes).addFlashAttribute(
            eq("sucesso_global"), 
            contains("Inscrição na newsletter cancelada")
        );
    }

    @Test
    void inscreverNaNewsletter_withSQLException_shouldHandleErrorGracefully() throws SQLException {
        // Arrange
        when(session.getAttribute("usuarioLogado")).thenReturn(studentUser);
        when(newsletterService.createEmail(studentUser.getEmail())).thenThrow(new SQLException("Database error"));

        // Act
        String result = newsletterController.inscreverNaNewsletter(session, redirectAttributes, request);

        // Assert
        assertEquals("redirect:" + REFERER_URL, result);
        verify(redirectAttributes).addFlashAttribute(
            eq("erro_global"), 
            eq("Erro técnico ao tentar se inscrever na newsletter. Tente novamente.")
        );
    }

    @Test
    void inscreverNaNewsletter_withIllegalArgumentException_shouldHandleErrorGracefully() throws SQLException {
        // Arrange
        when(session.getAttribute("usuarioLogado")).thenReturn(studentUser);
        String errorMessage = "This email is already subscribed and active in the newsletter.";
        when(newsletterService.createEmail(studentUser.getEmail())).thenThrow(new IllegalArgumentException(errorMessage));

        // Act
        String result = newsletterController.inscreverNaNewsletter(session, redirectAttributes, request);

        // Assert
        assertEquals("redirect:" + REFERER_URL, result);
        verify(redirectAttributes).addFlashAttribute(eq("erro_global"), eq(errorMessage));
    }
}