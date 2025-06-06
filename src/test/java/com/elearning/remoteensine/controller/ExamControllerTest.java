package com.elearning.remoteensine.controller;

import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.Exam;
import com.elearning.remoteensine.model.Professor;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.UserType;
import com.elearning.remoteensine.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExamControllerTest {

    @Mock
    private ExamService examService;
    
    @Mock
    private CourseService courseService;
    
    @Mock
    private EnrollService enrollService;
    
    @Mock
    private ExamDefinitionService examDefinitionService;
    
    @Mock
    private ExamQuestionService examQuestionService;
    
    @Mock
    private HttpSession session;
    
    @Mock
    private Model model;
    
    @Mock
    private RedirectAttributes redirectAttributes;
    
    private ExamController examController;
    private User studentUser;
    private User professorUser;
    private Course testCourse;
    private Exam testExam;
    
    @BeforeEach
    void setUp() {
        examController = new ExamController(
            examService, courseService, enrollService, examQuestionService, examDefinitionService
        );
        
        // Setup student user
        studentUser = new User("Student Name", "student@example.com", "password", UserType.STUDENT);
        studentUser.setIdUser(1);
        
        // Setup professor user
        professorUser = new Professor("Professor Name", "professor@example.com", "password", "Computer Science");
        professorUser.setIdUser(2);
        
        // Setup test course
        testCourse = new Course("Java Programming", "Learn Java Programming", (Professor) professorUser, 99.99, "Programming", 40);
        testCourse.setIdCourse(1);
        
        // Setup test exam
        testExam = new Exam(testCourse.getIdCourse(), studentUser.getIdUser(), 5, "Great course!");
        testExam.setExamId(1);
    }

    @Test
    void exibirFormularioAvaliacao_withStudentEnrolled_shouldDisplayForm() throws SQLException {
        // Arrange
        int courseId = testCourse.getIdCourse();
        
        when(session.getAttribute("usuarioLogado")).thenReturn(studentUser);
        when(courseService.searchCourseByIdComplete(courseId)).thenReturn(testCourse);
        when(enrollService.checkEnrolledStudents(studentUser.getIdUser(), courseId)).thenReturn(true);
        when(examService.hasStudentEvaluatedCourse(studentUser.getIdUser(), courseId)).thenReturn(false);
        
        // Act
        String viewName = examController.exibirFormularioAvaliacao(courseId, model, session, redirectAttributes);
        
        // Assert
        assertEquals("form-avaliacao-curso", viewName);
        verify(model).addAttribute("curso", testCourse);
        verify(model).addAttribute(eq("avaliacao"), any(Exam.class));
        verify(courseService).searchCourseByIdComplete(courseId);
        verify(enrollService).checkEnrolledStudents(studentUser.getIdUser(), courseId);
        verify(examService).hasStudentEvaluatedCourse(studentUser.getIdUser(), courseId);
    }
    
    @Test
    void exibirFormularioAvaliacao_withNonStudentUser_shouldRedirectToCourse() throws SQLException {
        // Arrange
        int courseId = testCourse.getIdCourse();
        
        when(session.getAttribute("usuarioLogado")).thenReturn(professorUser);
        
        // Act
        String viewName = examController.exibirFormularioAvaliacao(courseId, model, session, redirectAttributes);
        
        // Assert
        assertEquals("redirect:/cursos/" + courseId, viewName);
        verify(redirectAttributes).addFlashAttribute(eq("erro_geral"), anyString());
        verifyNoInteractions(courseService);
        verifyNoInteractions(enrollService);
        verifyNoInteractions(examService);
    }
    
    @Test
    void exibirFormularioAvaliacao_withNonExistentCourse_shouldRedirectToCourses() throws SQLException {
        // Arrange
        int courseId = 999; // Non-existent course ID
        
        when(session.getAttribute("usuarioLogado")).thenReturn(studentUser);
        when(courseService.searchCourseByIdComplete(courseId)).thenReturn(null);
        
        // Act
        String viewName = examController.exibirFormularioAvaliacao(courseId, model, session, redirectAttributes);
        
        // Assert
        assertEquals("redirect:/cursos", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("erro_geral"), anyString());
        verify(courseService).searchCourseByIdComplete(courseId);
        verifyNoInteractions(enrollService);
        verifyNoInteractions(examService);
    }
    
    @Test
    void salvarAvaliacao_withValidData_shouldSaveAndRedirect() throws SQLException {
        // Arrange
        int courseId = testCourse.getIdCourse();
        
        when(session.getAttribute("usuarioLogado")).thenReturn(studentUser);
        when(examService.submitCourseEvaluation(
            eq(courseId), 
            eq(studentUser.getIdUser()), 
            eq(testExam.getGrade()), 
            eq(testExam.getComment())
        )).thenReturn(testExam);
        
        // Act
        String viewName = examController.salvarAvaliacao(courseId, testExam, session, redirectAttributes, model);
        
        // Assert
        assertEquals("redirect:/cursos/" + courseId, viewName);
        verify(examService).submitCourseEvaluation(
            eq(courseId), 
            eq(studentUser.getIdUser()), 
            eq(testExam.getGrade()), 
            eq(testExam.getComment())
        );
        verify(redirectAttributes).addFlashAttribute(eq("sucesso_global"), anyString());
    }
}