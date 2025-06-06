package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.CourseDAO;
import com.elearning.remoteensine.dao.ExamDefinitionDAO;
import com.elearning.remoteensine.dao.UserDAO;
import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.ExamDefinition;
import com.elearning.remoteensine.model.GradeStudent;
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
public class ExamDefinitionServiceTest {

    @Mock
    private ExamDefinitionDAO examDefinitionDAO;

    @Mock
    private CourseDAO courseDAO;

    @Mock
    private UserDAO userDAO;

    private ExamDefinitionService examDefinitionService;
    private Professor professor;
    private User regularUser;
    private Course course;
    private ExamDefinition examDefinition;

    @BeforeEach
    void setUp() {
        examDefinitionService = new ExamDefinitionService(examDefinitionDAO, courseDAO, userDAO);
        
        // Setup professor
        professor = new Professor("John Doe", "john@example.com", "password", "Computer Science");
        professor.setIdUser(1);
        
        // Setup regular user (non-professor)
        regularUser = new User("Jane Smith", "jane@example.com", "password", UserType.STUDENT);
        regularUser.setIdUser(2);
        
        // Setup course
        course = new Course("Java Programming", "Learn Java", professor, 99.99, "Programming", 40);
        course.setIdCourse(1);
        
        // Setup exam definition
        examDefinition = new ExamDefinition(1, "Java Midterm Exam", "Test your Java knowledge");
        examDefinition.setIdDefinitionExam(1);
    }

    @Test
    void createExamDefinition_withValidData_shouldCreateExamDefinition() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int idCourse = 1;
        int idProfessorLogado = 1;
        String title = "Java Midterm Exam";
        String description = "Test your Java knowledge";
        
        when(courseDAO.searchCourseById(idCourse)).thenReturn(course);
        when(userDAO.searchById(idProfessorLogado)).thenReturn(professor);
        when(examDefinitionDAO.saveExamDefinition(any(ExamDefinition.class))).thenReturn(examDefinition);
        
        // Act
        ExamDefinition result = examDefinitionService.createExamDefinition(idCourse, idProfessorLogado, title, description);
        
        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        assertEquals(idCourse, result.getIdCourse());
        
        verify(courseDAO).searchCourseById(idCourse);
        verify(userDAO).searchById(idProfessorLogado);
        verify(examDefinitionDAO).saveExamDefinition(any(ExamDefinition.class));
    }

    @Test
    void getExamDefinitionById_shouldReturnExamDefinition() throws SQLException {
        // Arrange
        int idExamDefinition = 1;
        
        when(examDefinitionDAO.findExamDefinitionById(idExamDefinition)).thenReturn(examDefinition);
        
        // Act
        ExamDefinition result = examDefinitionService.getExamDefinitionById(idExamDefinition);
        
        // Assert
        assertNotNull(result);
        assertEquals(idExamDefinition, result.getIdDefinitionExam());
        assertEquals("Java Midterm Exam", result.getTitle());
        assertEquals("Test your Java knowledge", result.getDescription());
        
        verify(examDefinitionDAO).findExamDefinitionById(idExamDefinition);
    }

    @Test
    void getExamDefinitionsForCourse_shouldReturnExamDefinitions() throws SQLException, IllegalAccessException, IllegalArgumentException {
        // Arrange
        int idCourse = 1;
        int idProfessorLogado = 1;
        List<ExamDefinition> examDefinitions = new ArrayList<>();
        examDefinitions.add(examDefinition);
        
        when(courseDAO.searchCourseById(idCourse)).thenReturn(course);
        when(examDefinitionDAO.findExamDefinitionsByCourseId(idCourse, true)).thenReturn(examDefinitions);
        
        // Act
        List<ExamDefinition> result = examDefinitionService.getExamDefinitionsForCourse(idCourse, idProfessorLogado);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Java Midterm Exam", result.get(0).getTitle());
        
        verify(courseDAO).searchCourseById(idCourse);
        verify(examDefinitionDAO).findExamDefinitionsByCourseId(idCourse, true);
    }

    @Test
    void updateExamDefinitionDetails_shouldUpdateExamDefinition() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int idExamDefinition = 1;
        int idCourse = 1;
        int idProfessorLogado = 1;
        String newTitle = "Updated Java Exam";
        String newDescription = "Updated exam description";
        
        when(examDefinitionDAO.findExamDefinitionById(idExamDefinition)).thenReturn(examDefinition);
        when(courseDAO.searchCourseById(idCourse)).thenReturn(course);
        when(examDefinitionDAO.updateExamDefinition(any(ExamDefinition.class))).thenReturn(true);
        
        // Act
        boolean result = examDefinitionService.updateExamDefinitionDetails(idExamDefinition, idCourse, idProfessorLogado, newTitle, newDescription);
        
        // Assert
        assertTrue(result);
        assertEquals(newTitle, examDefinition.getTitle());
        assertEquals(newDescription, examDefinition.getDescription());
        assertNotNull(examDefinition.getUpdateData());
        
        verify(examDefinitionDAO).findExamDefinitionById(idExamDefinition);
        verify(courseDAO).searchCourseById(idCourse);
        verify(examDefinitionDAO).updateExamDefinition(any(ExamDefinition.class));
    }

    @Test
    void deleteExamDefinition_shouldDeleteExamDefinition() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int idExamDefinition = 1;
        int idCourse = 1;
        int idProfessorLogado = 1;
        
        when(examDefinitionDAO.findExamDefinitionById(idExamDefinition)).thenReturn(examDefinition);
        when(courseDAO.searchCourseById(idCourse)).thenReturn(course);
        when(examDefinitionDAO.deleteExamDefinition(idExamDefinition)).thenReturn(true);
        
        // Act
        boolean result = examDefinitionService.deleteExamDefinition(idExamDefinition, idCourse, idProfessorLogado);
        
        // Assert
        assertTrue(result);
        
        verify(examDefinitionDAO).findExamDefinitionById(idExamDefinition);
        verify(courseDAO).searchCourseById(idCourse);
        verify(examDefinitionDAO).deleteExamDefinition(idExamDefinition);
    }

    @Test
    void publishExamDefinition_shouldPublishExamDefinition() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int idExamDefinition = 1;
        int idProfessorLogado = 1;
        
        examDefinition.setPublished(false);
        
        when(examDefinitionDAO.findExamDefinitionById(idExamDefinition)).thenReturn(examDefinition);
        when(courseDAO.searchCourseById(examDefinition.getIdCourse())).thenReturn(course);
        when(examDefinitionDAO.updateExamDefinition(any(ExamDefinition.class))).thenReturn(true);
        
        // Act
        boolean result = examDefinitionService.publishExamDefinition(idExamDefinition, idProfessorLogado);
        
        // Assert
        assertTrue(result);
        assertTrue(examDefinition.isPublished());
        assertNotNull(examDefinition.getUpdateData());
        
        verify(examDefinitionDAO).findExamDefinitionById(idExamDefinition);
        verify(courseDAO).searchCourseById(examDefinition.getIdCourse());
        verify(examDefinitionDAO).updateExamDefinition(any(ExamDefinition.class));
    }

    @Test
    void unpublishExamDefinition_shouldUnpublishExamDefinition() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int idExamDefinition = 1;
        int idProfessorLogado = 1;
        
        examDefinition.setPublished(true);
        
        when(examDefinitionDAO.findExamDefinitionById(idExamDefinition)).thenReturn(examDefinition);
        when(courseDAO.searchCourseById(examDefinition.getIdCourse())).thenReturn(course);
        when(examDefinitionDAO.updateExamDefinition(any(ExamDefinition.class))).thenReturn(true);
        
        // Act
        boolean result = examDefinitionService.unpublishExamDefinition(idExamDefinition, idProfessorLogado);
        
        // Assert
        assertTrue(result);
        assertFalse(examDefinition.isPublished());
        assertNotNull(examDefinition.getUpdateData());
        
        verify(examDefinitionDAO).findExamDefinitionById(idExamDefinition);
        verify(courseDAO).searchCourseById(examDefinition.getIdCourse());
        verify(examDefinitionDAO).updateExamDefinition(any(ExamDefinition.class));
    }

    @Test
    void createExamDefinition_withEmptyTitle_shouldThrowException() {
        // Arrange
        int idCourse = 1;
        int idProfessorLogado = 1;
        String title = "";
        String description = "Test your Java knowledge";
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            try {
                examDefinitionService.createExamDefinition(idCourse, idProfessorLogado, title, description);
            } catch (SQLException e) {
                fail("Should not throw SQLException but threw " + e);
            }
        });
        
        assertEquals("O título do exame não pode ser vazio.", exception.getMessage());
        
        verifyNoInteractions(examDefinitionDAO);
    }

    @Test
    void createExamDefinition_withNonExistentCourse_shouldThrowException() throws SQLException {
        // Arrange
        int idCourse = 999;
        int idProfessorLogado = 1;
        String title = "Java Midterm Exam";
        String description = "Test your Java knowledge";
        
        when(courseDAO.searchCourseById(idCourse)).thenReturn(null);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            try {
                examDefinitionService.createExamDefinition(idCourse, idProfessorLogado, title, description);
            } catch (SQLException e) {
                fail("Should not throw SQLException but threw " + e);
            }
        });
        
        assertEquals("Curso com ID " + idCourse + " não encontrado.", exception.getMessage());
        
        verify(courseDAO).searchCourseById(idCourse);
        verifyNoInteractions(examDefinitionDAO);
    }

    @Test
    void createExamDefinition_withNonProfessorUser_shouldThrowException() throws SQLException {
        // Arrange
        int idCourse = 1;
        int idProfessorLogado = 2; // ID of a non-professor user
        String title = "Java Midterm Exam";
        String description = "Test your Java knowledge";
        
        when(courseDAO.searchCourseById(idCourse)).thenReturn(course);
        when(userDAO.searchById(idProfessorLogado)).thenReturn(regularUser);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            try {
                examDefinitionService.createExamDefinition(idCourse, idProfessorLogado, title, description);
            } catch (SQLException e) {
                fail("Should not throw SQLException but threw " + e);
            }
        });
        
        assertEquals("Usuário com ID " + idProfessorLogado + " não é um professor válido.", exception.getMessage());
        
        verify(courseDAO).searchCourseById(idCourse);
        verify(userDAO).searchById(idProfessorLogado);
        verifyNoInteractions(examDefinitionDAO);
    }

    @Test
    void createExamDefinition_withUnauthorizedProfessor_shouldThrowException() throws SQLException {
        // Arrange
        int idCourse = 1;
        int idProfessorLogado = 3;
        String title = "Java Midterm Exam";
        String description = "Test your Java knowledge";
        
        Professor unauthorizedProfessor = new Professor("Another Professor", "another@example.com", "password", "Physics");
        unauthorizedProfessor.setIdUser(3);
        
        when(courseDAO.searchCourseById(idCourse)).thenReturn(course);
        when(userDAO.searchById(idProfessorLogado)).thenReturn(unauthorizedProfessor);
        
        // Act & Assert
        IllegalAccessException exception = assertThrows(IllegalAccessException.class, () -> {
            try {
                examDefinitionService.createExamDefinition(idCourse, idProfessorLogado, title, description);
            } catch (SQLException e) {
                fail("Should not throw SQLException but threw " + e);
            }
        });
        
        assertEquals("Professor não autorizado a criar exames para este curso.", exception.getMessage());
        
        verify(courseDAO).searchCourseById(idCourse);
        verify(userDAO).searchById(idProfessorLogado);
        verifyNoInteractions(examDefinitionDAO);
    }


    @Test
    void publishExamDefinition_withAlreadyPublishedExam_shouldReturnTrue() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int idExamDefinition = 1;
        int idProfessorLogado = 1;
        
        examDefinition.setPublished(true);
        
        when(examDefinitionDAO.findExamDefinitionById(idExamDefinition)).thenReturn(examDefinition);
        when(courseDAO.searchCourseById(examDefinition.getIdCourse())).thenReturn(course);
        
        // Act
        boolean result = examDefinitionService.publishExamDefinition(idExamDefinition, idProfessorLogado);
        
        // Assert
        assertTrue(result);
        assertTrue(examDefinition.isPublished());
        
        verify(examDefinitionDAO).findExamDefinitionById(idExamDefinition);
        verify(courseDAO).searchCourseById(examDefinition.getIdCourse());
        verify(examDefinitionDAO, never()).updateExamDefinition(any(ExamDefinition.class));
    }
}