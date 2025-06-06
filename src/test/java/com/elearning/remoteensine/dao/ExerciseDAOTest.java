package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.Exercise;
import com.elearning.remoteensine.model.enums.ExerciseType;
import com.elearning.remoteensine.util.DatabaseConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExerciseDAOTest {

    @Mock
    private DatabaseConnector databaseConnector;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private ExerciseDAO exerciseDAO;

    @BeforeEach
    void setUp() throws SQLException {
        exerciseDAO = new ExerciseDAO(databaseConnector);
        when(databaseConnector.getConnection()).thenReturn(connection);
    }

    @Test
    void saveExercise_shouldSaveAndReturnExerciseWithId() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);

        Exercise exercise = new Exercise();
        exercise.setClassroomId(1);
        exercise.setStatement("What is Java?");
        exercise.setExerciseType(ExerciseType.MULTIPLA_ESCOLHA);
        exercise.setCorrectAnswer("A");

        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(10);

        // Act
        Exercise savedExercise = exerciseDAO.saveExercise(exercise);

        // Assert
        assertEquals(10, savedExercise.getExerciseId());
        assertEquals(1, savedExercise.getClassroomId());
        assertEquals("What is Java?", savedExercise.getStatement());
        assertEquals(ExerciseType.MULTIPLA_ESCOLHA, savedExercise.getExerciseType());
        assertEquals("A", savedExercise.getCorrectAnswer());

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setString(2, "What is Java?");
        verify(preparedStatement).setString(3, "MULTIPLA_ESCOLHA");
        verify(preparedStatement).setString(4, "A");
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).getGeneratedKeys();
    }

    @Test
    void searchExerciseById_shouldReturnExerciseWhenFound() throws SQLException {
        // Arrange
        int exerciseId = 10;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        
        when(resultSet.getInt("exercise_id")).thenReturn(exerciseId);
        when(resultSet.getInt("classroom_id")).thenReturn(1);
        when(resultSet.getString("statement")).thenReturn("What is Java?");
        when(resultSet.getString("exercise_type")).thenReturn("MULTIPLA_ESCOLHA");
        when(resultSet.getString("correct_answer")).thenReturn("A");

        // Act
        Exercise foundExercise = exerciseDAO.searchExerciseById(exerciseId);

        // Assert
        assertNotNull(foundExercise);
        assertEquals(exerciseId, foundExercise.getExerciseId());
        assertEquals(1, foundExercise.getClassroomId());
        assertEquals("What is Java?", foundExercise.getStatement());
        assertEquals(ExerciseType.MULTIPLA_ESCOLHA, foundExercise.getExerciseType());
        assertEquals("A", foundExercise.getCorrectAnswer());

        verify(preparedStatement).setInt(1, exerciseId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void listExercisesByClass_shouldReturnListOfExercises() throws SQLException {
        // Arrange
        int classroomId = 1;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // Return true twice for two exercises, then false

        // First exercise
        when(resultSet.getInt("exercise_id")).thenReturn(10, 11);
        when(resultSet.getInt("classroom_id")).thenReturn(classroomId, classroomId);
        when(resultSet.getString("statement")).thenReturn("What is Java?", "What is Python?");
        when(resultSet.getString("exercise_type")).thenReturn("MULTIPLA_ESCOLHA", "DISSERTATIVA");
        when(resultSet.getString("correct_answer")).thenReturn("A", "Python is a programming language");

        // Act
        List<Exercise> exercises = exerciseDAO.listExercisesByClass(classroomId);

        // Assert
        assertNotNull(exercises);
        assertEquals(2, exercises.size());

        // First exercise
        assertEquals(10, exercises.get(0).getExerciseId());
        assertEquals(classroomId, exercises.get(0).getClassroomId());
        assertEquals("What is Java?", exercises.get(0).getStatement());
        assertEquals(ExerciseType.MULTIPLA_ESCOLHA, exercises.get(0).getExerciseType());
        assertEquals("A", exercises.get(0).getCorrectAnswer());

        // Second exercise
        assertEquals(11, exercises.get(1).getExerciseId());
        assertEquals(classroomId, exercises.get(1).getClassroomId());
        assertEquals("What is Python?", exercises.get(1).getStatement());
        assertEquals(ExerciseType.DISSERTATIVA, exercises.get(1).getExerciseType());
        assertEquals("Python is a programming language", exercises.get(1).getCorrectAnswer());

        verify(preparedStatement).setInt(1, classroomId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void updateExercise_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        Exercise exercise = new Exercise();
        exercise.setExerciseId(10);
        exercise.setClassroomId(1);
        exercise.setStatement("Updated Java question");
        exercise.setExerciseType(ExerciseType.MULTIPLA_ESCOLHA);
        exercise.setCorrectAnswer("B");

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = exerciseDAO.updateExercise(exercise);

        // Assert
        assertTrue(result);

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setString(2, "Updated Java question");
        verify(preparedStatement).setString(3, "MULTIPLA_ESCOLHA");
        verify(preparedStatement).setString(4, "B");
        verify(preparedStatement).setInt(5, 10);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void deleteExercise_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        int exerciseId = 10;
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = exerciseDAO.deleteExercise(exerciseId);

        // Assert
        assertTrue(result);

        verify(preparedStatement).setInt(1, exerciseId);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void deleteExerciseByClass_shouldReturnNumberOfDeletedExercises() throws SQLException {
        // Arrange
        int classroomId = 1;
        int expectedDeletedCount = 3;
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(expectedDeletedCount);

        // Act
        int deletedCount = exerciseDAO.deleteExerciseByClass(classroomId);

        // Assert
        assertEquals(expectedDeletedCount, deletedCount);

        verify(preparedStatement).setInt(1, classroomId);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void saveExercise_shouldThrowExceptionWhenClassroomIdIsInvalid() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);

        Exercise exercise = new Exercise();
        exercise.setClassroomId(0); // Invalid classroom ID
        exercise.setStatement("What is Java?");
        exercise.setExerciseType(ExerciseType.MULTIPLA_ESCOLHA);
        exercise.setCorrectAnswer("A");

        // Act & Assert
        SQLException exception = assertThrows(SQLException.class, () -> {
            exerciseDAO.saveExercise(exercise);
        });
        
        assertTrue(exception.getMessage().contains("Invalid or not defined class ID"));
        
        // Verify that no update was executed
        verify(preparedStatement, never()).executeUpdate();
    }
}