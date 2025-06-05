package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.Enroll;
import com.elearning.remoteensine.model.Professor;
import com.elearning.remoteensine.model.Student;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnrollDAOTest {

    @Mock
    private DatabaseConnector databaseConnector;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private EnrollDAO enrollDAO;

    @BeforeEach
    void setUp() throws SQLException {
        enrollDAO = new EnrollDAO(databaseConnector);
        when(databaseConnector.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
    }

    @Test
    void checkEnroll_shouldReturnTrueWhenEnrollmentExists() throws SQLException {
        // Arrange
        int studentId = 1;
        int courseId = 10;
        
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1); // Count > 0 means enrollment exists
        
        // Act
        boolean result = enrollDAO.checkEnroll(studentId, courseId);
        
        // Assert
        assertTrue(result);
        
        // Verify
        verify(preparedStatement).setInt(1, studentId);
        verify(preparedStatement).setInt(2, courseId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void checkEnroll_shouldReturnFalseWhenEnrollmentDoesNotExist() throws SQLException {
        // Arrange
        int studentId = 1;
        int courseId = 10;
        
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(0); // Count = 0 means enrollment doesn't exist
        
        // Act
        boolean result = enrollDAO.checkEnroll(studentId, courseId);
        
        // Assert
        assertFalse(result);
        
        // Verify
        verify(preparedStatement).setInt(1, studentId);
        verify(preparedStatement).setInt(2, courseId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void enrollmentStudent_shouldReturnFalseWhenAlreadyEnrolled() throws SQLException {
        // Arrange
        int studentId = 1;
        int courseId = 10;
        
        // Mock checkEnroll to return true (student already enrolled)
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(1); // Count > 0 means enrollment exists
        
        // Act
        boolean result = enrollDAO.enrollmentStudent(studentId, courseId);
        
        // Assert
        assertFalse(result);
        
        // Verify checkEnroll was called
        verify(preparedStatement).setInt(1, studentId);
        verify(preparedStatement).setInt(2, courseId);
        verify(preparedStatement).executeQuery();
        
        // Verify insert was NOT called
        verify(preparedStatement, never()).executeUpdate();
    }

    @Test
    void listCoursesByStudents_shouldReturnListOfEnrollments() throws SQLException {
        // Arrange
        int studentId = 1;
        LocalDateTime enrollDate = LocalDateTime.now();
        
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false); // One enrollment, then no more
        
        // Mock result set data
        when(resultSet.getInt("student_id")).thenReturn(studentId);
        when(resultSet.getInt("course_id")).thenReturn(10);
        when(resultSet.getTimestamp("enrollment_date")).thenReturn(Timestamp.valueOf(enrollDate));
        when(resultSet.getDouble("progress")).thenReturn(0.5);
        when(resultSet.getString("course_title")).thenReturn("Java Programming");
        when(resultSet.getString("description_course")).thenReturn("Learn Java");
        when(resultSet.getDouble("price_course")).thenReturn(99.99);
        when(resultSet.getString("category_course")).thenReturn("Programming");
        when(resultSet.getInt("load_hour_course")).thenReturn(40);
        when(resultSet.getInt("professor_id_course")).thenReturn(5);
        
        // Act
        List<Enroll> enrollments = enrollDAO.listCoursesByStudents(studentId);
        
        // Assert
        assertNotNull(enrollments);
        assertEquals(1, enrollments.size());
        
        Enroll enroll = enrollments.get(0);
        assertEquals(studentId, enroll.getStudent().getIdUser());
        assertEquals(10, enroll.getCourse().getIdCourse());
        assertEquals("Java Programming", enroll.getCourse().getTitle());
        assertEquals("Learn Java", enroll.getCourse().getDescription());
        assertEquals(99.99, enroll.getCourse().getPrice());
        assertEquals("Programming", enroll.getCourse().getCategory());
        assertEquals(40, enroll.getCourse().getHoursLoad());
        assertEquals(5, enroll.getCourse().getResponsibleProfessor().getIdUser());
        assertEquals(0.5, enroll.getProgress());
        assertEquals(enrollDate, enroll.getEnrollDate());
        
        // Verify
        verify(preparedStatement).setInt(1, studentId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void lisStudentByCourse_shouldReturnListOfEnrollments() throws SQLException {
        // Arrange
        int courseId = 10;
        LocalDateTime enrollDate = LocalDateTime.now();
        
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false); // One enrollment, then no more
        
        // Mock result set data
        when(resultSet.getInt("student_id")).thenReturn(1);
        when(resultSet.getInt("course_id")).thenReturn(courseId);
        when(resultSet.getTimestamp("enrollment_date")).thenReturn(Timestamp.valueOf(enrollDate));
        when(resultSet.getDouble("progress")).thenReturn(0.5);
        when(resultSet.getString("student_name")).thenReturn("John Doe");
        when(resultSet.getString("student_email")).thenReturn("john.doe@example.com");
        
        // Act
        List<Enroll> enrollments = enrollDAO.lisStudentByCourse(courseId);
        
        // Assert
        assertNotNull(enrollments);
        assertEquals(1, enrollments.size());
        
        Enroll enroll = enrollments.get(0);
        assertEquals(1, enroll.getStudent().getIdUser());
        assertEquals("John Doe", enroll.getStudent().getName());
        assertEquals("john.doe@example.com", enroll.getStudent().getEmail());
        assertEquals(courseId, enroll.getCourse().getIdCourse());
        assertEquals(0.5, enroll.getProgress());
        assertEquals(enrollDate, enroll.getEnrollDate());
        
        // Verify
        verify(preparedStatement).setInt(1, courseId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void updateProgress_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        int studentId = 1;
        int courseId = 10;
        double progress = 0.75;
        
        when(preparedStatement.executeUpdate()).thenReturn(1);
        
        // Act
        boolean result = enrollDAO.updateProgress(studentId, courseId, progress);
        
        // Assert
        assertTrue(result);
        
        // Verify
        verify(preparedStatement).setDouble(1, progress);
        verify(preparedStatement).setInt(2, studentId);
        verify(preparedStatement).setInt(3, courseId);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void cancelEnroll_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        int studentId = 1;
        int courseId = 10;
        
        when(preparedStatement.executeUpdate()).thenReturn(1);
        
        // Act
        boolean result = enrollDAO.cancelEnroll(studentId, courseId);
        
        // Assert
        assertTrue(result);
        
        // Verify
        verify(preparedStatement).setInt(1, studentId);
        verify(preparedStatement).setInt(2, courseId);
        verify(preparedStatement).executeUpdate();
    }
}