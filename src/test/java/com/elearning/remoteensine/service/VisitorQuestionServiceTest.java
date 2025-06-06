package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.VisitorQuestionDAO;
import com.elearning.remoteensine.dao.UserDAO;
import com.elearning.remoteensine.model.VisitorQuestion;
import com.elearning.remoteensine.model.Professor;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VisitorQuestionServiceTest {

    @Mock
    private VisitorQuestionDAO questionDAO;

    @Mock
    private UserDAO userDAO;

    private VisitorQuestionService visitorQuestionService;
    private Professor professor;
    private User regularUser;
    private VisitorQuestion question;

    @BeforeEach
    void setUp() {
        visitorQuestionService = new VisitorQuestionService(questionDAO, userDAO);
        
        // Setup professor
        professor = new Professor("John Doe", "john@example.com", "password", "Computer Science");
        professor.setIdUser(1);
        
        // Setup regular user
        regularUser = new User("Jane Smith", "jane@example.com", "password", UserType.STUDENT);
        regularUser.setIdUser(2);
        
        // Setup question
        question = new VisitorQuestion("Visitor Name", "visitor@example.com", "How do I enroll in a course?");
        question.setQuestionId(1);
    }

    @Test
    void submitQuestion_shouldSubmitValidQuestion() throws SQLException {
        // Arrange
        String visitorName = "John Visitor";
        String visitorEmail = "john.visitor@example.com";
        String questionText = "How do I access the course materials?";
        
        VisitorQuestion expectedQuestion = new VisitorQuestion(visitorName, visitorEmail, questionText);
        expectedQuestion.setQuestionId(1);
        
        when(questionDAO.saveQuestion(any(VisitorQuestion.class))).thenReturn(expectedQuestion);
        
        // Act
        VisitorQuestion result = visitorQuestionService.submitQuestion(visitorName, visitorEmail, questionText);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getQuestionId());
        assertEquals(visitorName, result.getVisitorName());
        assertEquals(visitorEmail, result.getVisitorEmail());
        assertEquals(questionText, result.getQuestionText());
        
        verify(questionDAO).saveQuestion(any(VisitorQuestion.class));
    }

    @Test
    void submitQuestion_shouldUseAnonymousNameWhenNameIsEmpty() throws SQLException {
        // Arrange
        String visitorName = "";
        String visitorEmail = "anonymous@example.com";
        String questionText = "How do I reset my password?";
        
        VisitorQuestion expectedQuestion = new VisitorQuestion("Visitante Anônimo", visitorEmail, questionText);
        expectedQuestion.setQuestionId(1);
        
        when(questionDAO.saveQuestion(any(VisitorQuestion.class))).thenReturn(expectedQuestion);
        
        // Act
        VisitorQuestion result = visitorQuestionService.submitQuestion(visitorName, visitorEmail, questionText);
        
        // Assert
        assertNotNull(result);
        assertEquals("Visitante Anônimo", result.getVisitorName());
        assertEquals(visitorEmail, result.getVisitorEmail());
        assertEquals(questionText, result.getQuestionText());
        
        verify(questionDAO).saveQuestion(any(VisitorQuestion.class));
    }

    @Test
    void submitQuestion_shouldThrowExceptionForInvalidEmail() {
        // Arrange
        String visitorName = "John Visitor";
        String visitorEmail = "invalid-email";
        String questionText = "How do I access the course materials?";
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            visitorQuestionService.submitQuestion(visitorName, visitorEmail, questionText);
        });
        
        assertEquals("\nInvalid visitor email format.", exception.getMessage());
        
        verifyNoInteractions(questionDAO);
    }

    @Test
    void submitQuestion_shouldThrowExceptionForEmptyQuestionText() {
        // Arrange
        String visitorName = "John Visitor";
        String visitorEmail = "john.visitor@example.com";
        String questionText = "";
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            visitorQuestionService.submitQuestion(visitorName, visitorEmail, questionText);
        });
        
        assertEquals("The question text cannot be empty.", exception.getMessage());
        
        verifyNoInteractions(questionDAO);
    }

    @Test
    void answerQuestion_shouldAnswerQuestionSuccessfully() throws SQLException {
        // Arrange
        int questionId = 1;
        int professorId = 1;
        String answerText = "You can enroll through our website by clicking on the 'Enroll' button.";
        
        VisitorQuestion unansweredQuestion = new VisitorQuestion("Visitor", "visitor@example.com", "How do I enroll?");
        unansweredQuestion.setQuestionId(questionId);
        
        when(questionDAO.searchById(questionId)).thenReturn(unansweredQuestion);
        when(userDAO.searchById(professorId)).thenReturn(professor);
        when(questionDAO.updateQuestion(any(VisitorQuestion.class))).thenReturn(true);
        
        // Act
        VisitorQuestion result = visitorQuestionService.answerQuestion(questionId, professorId, answerText);
        
        // Assert
        assertNotNull(result);
        assertEquals(questionId, result.getQuestionId());
        assertEquals(answerText, result.getAnswer());
        assertEquals(professorId, result.getProfessorResponsibleId());
        
        verify(questionDAO).searchById(questionId);
        verify(userDAO).searchById(professorId);
        verify(questionDAO).updateQuestion(any(VisitorQuestion.class));
    }

    @Test
    void answerQuestion_shouldThrowExceptionForNonExistentQuestion() throws SQLException {
        // Arrange
        int questionId = 999;
        int professorId = 1;
        String answerText = "This is an answer.";
        
        when(questionDAO.searchById(questionId)).thenReturn(null);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            visitorQuestionService.answerQuestion(questionId, professorId, answerText);
        });
        
        assertEquals("Question ID " + questionId + " not found.", exception.getMessage());
        
        verify(questionDAO).searchById(questionId);
        verifyNoInteractions(userDAO);
    }

    @Test
    void answerQuestion_shouldThrowExceptionForNonExistentProfessor() throws SQLException {
        // Arrange
        int questionId = 1;
        int professorId = 999;
        String answerText = "This is an answer.";
        
        VisitorQuestion unansweredQuestion = new VisitorQuestion("Visitor", "visitor@example.com", "How do I enroll?");
        unansweredQuestion.setQuestionId(questionId);
        
        when(questionDAO.searchById(questionId)).thenReturn(unansweredQuestion);
        when(userDAO.searchById(professorId)).thenReturn(null);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            visitorQuestionService.answerQuestion(questionId, professorId, answerText);
        });
        
        assertEquals("Professor ID " + professorId + " not found.", exception.getMessage());
        
        verify(questionDAO).searchById(questionId);
        verify(userDAO).searchById(professorId);
    }

    @Test
    void answerQuestion_shouldThrowExceptionForNonProfessorUser() throws SQLException {
        // Arrange
        int questionId = 1;
        int userId = 2; // Regular user, not a professor
        String answerText = "This is an answer.";
        
        VisitorQuestion unansweredQuestion = new VisitorQuestion("Visitor", "visitor@example.com", "How do I enroll?");
        unansweredQuestion.setQuestionId(questionId);
        
        when(questionDAO.searchById(questionId)).thenReturn(unansweredQuestion);
        when(userDAO.searchById(userId)).thenReturn(regularUser);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            visitorQuestionService.answerQuestion(questionId, userId, answerText);
        });
        
        assertEquals("Professor ID " + userId + " not found.", exception.getMessage());
        
        verify(questionDAO).searchById(questionId);
        verify(userDAO).searchById(userId);
    }

    @Test
    void searchQuestion_shouldReturnQuestionWhenFound() throws SQLException {
        // Arrange
        int questionId = 1;
        
        when(questionDAO.searchById(questionId)).thenReturn(question);
        
        // Act
        VisitorQuestion result = visitorQuestionService.searchQuestion(questionId);
        
        // Assert
        assertNotNull(result);
        assertEquals(questionId, result.getQuestionId());
        assertEquals("Visitor Name", result.getVisitorName());
        assertEquals("visitor@example.com", result.getVisitorEmail());
        assertEquals("How do I enroll in a course?", result.getQuestionText());
        
        verify(questionDAO).searchById(questionId);
    }

    @Test
    void searchQuestion_shouldReturnNullWhenNotFound() throws SQLException {
        // Arrange
        int questionId = 999;
        
        when(questionDAO.searchById(questionId)).thenReturn(null);
        
        // Act
        VisitorQuestion result = visitorQuestionService.searchQuestion(questionId);
        
        // Assert
        assertNull(result);
        
        verify(questionDAO).searchById(questionId);
    }

    @Test
    void listAllQuestions_shouldReturnAllQuestions() throws SQLException {
        // Arrange
        List<VisitorQuestion> expectedQuestions = new ArrayList<>();
        
        VisitorQuestion question1 = new VisitorQuestion("Visitor 1", "visitor1@example.com", "Question 1");
        question1.setQuestionId(1);
        
        VisitorQuestion question2 = new VisitorQuestion("Visitor 2", "visitor2@example.com", "Question 2");
        question2.setQuestionId(2);
        question2.setAnswer("Answer to question 2");
        question2.setProfessorResponsibleId(1);
        
        expectedQuestions.add(question1);
        expectedQuestions.add(question2);
        
        when(questionDAO.listAllQuestions(false)).thenReturn(expectedQuestions);
        
        // Act
        List<VisitorQuestion> result = visitorQuestionService.listAllQuestions(false);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getQuestionId());
        assertEquals(2, result.get(1).getQuestionId());
        assertNull(result.get(0).getAnswer());
        assertEquals("Answer to question 2", result.get(1).getAnswer());
        
        verify(questionDAO).listAllQuestions(false);
    }

    @Test
    void listAllQuestions_shouldReturnOnlyUnansweredQuestions() throws SQLException {
        // Arrange
        List<VisitorQuestion> expectedQuestions = new ArrayList<>();
        
        VisitorQuestion question1 = new VisitorQuestion("Visitor 1", "visitor1@example.com", "Question 1");
        question1.setQuestionId(1);
        
        expectedQuestions.add(question1);
        
        when(questionDAO.listAllQuestions(true)).thenReturn(expectedQuestions);
        
        // Act
        List<VisitorQuestion> result = visitorQuestionService.listAllQuestions(true);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getQuestionId());
        assertNull(result.get(0).getAnswer());
        
        verify(questionDAO).listAllQuestions(true);
    }

    @Test
    void deleteVisitorQuestions_shouldDeleteQuestionSuccessfully() throws SQLException, IllegalAccessException {
        // Arrange
        int questionId = 1;
        int professorId = 1;
        
        when(questionDAO.searchById(questionId)).thenReturn(question);
        when(userDAO.searchById(professorId)).thenReturn(professor);
        when(questionDAO.deleteQuestion(questionId)).thenReturn(true);
        
        // Act
        boolean result = visitorQuestionService.deleteVisitorQuestions(questionId, professorId);
        
        // Assert
        assertTrue(result);
        
        verify(questionDAO).searchById(questionId);
        verify(userDAO).searchById(professorId);
        verify(questionDAO).deleteQuestion(questionId);
    }

    @Test
    void deleteVisitorQuestions_shouldThrowExceptionForNonExistentQuestion() throws SQLException {
        // Arrange
        int questionId = 999;
        int professorId = 1;
        
        when(questionDAO.searchById(questionId)).thenReturn(null);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            visitorQuestionService.deleteVisitorQuestions(questionId, professorId);
        });
        
        assertEquals("Question ID " + questionId + " not found.", exception.getMessage());
        
        verify(questionDAO).searchById(questionId);
        verifyNoInteractions(userDAO);
    }

    @Test
    void deleteVisitorQuestions_shouldThrowExceptionForNonProfessorUser() throws SQLException {
        // Arrange
        int questionId = 1;
        int userId = 2; // Regular user, not a professor
        
        when(questionDAO.searchById(questionId)).thenReturn(question);
        when(userDAO.searchById(userId)).thenReturn(regularUser);
        
        // Act & Assert
        IllegalAccessException exception = assertThrows(IllegalAccessException.class, () -> {
            visitorQuestionService.deleteVisitorQuestions(questionId, userId);
        });
        
        assertEquals("User not authorized to delete visitor questions.", exception.getMessage());
        
        verify(questionDAO).searchById(questionId);
        verify(userDAO).searchById(userId);
    }
}