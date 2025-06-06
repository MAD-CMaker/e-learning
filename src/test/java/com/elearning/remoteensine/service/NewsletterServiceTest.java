package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.NewsletterDAO;
import com.elearning.remoteensine.model.NewsLetterInscription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NewsletterServiceTest {

    @Mock
    private NewsletterDAO newsletterDAO;

    private NewsletterService newsletterService;

    @BeforeEach
    void setUp() {
        newsletterService = new NewsletterService(newsletterDAO);
    }

    @Test
    void createEmail_withValidEmail_shouldSaveNewInscription() throws SQLException {
        // Arrange
        String validEmail = "test@example.com";
        NewsLetterInscription savedInscription = new NewsLetterInscription(validEmail);
        savedInscription.setInscriptionId(1);
        
        when(newsletterDAO.searchByEmail(validEmail)).thenReturn(null);
        when(newsletterDAO.saveInscription(any(NewsLetterInscription.class))).thenReturn(savedInscription);

        // Act
        NewsLetterInscription result = newsletterService.createEmail(validEmail);

        // Assert
        assertNotNull(result);
        assertEquals(validEmail, result.getEmail());
        assertEquals(1, result.getInscriptionId());
        assertTrue(result.isActive());
        verify(newsletterDAO).searchByEmail(validEmail);
        verify(newsletterDAO).saveInscription(any(NewsLetterInscription.class));
    }

    @Test
    void createEmail_withInactiveEmail_shouldReactivateInscription() throws SQLException {
        // Arrange
        String email = "inactive@example.com";
        NewsLetterInscription existingInscription = new NewsLetterInscription(email);
        existingInscription.setInscriptionId(2);
        existingInscription.setActive(false);
        
        when(newsletterDAO.searchByEmail(email)).thenReturn(existingInscription);
        when(newsletterDAO.updateInscriptionStatus(existingInscription)).thenReturn(true);

        // Act
        NewsLetterInscription result = newsletterService.createEmail(email);

        // Assert
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertTrue(result.isActive());
        verify(newsletterDAO).searchByEmail(email);
        verify(newsletterDAO).updateInscriptionStatus(existingInscription);
        verify(newsletterDAO, never()).saveInscription(any(NewsLetterInscription.class));
    }

    @Test
    void cancelInscription_withActiveEmail_shouldDeactivateInscription() throws SQLException {
        // Arrange
        String email = "active@example.com";
        NewsLetterInscription activeInscription = new NewsLetterInscription(email);
        activeInscription.setInscriptionId(3);
        activeInscription.setActive(true);
        
        when(newsletterDAO.searchByEmail(email)).thenReturn(activeInscription);
        when(newsletterDAO.updateInscriptionStatus(any(NewsLetterInscription.class))).thenReturn(true);

        // Act
        boolean result = newsletterService.cancelInscription(email);

        // Assert
        assertTrue(result);
        assertFalse(activeInscription.isActive());
        verify(newsletterDAO).searchByEmail(email);
        verify(newsletterDAO).updateInscriptionStatus(activeInscription);
    }

    @Test
    void checkInscriptionStatus_withActiveEmail_shouldReturnTrue() throws SQLException {
        // Arrange
        String email = "active@example.com";
        NewsLetterInscription activeInscription = new NewsLetterInscription(email);
        activeInscription.setActive(true);
        
        when(newsletterDAO.searchByEmail(email)).thenReturn(activeInscription);

        // Act
        boolean result = newsletterService.checkInscriptionStatus(email);

        // Assert
        assertTrue(result);
        verify(newsletterDAO).searchByEmail(email);
    }

    @Test
    void listAllActivesEmails_shouldReturnOnlyActiveEmails() throws SQLException {
        // Arrange
        NewsLetterInscription inscription1 = new NewsLetterInscription("email1@example.com");
        inscription1.setActive(true);
        
        NewsLetterInscription inscription2 = new NewsLetterInscription("email2@example.com");
        inscription2.setActive(true);
        
        List<NewsLetterInscription> activeInscriptions = Arrays.asList(inscription1, inscription2);
        List<String> expectedEmails = Arrays.asList("email1@example.com", "email2@example.com");
        
        when(newsletterDAO.listAllInscriptions(true)).thenReturn(activeInscriptions);

        // Act
        List<String> result = newsletterService.listAllActivesEmails();

        // Assert
        assertEquals(expectedEmails.size(), result.size());
        assertTrue(result.containsAll(expectedEmails));
        verify(newsletterDAO).listAllInscriptions(true);
    }

    @Test
    void createEmail_withInvalidEmailFormat_shouldThrowException() throws SQLException {
        // Arrange
        String invalidEmail = "invalid-email";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> newsletterService.createEmail(invalidEmail)
        );
        
        assertEquals("Invalid email format.", exception.getMessage());
        verify(newsletterDAO, never()).searchByEmail(anyString());
        verify(newsletterDAO, never()).saveInscription(any(NewsLetterInscription.class));
    }
}