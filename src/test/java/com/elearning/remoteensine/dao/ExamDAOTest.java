package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.Exam;
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
public class ExamDAOTest {

    @Mock
    private DatabaseConnector databaseConnector;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private ExamDAO examDAO;

    @BeforeEach
    void setUp() throws SQLException {
        examDAO = new ExamDAO(databaseConnector);
        when(databaseConnector.getConnection()).thenReturn(connection);
    }

    @Test
    void saveExam_shouldSaveAndReturnExamWithId() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);

        Exam exam = new Exam();
        exam.setCourseId(1);
        exam.setIdExamDefinition(2);
        exam.setStudentId(3);
        exam.setGrade(85);
        exam.setComment("Great exam!");
        LocalDateTime examDate = LocalDateTime.now();
        exam.setHourDate(examDate);
        exam.setSubmitted(true);

        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(10);

        // Act
        Exam savedExam = examDAO.saveExam(exam);

        // Assert
        assertEquals(10, savedExam.getExamId());
        assertEquals(1, savedExam.getCourseId());
        assertEquals(2, savedExam.getIdExamDefinition());
        assertEquals(3, savedExam.getStudentId());
        assertEquals(85, savedExam.getGrade());
        assertEquals("Great exam!", savedExam.getComment());
        assertEquals(examDate, savedExam.getHourDate());
        assertTrue(savedExam.isSubmitted());

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
        verify(preparedStatement).setInt(3, 3);
        verify(preparedStatement).setInt(4, 85);
        verify(preparedStatement).setString(5, "Great exam!");
        verify(preparedStatement).setTimestamp(eq(6), any(Timestamp.class));
        verify(preparedStatement).setBoolean(7, true);
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).getGeneratedKeys();
    }

    @Test
    void searchExamById_shouldReturnExamWhenFound() throws SQLException {
        // Arrange
        int examId = 10;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        
        when(resultSet.getInt("exam_id")).thenReturn(examId);
        when(resultSet.getInt("course_id")).thenReturn(1);
        when(resultSet.getInt("student_id")).thenReturn(3);
        when(resultSet.getInt("grade")).thenReturn(85);
        when(resultSet.getString("comment")).thenReturn("Great exam!");
        when(resultSet.getTimestamp("hour_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));

        // Act
        Exam foundExam = examDAO.searchExamById(examId);

        // Assert
        assertNotNull(foundExam);
        assertEquals(examId, foundExam.getExamId());
        assertEquals(1, foundExam.getCourseId());
        assertEquals(3, foundExam.getStudentId());
        assertEquals(85, foundExam.getGrade());
        assertEquals("Great exam!", foundExam.getComment());
        assertNotNull(foundExam.getHourDate());

        verify(preparedStatement).setInt(1, examId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void searchExamById_shouldReturnNullWhenNotFound() throws SQLException {
        // Arrange
        int examId = 999;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // Act
        Exam foundExam = examDAO.searchExamById(examId);

        // Assert
        assertNull(foundExam);
        verify(preparedStatement).setInt(1, examId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void listCoursesExams_shouldReturnListOfExams() throws SQLException {
        // Arrange
        int courseId = 1;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // Return true twice for two exams, then false

        // First exam
        when(resultSet.getInt("exam_id")).thenReturn(10, 11);
        when(resultSet.getInt("course_id")).thenReturn(courseId, courseId);
        when(resultSet.getInt("student_id")).thenReturn(3, 4);
        when(resultSet.getInt("grade")).thenReturn(85, 90);
        when(resultSet.getString("comment")).thenReturn("Great exam!", "Excellent work");
        when(resultSet.getTimestamp("hour_date")).thenReturn(
            Timestamp.valueOf(LocalDateTime.now().minusDays(1)),
            Timestamp.valueOf(LocalDateTime.now())
        );

        // Act
        List<Exam> exams = examDAO.listCoursesExams(courseId);

        // Assert
        assertNotNull(exams);
        assertEquals(2, exams.size());

        // First exam
        assertEquals(10, exams.get(0).getExamId());
        assertEquals(courseId, exams.get(0).getCourseId());
        assertEquals(3, exams.get(0).getStudentId());
        assertEquals(85, exams.get(0).getGrade());
        assertEquals("Great exam!", exams.get(0).getComment());

        // Second exam
        assertEquals(11, exams.get(1).getExamId());
        assertEquals(courseId, exams.get(1).getCourseId());
        assertEquals(4, exams.get(1).getStudentId());
        assertEquals(90, exams.get(1).getGrade());
        assertEquals("Excellent work", exams.get(1).getComment());

        verify(preparedStatement).setInt(1, courseId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void mediaCoursesGrade_shouldReturnAverageGrade() throws SQLException {
        // Arrange
        int courseId = 1;
        double expectedAverage = 87.5;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getDouble(1)).thenReturn(expectedAverage);

        // Act
        double actualAverage = examDAO.mediaCoursesGrade(courseId);

        // Assert
        assertEquals(expectedAverage, actualAverage);
        verify(preparedStatement).setInt(1, courseId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void updateExam_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        Exam exam = new Exam();
        exam.setExamId(10);
        exam.setGrade(95);
        exam.setComment("Updated comment");

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = examDAO.updateExam(exam);

        // Assert
        assertTrue(result);

        verify(preparedStatement).setInt(1, 95);
        verify(preparedStatement).setString(2, "Updated comment");
        verify(preparedStatement).setTimestamp(eq(3), any(Timestamp.class));
        verify(preparedStatement).setInt(4, 10);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void deleteExam_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        int examId = 10;
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = examDAO.deleteExam(examId);

        // Assert
        assertTrue(result);

        verify(preparedStatement).setInt(1, examId);
        verify(preparedStatement).executeUpdate();
    }
}