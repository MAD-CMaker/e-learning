package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.ExamQuestion;
import com.elearning.remoteensine.model.enums.ExerciseType;
import com.elearning.remoteensine.util.DatabaseConnector;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExamQuestionDAOTest {

    @Mock
    private DatabaseConnector databaseConnector;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private ExamQuestionDAO examQuestionDAO;

    @BeforeEach
    void setUp() throws SQLException {
        examQuestionDAO = new ExamQuestionDAO(databaseConnector);
        when(databaseConnector.getConnection()).thenReturn(connection);
    }

    @Test
    void saveQuestion_shouldSaveAndReturnQuestionWithId() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);

        ExamQuestion question = new ExamQuestion();
        question.setIdDefinitionExam(1);
        question.setStatement("What is the capital of France?");
        question.setExerciseType(ExerciseType.MULTIPLA_ESCOLHA);
        question.setOptions("[{\"id\":\"A\",\"text\":\"Paris\"},{\"id\":\"B\",\"text\":\"London\"},{\"id\":\"C\",\"text\":\"Berlin\"}]");
        question.setCorrectAnswer("A");
        question.setGrade(10.0);
        question.setExamSequence(1);

        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(5);

        // Act
        ExamQuestion savedQuestion = examQuestionDAO.saveQuestion(question);

        // Assert
        assertEquals(5, savedQuestion.getIdExamQuestion());
        assertEquals(1, savedQuestion.getIdDefinitionExam());
        assertEquals("What is the capital of France?", savedQuestion.getStatement());
        assertEquals(ExerciseType.MULTIPLA_ESCOLHA, savedQuestion.getExerciseType());
        assertEquals("[{\"id\":\"A\",\"text\":\"Paris\"},{\"id\":\"B\",\"text\":\"London\"},{\"id\":\"C\",\"text\":\"Berlin\"}]", savedQuestion.getOptions());
        assertEquals("A", savedQuestion.getCorrectAnswer());
        assertEquals(10.0, savedQuestion.getGrade());
        assertEquals(1, savedQuestion.getExamSequence());

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setString(2, "What is the capital of France?");
        verify(preparedStatement).setString(3, "MULTIPLA_ESCOLHA");
        verify(preparedStatement).setString(4, "[{\"id\":\"A\",\"text\":\"Paris\"},{\"id\":\"B\",\"text\":\"London\"},{\"id\":\"C\",\"text\":\"Berlin\"}]");
        verify(preparedStatement).setString(5, "A");
        verify(preparedStatement).setDouble(6, 10.0);
        verify(preparedStatement).setInt(7, 1);
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).getGeneratedKeys();
    }

    @Test
    void findQuestionById_shouldReturnQuestionWhenFound() throws SQLException {
        // Arrange
        int questionId = 5;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        
        when(resultSet.getInt("id_exam_question")).thenReturn(questionId);
        when(resultSet.getInt("id_exam_definition")).thenReturn(1);
        when(resultSet.getString("statement")).thenReturn("What is the capital of France?");
        when(resultSet.getString("exercise_type")).thenReturn("MULTIPLA_ESCOLHA");
        when(resultSet.getString("options")).thenReturn("[{\"id\":\"A\",\"text\":\"Paris\"},{\"id\":\"B\",\"text\":\"London\"},{\"id\":\"C\",\"text\":\"Berlin\"}]");
        when(resultSet.getString("correct_answer")).thenReturn("A");
        when(resultSet.getDouble("grade")).thenReturn(10.0);
        when(resultSet.getInt("exam_sequence")).thenReturn(1);

        // Act
        ExamQuestion foundQuestion = examQuestionDAO.findQuestionById(questionId);

        // Assert
        assertNotNull(foundQuestion);
        assertEquals(questionId, foundQuestion.getIdExamQuestion());
        assertEquals(1, foundQuestion.getIdDefinitionExam());
        assertEquals("What is the capital of France?", foundQuestion.getStatement());
        assertEquals(ExerciseType.MULTIPLA_ESCOLHA, foundQuestion.getExerciseType());
        assertEquals("[{\"id\":\"A\",\"text\":\"Paris\"},{\"id\":\"B\",\"text\":\"London\"},{\"id\":\"C\",\"text\":\"Berlin\"}]", foundQuestion.getOptions());
        assertEquals("A", foundQuestion.getCorrectAnswer());
        assertEquals(10.0, foundQuestion.getGrade());
        assertEquals(1, foundQuestion.getExamSequence());

        verify(preparedStatement).setInt(1, questionId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void findQuestionById_shouldReturnNullWhenNotFound() throws SQLException {
        // Arrange
        int questionId = 999;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // Act
        ExamQuestion foundQuestion = examQuestionDAO.findQuestionById(questionId);

        // Assert
        assertNull(foundQuestion);
        verify(preparedStatement).setInt(1, questionId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void findQuestionsByExamDefinitionId_shouldReturnListOfQuestions() throws SQLException {
        // Arrange
        int examDefinitionId = 1;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // Return true twice for two questions, then false

        // First question
        when(resultSet.getInt("id_exam_question")).thenReturn(5, 6);
        when(resultSet.getInt("id_exam_definition")).thenReturn(examDefinitionId, examDefinitionId);
        when(resultSet.getString("statement")).thenReturn(
            "What is the capital of France?", 
            "What is the capital of Germany?"
        );
        when(resultSet.getString("exercise_type")).thenReturn("MULTIPLA_ESCOLHA", "MULTIPLA_ESCOLHA");
        when(resultSet.getString("options")).thenReturn(
            "[{\"id\":\"A\",\"text\":\"Paris\"},{\"id\":\"B\",\"text\":\"London\"},{\"id\":\"C\",\"text\":\"Berlin\"}]",
            "[{\"id\":\"A\",\"text\":\"Paris\"},{\"id\":\"B\",\"text\":\"Berlin\"},{\"id\":\"C\",\"text\":\"Madrid\"}]"
        );
        when(resultSet.getString("correct_answer")).thenReturn("A", "B");
        when(resultSet.getDouble("grade")).thenReturn(10.0, 15.0);
        when(resultSet.getInt("exam_sequence")).thenReturn(1, 2);

        // Act
        List<ExamQuestion> questions = examQuestionDAO.findQuestionsByExamDefinitionId(examDefinitionId);

        // Assert
        assertNotNull(questions);
        assertEquals(2, questions.size());

        // First question
        assertEquals(5, questions.get(0).getIdExamQuestion());
        assertEquals(examDefinitionId, questions.get(0).getIdDefinitionExam());
        assertEquals("What is the capital of France?", questions.get(0).getStatement());
        assertEquals(ExerciseType.MULTIPLA_ESCOLHA, questions.get(0).getExerciseType());
        assertEquals("A", questions.get(0).getCorrectAnswer());
        assertEquals(10.0, questions.get(0).getGrade());
        assertEquals(1, questions.get(0).getExamSequence());

        // Second question
        assertEquals(6, questions.get(1).getIdExamQuestion());
        assertEquals(examDefinitionId, questions.get(1).getIdDefinitionExam());
        assertEquals("What is the capital of Germany?", questions.get(1).getStatement());
        assertEquals(ExerciseType.MULTIPLA_ESCOLHA, questions.get(1).getExerciseType());
        assertEquals("B", questions.get(1).getCorrectAnswer());
        assertEquals(15.0, questions.get(1).getGrade());
        assertEquals(2, questions.get(1).getExamSequence());

        verify(preparedStatement).setInt(1, examDefinitionId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void updateQuestion_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        ExamQuestion question = new ExamQuestion();
        question.setIdExamQuestion(5);
        question.setIdDefinitionExam(1);
        question.setStatement("Updated question statement");
        question.setExerciseType(ExerciseType.MULTIPLA_ESCOLHA);
        question.setOptions("[{\"id\":\"A\",\"text\":\"Updated option A\"},{\"id\":\"B\",\"text\":\"Updated option B\"}]");
        question.setCorrectAnswer("B");
        question.setGrade(20.0);
        question.setExamSequence(3);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = examQuestionDAO.updateQuestion(question);

        // Assert
        assertTrue(result);

        verify(preparedStatement).setString(1, "Updated question statement");
        verify(preparedStatement).setString(2, "MULTIPLA_ESCOLHA");
        verify(preparedStatement).setString(3, "[{\"id\":\"A\",\"text\":\"Updated option A\"},{\"id\":\"B\",\"text\":\"Updated option B\"}]");
        verify(preparedStatement).setString(4, "B");
        verify(preparedStatement).setDouble(5, 20.0);
        verify(preparedStatement).setInt(6, 3);
        verify(preparedStatement).setInt(7, 5);
        verify(preparedStatement).setInt(8, 1);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void deleteQuestion_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        int questionId = 5;
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = examQuestionDAO.deleteQuestion(questionId);

        // Assert
        assertTrue(result);

        verify(preparedStatement).setInt(1, questionId);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void deleteQuestionsByExamDefinitionId_shouldReturnNumberOfDeletedRows() throws SQLException {
        // Arrange
        int examDefinitionId = 1;
        int expectedDeletedRows = 3;
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(expectedDeletedRows);

        // Act
        int deletedRows = examQuestionDAO.deleteQuestionsByExamDefinitionId(examDefinitionId);

        // Assert
        assertEquals(expectedDeletedRows, deletedRows);

        verify(preparedStatement).setInt(1, examDefinitionId);
        verify(preparedStatement).executeUpdate();
    }
}