package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.StudentsExerciseAnswer;
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
public class StudentsExerciseAnswerDAOTest {

    @Mock
    private DatabaseConnector databaseConnector;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private StudentsExerciseAnswerDAO studentsExerciseAnswerDAO;

    @BeforeEach
    void setUp() throws SQLException {
        studentsExerciseAnswerDAO = new StudentsExerciseAnswerDAO(databaseConnector);
        when(databaseConnector.getConnection()).thenReturn(connection);
    }

    @Test
    void saveAnswer_shouldSaveAndReturnAnswerWithId() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);

        StudentsExerciseAnswer answer = new StudentsExerciseAnswer();
        answer.setIdExercise(1);
        answer.setIdStudent(2);
        answer.setIdClass(3);
        answer.setIdCourse(4);
        answer.setAnswerText("This is my answer");
        LocalDateTime now = LocalDateTime.now();
        answer.setSendDate(now);
        answer.setCorrect(true);
        answer.setGrade(9.5);
        answer.setFeedbackProfessor("Good job!");

        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(10);

        // Act
        StudentsExerciseAnswer savedAnswer = studentsExerciseAnswerDAO.saveAnswer(answer);

        // Assert
        assertEquals(10, savedAnswer.getIdStudentAnswerExercise());
        assertEquals(1, savedAnswer.getIdExercise());
        assertEquals(2, savedAnswer.getIdStudent());
        assertEquals(3, savedAnswer.getIdClass());
        assertEquals(4, savedAnswer.getIdCourse());
        assertEquals("This is my answer", savedAnswer.getAnswerText());
        assertEquals(now, savedAnswer.getSendDate());
        assertEquals(true, savedAnswer.getCorrect());
        assertEquals(9.5, savedAnswer.getGrade());
        assertEquals("Good job!", savedAnswer.getFeedbackProfessor());

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
        verify(preparedStatement).setInt(3, 3);
        verify(preparedStatement).setInt(4, 4);
        verify(preparedStatement).setString(5, "This is my answer");
        verify(preparedStatement).setTimestamp(eq(6), any(Timestamp.class));
        verify(preparedStatement).setBoolean(7, true);
        verify(preparedStatement).setDouble(8, 9.5);
        verify(preparedStatement).setString(9, "Good job!");
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).getGeneratedKeys();
    }

    @Test
    void getAnswerByStudentAndExercise_shouldReturnAnswerWhenFound() throws SQLException {
        // Arrange
        int studentId = 2;
        int exerciseId = 1;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        
        when(resultSet.getInt("id_student_answer_exercise")).thenReturn(10);
        when(resultSet.getInt("id_exercise")).thenReturn(exerciseId);
        when(resultSet.getInt("id_student")).thenReturn(studentId);
        when(resultSet.getInt("id_class")).thenReturn(3);
        when(resultSet.getInt("id_course")).thenReturn(4);
        when(resultSet.getString("answer_text")).thenReturn("This is my answer");
        when(resultSet.getTimestamp("send_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getObject("is_correct")).thenReturn(true);
        when(resultSet.getObject("grade")).thenReturn(9.5);
        when(resultSet.getString("feedback_professor")).thenReturn("Good job!");

        // Act
        StudentsExerciseAnswer foundAnswer = studentsExerciseAnswerDAO.getAnswerByStudentAndExercise(studentId, exerciseId);

        // Assert
        assertNotNull(foundAnswer);
        assertEquals(10, foundAnswer.getIdStudentAnswerExercise());
        assertEquals(exerciseId, foundAnswer.getIdExercise());
        assertEquals(studentId, foundAnswer.getIdStudent());
        assertEquals(3, foundAnswer.getIdClass());
        assertEquals(4, foundAnswer.getIdCourse());
        assertEquals("This is my answer", foundAnswer.getAnswerText());
        assertNotNull(foundAnswer.getSendDate());
        assertEquals("Good job!", foundAnswer.getFeedbackProfessor());

        verify(preparedStatement).setInt(1, studentId);
        verify(preparedStatement).setInt(2, exerciseId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void getAnswerByStudentAndExercise_shouldReturnNullWhenNotFound() throws SQLException {
        // Arrange
        int studentId = 2;
        int exerciseId = 1;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false); // No result found

        // Act
        StudentsExerciseAnswer foundAnswer = studentsExerciseAnswerDAO.getAnswerByStudentAndExercise(studentId, exerciseId);

        // Assert
        assertNull(foundAnswer);

        verify(preparedStatement).setInt(1, studentId);
        verify(preparedStatement).setInt(2, exerciseId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void getAnswersByStudentAndClassroom_shouldReturnListOfAnswers() throws SQLException {
        // Arrange
        int studentId = 2;
        int classroomId = 3;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // Return true twice for two answers, then false

        // First answer
        when(resultSet.getInt("id_student_answer_exercise")).thenReturn(10, 11);
        when(resultSet.getInt("id_exercise")).thenReturn(1, 2);
        when(resultSet.getInt("id_student")).thenReturn(studentId, studentId);
        when(resultSet.getInt("id_class")).thenReturn(classroomId, classroomId);
        when(resultSet.getInt("id_course")).thenReturn(4, 4);
        when(resultSet.getString("answer_text")).thenReturn("Answer 1", "Answer 2");
        when(resultSet.getTimestamp("send_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getObject("is_correct")).thenReturn(true, false);
        when(resultSet.getObject("grade")).thenReturn(9.5, 7.0);
        when(resultSet.getString("feedback_professor")).thenReturn("Good job!", "Needs improvement");

        // Act
        List<StudentsExerciseAnswer> answers = studentsExerciseAnswerDAO.getAnswersByStudentAndClassroom(studentId, classroomId);

        // Assert
        assertNotNull(answers);
        assertEquals(2, answers.size());

        // First answer
        assertEquals(10, answers.get(0).getIdStudentAnswerExercise());
        assertEquals(1, answers.get(0).getIdExercise());
        assertEquals(studentId, answers.get(0).getIdStudent());
        assertEquals(classroomId, answers.get(0).getIdClass());
        assertEquals(4, answers.get(0).getIdCourse());
        assertEquals("Answer 1", answers.get(0).getAnswerText());
        assertNotNull(answers.get(0).getSendDate());
        assertEquals("Good job!", answers.get(0).getFeedbackProfessor());

        // Second answer
        assertEquals(11, answers.get(1).getIdStudentAnswerExercise());
        assertEquals(2, answers.get(1).getIdExercise());
        assertEquals(studentId, answers.get(1).getIdStudent());
        assertEquals(classroomId, answers.get(1).getIdClass());
        assertEquals(4, answers.get(1).getIdCourse());
        assertEquals("Answer 2", answers.get(1).getAnswerText());
        assertNotNull(answers.get(1).getSendDate());
        assertEquals("Needs improvement", answers.get(1).getFeedbackProfessor());

        verify(preparedStatement).setInt(1, studentId);
        verify(preparedStatement).setInt(2, classroomId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void updateAnswer_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        StudentsExerciseAnswer answer = new StudentsExerciseAnswer();
        answer.setIdStudentAnswerExercise(10);
        answer.setAnswerText("Updated answer");
        LocalDateTime now = LocalDateTime.now();
        answer.setSendDate(now);
        answer.setCorrect(true);
        answer.setGrade(9.8);
        answer.setFeedbackProfessor("Excellent work!");

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = studentsExerciseAnswerDAO.updateAnswer(answer);

        // Assert
        assertTrue(result);

        verify(preparedStatement).setString(1, "Updated answer");
        verify(preparedStatement).setTimestamp(eq(2), any(Timestamp.class));
        verify(preparedStatement).setBoolean(3, true);
        verify(preparedStatement).setDouble(4, 9.8);
        verify(preparedStatement).setString(5, "Excellent work!");
        verify(preparedStatement).setInt(6, 10);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void updateAnswer_shouldReturnFalseWhenNoRowsAffected() throws SQLException {
        // Arrange
        StudentsExerciseAnswer answer = new StudentsExerciseAnswer();
        answer.setIdStudentAnswerExercise(999); // Non-existent ID
        answer.setAnswerText("Updated answer");
        answer.setSendDate(LocalDateTime.now());
        answer.setCorrect(true);
        answer.setGrade(9.8);
        answer.setFeedbackProfessor("Excellent work!");

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(0); // No rows affected

        // Act
        boolean result = studentsExerciseAnswerDAO.updateAnswer(answer);

        // Assert
        assertFalse(result);

        verify(preparedStatement).setString(1, "Updated answer");
        verify(preparedStatement).setTimestamp(eq(2), any(Timestamp.class));
        verify(preparedStatement).setBoolean(3, true);
        verify(preparedStatement).setDouble(4, 9.8);
        verify(preparedStatement).setString(5, "Excellent work!");
        verify(preparedStatement).setInt(6, 999);
        verify(preparedStatement).executeUpdate();
    }
}