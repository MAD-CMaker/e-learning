package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.CourseDAO;
import com.elearning.remoteensine.dao.EnrollDAO;
import com.elearning.remoteensine.dao.UserDAO;
import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.Enroll;
import com.elearning.remoteensine.model.Student;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnrollServiceTest {

    @Mock
    private EnrollDAO enrollDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private CourseDAO courseDAO;

    private EnrollService enrollService;
    private User studentUser;
    private User nonStudentUser;
    private Course course;
    private List<Enroll> enrollments;
    private Student student;

    @BeforeEach
    void setUp() {
        enrollService = new EnrollService(enrollDAO, userDAO, courseDAO);
        
        // Setup student user
        studentUser = new User("John Student", "john@example.com", "password", UserType.STUDENT);
        studentUser.setIdUser(1);
        
        // Setup student
        student = new Student("John Student", "john@example.com", "password");
        student.setIdUser(1);
        
        // Setup non-student user
        nonStudentUser = new User("Jane Professor", "jane@example.com", "password", UserType.PROFESSOR);
        nonStudentUser.setIdUser(2);
        
        // Setup course
        course = new Course();
        course.setIdCourse(1);
        course.setTitle("Java Programming");
        
        // Setup enrollments
        enrollments = new ArrayList<>();
        Enroll enroll1 = new Enroll();
        enroll1.setStudent(student);
        enroll1.setCourse(course);
        enroll1.setProgress(0.5);
        enroll1.setEnrollDate(LocalDateTime.now());
        enrollments.add(enroll1);
    }

    @Test
    void makeEnroll_shouldSuccessfullyEnrollStudentInCourse() throws SQLException {
        // Arrange
        int studentId = 1;
        int courseId = 1;
        
        when(userDAO.searchById(studentId)).thenReturn(studentUser);
        when(courseDAO.searchCourseById(courseId)).thenReturn(course);
        when(enrollDAO.checkEnroll(studentId, courseId)).thenReturn(false);
        when(enrollDAO.enrollmentStudent(studentId, courseId)).thenReturn(true);
        
        // Act
        boolean result = enrollService.makeEnroll(studentId, courseId);
        
        // Assert
        assertTrue(result);
        verify(userDAO).searchById(studentId);
        verify(courseDAO).searchCourseById(courseId);
        verify(enrollDAO).checkEnroll(studentId, courseId);
        verify(enrollDAO).enrollmentStudent(studentId, courseId);
    }

    @Test
    void enrollmentCoursesCheck_shouldListEnrolledCoursesForStudent() throws SQLException {
        // Arrange
        int studentId = 1;
        
        when(userDAO.searchById(studentId)).thenReturn(studentUser);
        when(enrollDAO.listCoursesByStudents(studentId)).thenReturn(enrollments);
        
        // Act
        List<Enroll> result = enrollService.enrollmentCoursesCheck(studentId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getStudent().getIdUser());
        assertEquals(1, result.get(0).getCourse().getIdCourse());
        assertEquals(0.5, result.get(0).getProgress());
        
        verify(userDAO).searchById(studentId);
        verify(enrollDAO).listCoursesByStudents(studentId);
    }

    @Test
    void enrolledStudentsCourses_shouldListStudentsEnrolledInCourse() throws SQLException {
        // Arrange
        int courseId = 1;
        
        when(courseDAO.searchCourseById(courseId)).thenReturn(course);
        when(enrollDAO.lisStudentByCourse(courseId)).thenReturn(enrollments);
        
        // Act
        List<Enroll> result = enrollService.enrolledStudentsCourses(courseId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getStudent().getIdUser());
        assertEquals(1, result.get(0).getCourse().getIdCourse());
        
        verify(courseDAO).searchCourseById(courseId);
        verify(enrollDAO).lisStudentByCourse(courseId);
    }

    @Test
    void updateStudentProgress_shouldUpdateProgressSuccessfully() throws SQLException {
        // Arrange
        int studentId = 1;
        int courseId = 1;
        double newProgress = 0.75;
        
        when(enrollDAO.checkEnroll(studentId, courseId)).thenReturn(true);
        when(enrollDAO.updateProgress(studentId, courseId, newProgress)).thenReturn(true);
        
        // Act
        boolean result = enrollService.updateStudentProgress(studentId, courseId, newProgress);
        
        // Assert
        assertTrue(result);
        verify(enrollDAO).checkEnroll(studentId, courseId);
        verify(enrollDAO).updateProgress(studentId, courseId, newProgress);
    }

    @Test
    void cancelEnrollStudent_shouldCancelEnrollmentSuccessfully() throws SQLException {
        // Arrange
        int studentId = 1;
        int courseId = 1;
        
        when(enrollDAO.cancelEnroll(studentId, courseId)).thenReturn(true);
        
        // Act
        boolean result = enrollService.cancelEnrollStudent(studentId, courseId);
        
        // Assert
        assertTrue(result);
        verify(enrollDAO, times(2)).cancelEnroll(studentId, courseId);
    }

    @Test
    void makeEnroll_shouldThrowExceptionForNonExistentStudent() throws SQLException {
        // Arrange
        int nonExistentStudentId = 999;
        int courseId = 1;
        
        when(userDAO.searchById(nonExistentStudentId)).thenReturn(null);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            enrollService.makeEnroll(nonExistentStudentId, courseId);
        });
        
        assertEquals("Student ID " + nonExistentStudentId + " not found.", exception.getMessage());
        verify(userDAO).searchById(nonExistentStudentId);
        verifyNoInteractions(courseDAO);
        verifyNoInteractions(enrollDAO);
    }

    @Test
    void makeEnroll_shouldThrowExceptionForNonExistentCourse() throws SQLException {
        // Arrange
        int studentId = 1;
        int nonExistentCourseId = 999;
        
        when(userDAO.searchById(studentId)).thenReturn(studentUser);
        when(courseDAO.searchCourseById(nonExistentCourseId)).thenReturn(null);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            enrollService.makeEnroll(studentId, nonExistentCourseId);
        });
        
        assertEquals("Course com ID " + nonExistentCourseId + " not found.", exception.getMessage());
        verify(userDAO).searchById(studentId);
        verify(courseDAO).searchCourseById(nonExistentCourseId);
        verifyNoInteractions(enrollDAO);
    }

    @Test
    void makeEnroll_shouldThrowExceptionForAlreadyEnrolledStudent() throws SQLException {
        // Arrange
        int studentId = 1;
        int courseId = 1;
        
        when(userDAO.searchById(studentId)).thenReturn(studentUser);
        when(courseDAO.searchCourseById(courseId)).thenReturn(course);
        when(enrollDAO.checkEnroll(studentId, courseId)).thenReturn(true);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            enrollService.makeEnroll(studentId, courseId);
        });
        
        assertEquals("Student already enrolled in this course.", exception.getMessage());
        verify(userDAO).searchById(studentId);
        verify(courseDAO).searchCourseById(courseId);
        verify(enrollDAO).checkEnroll(studentId, courseId);
        verify(enrollDAO, never()).enrollmentStudent(anyInt(), anyInt());
    }

    @Test
    void makeEnroll_shouldThrowExceptionForNonStudentUser() throws SQLException {
        // Arrange
        int nonStudentId = 2;
        int courseId = 1;
        
        when(userDAO.searchById(nonStudentId)).thenReturn(nonStudentUser);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            enrollService.makeEnroll(nonStudentId, courseId);
        });
        
        assertEquals("Student ID " + nonStudentId + " not found.", exception.getMessage());
        verify(userDAO).searchById(nonStudentId);
        verifyNoInteractions(courseDAO);
        verifyNoInteractions(enrollDAO);
    }
}