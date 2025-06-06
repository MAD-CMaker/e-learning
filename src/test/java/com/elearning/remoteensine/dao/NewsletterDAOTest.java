package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.NewsLetterInscription;
import com.elearning.remoteensine.util.DatabaseConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NewsletterDAOTest {

    @Mock
    private DatabaseConnector databaseConnector;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private NewsletterDAO newsletterDAO;

    @BeforeEach
    void setUp() throws SQLException {
        newsletterDAO = new NewsletterDAO(databaseConnector);
        when(databaseConnector.getConnection()).thenReturn(connection);
    }

    @Test
    void saveInscription_shouldSaveAndReturnInscriptionWithId() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);

        NewsLetterInscription inscription = new NewsLetterInscription("test@example.com");
        LocalDateTime inscriptionDate = LocalDateTime.now();
        inscription.setInscriptionDate(inscriptionDate);

        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(10);

        // Act
        NewsLetterInscription savedInscription = newsletterDAO.saveInscription(inscription);

        // Assert
        assertEquals(10, savedInscription.getInscriptionId());
        assertEquals("test@example.com", savedInscription.getEmail());
        assertEquals(inscriptionDate, savedInscription.getInscriptionDate());
        assertTrue(savedInscription.isActive());

        verify(preparedStatement).setString(1, "test@example.com");
        verify(preparedStatement).setTimestamp(eq(2), any(Timestamp.class));
        verify(preparedStatement).setBoolean(3, true);
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).getGeneratedKeys();
    }

    @Test
    void searchByEmail_shouldReturnInscriptionWhenFound() throws SQLException {
        // Arrange
        String email = "test@example.com";
        LocalDateTime inscriptionDate = LocalDateTime.now();

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        
        when(resultSet.getInt("inscription_id")).thenReturn(10);
        when(resultSet.getString("email")).thenReturn(email);
        when(resultSet.getTimestamp("inscription_hour")).thenReturn(Timestamp.valueOf(inscriptionDate));
        when(resultSet.getBoolean("active")).thenReturn(true);

        // Act
        NewsLetterInscription foundInscription = newsletterDAO.searchByEmail(email);

        // Assert
        assertNotNull(foundInscription);
        assertEquals(10, foundInscription.getInscriptionId());
        assertEquals(email, foundInscription.getEmail());
        assertEquals(inscriptionDate, foundInscription.getInscriptionDate());
        assertTrue(foundInscription.isActive());

        verify(preparedStatement).setString(1, email);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void searchByEmail_shouldReturnNullWhenNotFound() throws SQLException {
        // Arrange
        String email = "nonexistent@example.com";

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // Act
        NewsLetterInscription foundInscription = newsletterDAO.searchByEmail(email);

        // Assert
        assertNull(foundInscription);

        verify(preparedStatement).setString(1, email);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void updateInscriptionStatus_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        NewsLetterInscription inscription = new NewsLetterInscription("test@example.com");
        inscription.setInscriptionId(10);
        inscription.setActive(false); // Setting to inactive for the update

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = newsletterDAO.updateInscriptionStatus(inscription);

        // Assert
        assertTrue(result);

        verify(preparedStatement).setBoolean(1, false);
        verify(preparedStatement).setInt(2, 10);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void listAllInscriptions_shouldReturnAllInscriptions() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // Return true twice for two inscriptions, then false

        LocalDateTime date1 = LocalDateTime.now().minusDays(1);
        LocalDateTime date2 = LocalDateTime.now();

        // First inscription
        when(resultSet.getInt("inscription_id")).thenReturn(10, 11);
        when(resultSet.getString("email")).thenReturn("test1@example.com", "test2@example.com");
        when(resultSet.getTimestamp("inscription_hour")).thenReturn(
                Timestamp.valueOf(date1), 
                Timestamp.valueOf(date2)
        );
        when(resultSet.getBoolean("active")).thenReturn(true, false);

        // Act
        List<NewsLetterInscription> inscriptions = newsletterDAO.listAllInscriptions(false); // false = list all

        // Assert
        assertNotNull(inscriptions);
        assertEquals(2, inscriptions.size());

        // First inscription
        assertEquals(10, inscriptions.get(0).getInscriptionId());
        assertEquals("test1@example.com", inscriptions.get(0).getEmail());
        assertEquals(date1, inscriptions.get(0).getInscriptionDate());
        assertTrue(inscriptions.get(0).isActive());

        // Second inscription
        assertEquals(11, inscriptions.get(1).getInscriptionId());
        assertEquals("test2@example.com", inscriptions.get(1).getEmail());
        assertEquals(date2, inscriptions.get(1).getInscriptionDate());
        assertFalse(inscriptions.get(1).isActive());

        verify(preparedStatement).executeQuery();
    }

    @Test
    void listAllInscriptions_shouldReturnOnlyActiveInscriptions() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false); // Return true once for one active inscription, then false

        LocalDateTime date = LocalDateTime.now();

        when(resultSet.getInt("inscription_id")).thenReturn(10);
        when(resultSet.getString("email")).thenReturn("test1@example.com");
        when(resultSet.getTimestamp("inscription_hour")).thenReturn(Timestamp.valueOf(date));
        when(resultSet.getBoolean("active")).thenReturn(true);

        // Act
        List<NewsLetterInscription> inscriptions = newsletterDAO.listAllInscriptions(true); // true = only active

        // Assert
        assertNotNull(inscriptions);
        assertEquals(1, inscriptions.size());

        assertEquals(10, inscriptions.get(0).getInscriptionId());
        assertEquals("test1@example.com", inscriptions.get(0).getEmail());
        assertEquals(date, inscriptions.get(0).getInscriptionDate());
        assertTrue(inscriptions.get(0).isActive());

        verify(preparedStatement).executeQuery();
    }

    @Test
    void saveInscription_shouldThrowExceptionWhenDuplicateEmail() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);

        NewsLetterInscription inscription = new NewsLetterInscription("duplicate@example.com");

        // Simulate a SQL exception for duplicate key
        SQLException sqlException = new SQLException("Duplicate entry", "23000", 1062);
        when(preparedStatement.executeUpdate()).thenThrow(sqlException);

        // Act & Assert
        SQLException exception = assertThrows(SQLException.class, () -> {
            newsletterDAO.saveInscription(inscription);
        });
        
        assertEquals("23000", exception.getSQLState());
        
        verify(preparedStatement).setString(1, "duplicate@example.com");
        verify(preparedStatement).setTimestamp(eq(2), any(Timestamp.class));
        verify(preparedStatement).setBoolean(3, true);
        verify(preparedStatement).executeUpdate();
    }
}