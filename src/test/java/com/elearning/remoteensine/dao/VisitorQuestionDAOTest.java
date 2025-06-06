package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.VisitorQuestion;
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
public class VisitorQuestionDAOTest {

    @Mock
    private DatabaseConnector databaseConnector;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private VisitorQuestionDAO visitorQuestionDAO;

    @BeforeEach
    void setUp() throws SQLException {
        visitorQuestionDAO = new VisitorQuestionDAO(databaseConnector);
        when(databaseConnector.getConnection()).thenReturn(connection);
    }

    @Test
    void saveQuestion_shouldSaveAndReturnQuestionWithId() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);

        VisitorQuestion question = new VisitorQuestion();
        question.setVisitorName("John Doe");
        question.setVisitorEmail("john.doe@example.com");
        question.setQuestionText("How can I enroll in a course?");
        LocalDateTime questionTime = LocalDateTime.now();
        question.setQuestionHour(questionTime);

        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(5);

        // Act
        VisitorQuestion savedQuestion = visitorQuestionDAO.saveQuestion(question);

        // Assert
        assertEquals(5, savedQuestion.getQuestionId());
        assertEquals("John Doe", savedQuestion.getVisitorName());
        assertEquals("john.doe@example.com", savedQuestion.getVisitorEmail());
        assertEquals("How can I enroll in a course?", savedQuestion.getQuestionText());
        assertEquals(questionTime, savedQuestion.getQuestionHour());

        verify(preparedStatement).setString(1, "John Doe");
        verify(preparedStatement).setString(2, "john.doe@example.com");
        verify(preparedStatement).setString(3, "How can I enroll in a course?");
        verify(preparedStatement).setTimestamp(eq(4), any(Timestamp.class));
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).getGeneratedKeys();
    }

    @Test
    void searchById_shouldReturnQuestionWhenFound() throws SQLException {
        // Arrange
        int questionId = 5;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        
        when(resultSet.getInt("question_id")).thenReturn(questionId);
        when(resultSet.getString("visitor_name")).thenReturn("John Doe");
        when(resultSet.getString("visitor_email")).thenReturn("john.doe@example.com");
        when(resultSet.getString("question_text")).thenReturn("How can I enroll in a course?");
        when(resultSet.getTimestamp("question_hour_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getString("answer")).thenReturn(null);
        when(resultSet.getTimestamp("answer_hour_date")).thenReturn(null);
        when(resultSet.getInt("professor_responsible_id")).thenReturn(0);
        when(resultSet.wasNull()).thenReturn(true);

        // Act
        VisitorQuestion foundQuestion = visitorQuestionDAO.searchById(questionId);

        // Assert
        assertNotNull(foundQuestion);
        assertEquals(questionId, foundQuestion.getQuestionId());
        assertEquals("John Doe", foundQuestion.getVisitorName());
        assertEquals("john.doe@example.com", foundQuestion.getVisitorEmail());
        assertEquals("How can I enroll in a course?", foundQuestion.getQuestionText());
        assertNull(foundQuestion.getAnswer());
        assertNull(foundQuestion.getProfessorResponsibleId());

        verify(preparedStatement).setInt(1, questionId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void searchById_shouldReturnNullWhenNotFound() throws SQLException {
        // Arrange
        int questionId = 999;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // Act
        VisitorQuestion foundQuestion = visitorQuestionDAO.searchById(questionId);

        // Assert
        assertNull(foundQuestion);
        verify(preparedStatement).setInt(1, questionId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void listAllQuestions_shouldReturnAllQuestions() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // Return true twice for two questions, then false

        // First question
        when(resultSet.getInt("question_id")).thenReturn(5, 6);
        when(resultSet.getString("visitor_name")).thenReturn("John Doe", "Jane Smith");
        when(resultSet.getString("visitor_email")).thenReturn("john.doe@example.com", "jane.smith@example.com");
        when(resultSet.getString("question_text")).thenReturn("How can I enroll in a course?", "What are the payment methods?");
        when(resultSet.getTimestamp("question_hour_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        
        // First question is unanswered, second is answered
        when(resultSet.getString("answer")).thenReturn(null, "You can pay with credit card or PayPal.");
        when(resultSet.getTimestamp("answer_hour_date")).thenReturn(null, Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getInt("professor_responsible_id")).thenReturn(0, 2);
        when(resultSet.wasNull()).thenReturn(true, false);

        // Act
        List<VisitorQuestion> questions = visitorQuestionDAO.listAllQuestions(false);

        // Assert
        assertNotNull(questions);
        assertEquals(2, questions.size());

        // First question
        assertEquals(5, questions.get(0).getQuestionId());
        assertEquals("John Doe", questions.get(0).getVisitorName());
        assertEquals("john.doe@example.com", questions.get(0).getVisitorEmail());
        assertEquals("How can I enroll in a course?", questions.get(0).getQuestionText());
        assertNull(questions.get(0).getAnswer());
        assertNull(questions.get(0).getProfessorResponsibleId());

        // Second question
        assertEquals(6, questions.get(1).getQuestionId());
        assertEquals("Jane Smith", questions.get(1).getVisitorName());
        assertEquals("jane.smith@example.com", questions.get(1).getVisitorEmail());
        assertEquals("What are the payment methods?", questions.get(1).getQuestionText());
        assertEquals("You can pay with credit card or PayPal.", questions.get(1).getAnswer());
        assertEquals(2, questions.get(1).getProfessorResponsibleId());

        verify(preparedStatement).executeQuery();
    }

    @Test
    void listAllQuestions_shouldReturnOnlyUnansweredQuestions() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false); // Return true once for one unanswered question, then false

        when(resultSet.getInt("question_id")).thenReturn(5);
        when(resultSet.getString("visitor_name")).thenReturn("John Doe");
        when(resultSet.getString("visitor_email")).thenReturn("john.doe@example.com");
        when(resultSet.getString("question_text")).thenReturn("How can I enroll in a course?");
        when(resultSet.getTimestamp("question_hour_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getString("answer")).thenReturn(null);
        when(resultSet.getTimestamp("answer_hour_date")).thenReturn(null);
        when(resultSet.getInt("professor_responsible_id")).thenReturn(0);
        when(resultSet.wasNull()).thenReturn(true);

        // Act
        List<VisitorQuestion> questions = visitorQuestionDAO.listAllQuestions(true);

        // Assert
        assertNotNull(questions);
        assertEquals(1, questions.size());
        assertEquals(5, questions.get(0).getQuestionId());
        assertEquals("John Doe", questions.get(0).getVisitorName());
        assertEquals("john.doe@example.com", questions.get(0).getVisitorEmail());
        assertEquals("How can I enroll in a course?", questions.get(0).getQuestionText());
        assertNull(questions.get(0).getAnswer());
        assertNull(questions.get(0).getProfessorResponsibleId());

        verify(preparedStatement).executeQuery();
    }

    @Test
    void updateQuestion_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        VisitorQuestion question = new VisitorQuestion();
        question.setQuestionId(5);
        question.setAnswer("You can enroll through our website.");
        question.setAnswerHour(LocalDateTime.now());
        question.setProfessorResponsibleId(2);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = visitorQuestionDAO.updateQuestion(question);

        // Assert
        assertTrue(result);

        verify(preparedStatement).setString(1, "You can enroll through our website.");
        verify(preparedStatement).setTimestamp(eq(2), any(Timestamp.class));
        verify(preparedStatement).setInt(3, 2);
        verify(preparedStatement).setInt(4, 5);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void updateQuestion_shouldReturnFalseWhenNoRowsAffected() throws SQLException {
        // Arrange
        VisitorQuestion question = new VisitorQuestion();
        question.setQuestionId(999); // Non-existent ID
        question.setAnswer("You can enroll through our website.");
        question.setAnswerHour(LocalDateTime.now());
        question.setProfessorResponsibleId(2);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = visitorQuestionDAO.updateQuestion(question);

        // Assert
        assertFalse(result);

        verify(preparedStatement).setString(1, "You can enroll through our website.");
        verify(preparedStatement).setTimestamp(eq(2), any(Timestamp.class));
        verify(preparedStatement).setInt(3, 2);
        verify(preparedStatement).setInt(4, 999);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void deleteQuestion_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        int questionId = 5;
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = visitorQuestionDAO.deleteQuestion(questionId);

        // Assert
        assertTrue(result);

        verify(preparedStatement).setInt(1, questionId);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void deleteQuestion_shouldReturnFalseWhenNoRowsAffected() throws SQLException {
        // Arrange
        int questionId = 999; // Non-existent ID
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Act
        boolean result = visitorQuestionDAO.deleteQuestion(questionId);

        // Assert
        assertFalse(result);

        verify(preparedStatement).setInt(1, questionId);
        verify(preparedStatement).executeUpdate();
    }
}