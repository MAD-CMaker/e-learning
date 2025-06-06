package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.*;
import com.elearning.remoteensine.model.*;
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
public class ExamServiceTest {

    @Mock
    private ExamDAO examDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private CourseDAO courseDAO;

    @Mock
    private EnrollDAO enrollDAO;

    @Mock
    private ExamDefinitionDAO examDefinitionDAO;

    private ExamService examService;
    private User student;
    private User nonStudent;
    private Course course;
    private Exam exam;
    private ExamDefinition examDefinition;

    @BeforeEach
    void setUp() {
        examService = new ExamService(examDAO, userDAO, courseDAO, enrollDAO, examDefinitionDAO);
        
        // Setup student
        student = new User("John Student", "john@example.com", "password", UserType.STUDENT);
        student.setIdUser(1);
        
        // Setup non-student user
        nonStudent = new User("Jane Professor", "jane@example.com", "password", UserType.PROFESSOR);
        nonStudent.setIdUser(2);
        
        // Setup course
        Professor professor = new Professor("Professor Name", "professor@example.com", "password", "Computer Science");
        professor.setIdUser(3);
        course = new Course("Java Programming", "Learn Java", professor, 99.99, "Programming", 40);
        course.setIdCourse(1);
        
        // Setup exam
        exam = new Exam(1, 1, 85, "Great course!");
        exam.setExamId(1);
        exam.setHourDate(LocalDateTime.now());
        
        // Setup exam definition
        examDefinition = new ExamDefinition(1, "Java Final Exam", "Test your Java knowledge");
        examDefinition.setIdDefinitionExam(1);
    }

    @Test
    void submitCourseEvaluation_withValidData_shouldSubmitEvaluation() throws SQLException {
        // Arrange
        int idCourse = 1;
        int idStudent = 1;
        int grade = 85;
        String comment = "Great course!";
        
        when(userDAO.searchById(idStudent)).thenReturn(student);
        when(courseDAO.searchCourseById(idCourse)).thenReturn(course);
        when(enrollDAO.checkEnroll(idStudent, idCourse)).thenReturn(true);
        when(examDAO.searchExamByStundentACourse(idStudent, idCourse)).thenReturn(null);
        when(examDAO.saveExam(any(Exam.class))).thenReturn(exam);
        
        // Act
        Exam result = examService.submitCourseEvaluation(idCourse, idStudent, grade, comment);
        
        // Assert
        assertNotNull(result);
        assertEquals(idCourse, result.getCourseId());
        assertEquals(idStudent, result.getStudentId());
        assertEquals(grade, result.getGrade());
        assertEquals(comment, result.getComment());
        
        verify(userDAO).searchById(idStudent);
        verify(courseDAO).searchCourseById(idCourse);
        verify(enrollDAO).checkEnroll(idStudent, idCourse);
        verify(examDAO).searchExamByStundentACourse(idStudent, idCourse);
        verify(examDAO).saveExam(any(Exam.class));
    }

    @Test
    void createExam_withValidData_shouldCreateExam() throws SQLException {
        // Arrange
        int idCourse = 1;
        int idStudent = 1;
        int grade = 85;
        String comment = "Great course!";
        
        when(userDAO.searchById(idStudent)).thenReturn(student);
        when(courseDAO.searchCourseById(idCourse)).thenReturn(course);
        when(enrollDAO.checkEnroll(idStudent, idCourse)).thenReturn(true);
        when(examDAO.searchExamByStundentACourse(idStudent, idCourse)).thenReturn(null);
        when(examDAO.saveExam(any(Exam.class))).thenReturn(exam);
        
        // Act
        Exam result = examService.createExam(idCourse, idStudent, grade, comment);
        
        // Assert
        assertNotNull(result);
        assertEquals(idCourse, result.getCourseId());
        assertEquals(idStudent, result.getStudentId());
        assertEquals(grade, result.getGrade());
        assertEquals(comment, result.getComment());
        
        verify(userDAO).searchById(idStudent);
        verify(courseDAO).searchCourseById(idCourse);
        verify(enrollDAO).checkEnroll(idStudent, idCourse);
        verify(examDAO).searchExamByStundentACourse(idStudent, idCourse);
        verify(examDAO).saveExam(any(Exam.class));
    }

    @Test
    void getEvaluationsForCourseWithStudentNames_shouldReturnEvaluationsWithNames() throws SQLException {
        // Arrange
        int idCourse = 1;
        List<Exam> examsList = new ArrayList<>();
        examsList.add(exam);
        
        when(courseDAO.searchCourseById(idCourse)).thenReturn(course);
        when(examDAO.listCoursesExams(idCourse)).thenReturn(examsList);
        when(userDAO.searchById(exam.getStudentId())).thenReturn(student);
        
        // Act
        List<Exam> result = examService.getEvaluationsForCourseWithStudentNames(idCourse);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(student.getName(), result.get(0).getStudentName());
        
        verify(courseDAO).searchCourseById(idCourse);
        verify(examDAO).listCoursesExams(idCourse);
        verify(userDAO).searchById(exam.getStudentId());
    }

    @Test
    void updateMyExam_asOwner_shouldUpdateExam() throws SQLException, IllegalAccessException {
        // Arrange
        int idExam = 1;
        int idStudentLogged = 1;
        int newGrade = 90;
        String newComment = "Updated comment";
        
        when(examDAO.searchExamById(idExam)).thenReturn(exam);
        when(examDAO.updateExam(any(Exam.class))).thenReturn(true);
        
        // Act
        boolean result = examService.updateMyExam(idExam, idStudentLogged, newGrade, newComment);
        
        // Assert
        assertTrue(result);
        assertEquals(newGrade, exam.getGrade());
        assertEquals(newComment, exam.getComment());
        
        verify(examDAO).searchExamById(idExam);
        verify(examDAO).updateExam(exam);
    }

    @Test
    void deleteExam_asOwner_shouldDeleteExam() throws SQLException, IllegalAccessException {
        // Arrange
        int idExam = 1;
        int idUserLogged = 1;
        
        when(examDAO.searchExamById(idExam)).thenReturn(exam);
        when(userDAO.searchById(idUserLogged)).thenReturn(student);
        when(examDAO.deleteExam(idExam)).thenReturn(true);
        
        // Act
        boolean result = examService.deleteExam(idExam, idUserLogged);
        
        // Assert
        assertTrue(result);
        
        verify(examDAO).searchExamById(idExam);
        verify(userDAO).searchById(idUserLogged);
        verify(examDAO).deleteExam(idExam);
    }

    @Test
    void submitCourseEvaluation_withNonExistentStudent_shouldThrowException() throws SQLException {
        // Arrange
        int idCourse = 1;
        int idStudent = 999; // Non-existent student ID
        int grade = 85;
        String comment = "Great course!";
        
        when(userDAO.searchById(idStudent)).thenReturn(null);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            examService.submitCourseEvaluation(idCourse, idStudent, grade, comment);
        });
        
        assertTrue(exception.getMessage().contains("não encontrado"));
        
        verify(userDAO).searchById(idStudent);
        verifyNoInteractions(examDAO);
    }

    @Test
    void submitCourseEvaluation_withNonStudentUser_shouldThrowException() throws SQLException {
        // Arrange
        int idCourse = 1;
        int idStudent = 2; // ID of a non-student user
        int grade = 85;
        String comment = "Great course!";
        
        when(userDAO.searchById(idStudent)).thenReturn(nonStudent);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            examService.submitCourseEvaluation(idCourse, idStudent, grade, comment);
        });
        
        assertTrue(exception.getMessage().contains("não é um estudante"));
        
        verify(userDAO).searchById(idStudent);
        verifyNoInteractions(examDAO);
    }

    @Test
    void submitCourseEvaluation_withNonExistentCourse_shouldThrowException() throws SQLException {
        // Arrange
        int idCourse = 999; // Non-existent course ID
        int idStudent = 1;
        int grade = 85;
        String comment = "Great course!";
        
        when(userDAO.searchById(idStudent)).thenReturn(student);
        when(courseDAO.searchCourseById(idCourse)).thenReturn(null);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            examService.submitCourseEvaluation(idCourse, idStudent, grade, comment);
        });
        
        assertTrue(exception.getMessage().contains("não encontrado"));
        
        verify(userDAO).searchById(idStudent);
        verify(courseDAO).searchCourseById(idCourse);
        verifyNoInteractions(examDAO);
    }
}