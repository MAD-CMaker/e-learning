package com.elearning.remoteensine.controller;

import com.elearning.remoteensine.model.*;
import com.elearning.remoteensine.model.enums.ExerciseType;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExamDefinitionControllerTest {

    @Mock
    private ExamDefinitionService examDefinitionService;
    
    @Mock
    private ExamQuestionService examQuestionService;
    
    @Mock
    private CourseService courseService;
    
    @Mock
    private EnrollService enrollService;
    
    @Mock
    private ExamService examService;
    
    @Mock
    private HttpSession session;
    
    @Mock
    private Model model;
    
    @Mock
    private RedirectAttributes redirectAttributes;
    
    private ExamDefinitionController testeController;
    private User studentUser;
    private User professorUser;
    private Course testCourse;
    private ExamDefinition testExamDefinition;
    private List<ExamQuestion> testQuestions;
    
    @BeforeEach
    void setUp() {
        testeController = new ExamDefinitionController(
            examDefinitionService, examQuestionService, courseService, enrollService, examService
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
        
        // Setup test exam definition
        testExamDefinition = new ExamDefinition(testCourse.getIdCourse(), "Java Basics Test", "Test covering Java basics");
        testExamDefinition.setIdDefinitionExam(1);
        testExamDefinition.setPublished(true);
        
        // Setup test questions
        testQuestions = new ArrayList<>();
        ExamQuestion question1 = new ExamQuestion(
            testExamDefinition.getIdDefinitionExam(),
            "What is Java?",
            ExerciseType.MULTIPLA_ESCOLHA,
            "A:A programming language;B:A coffee type;C:An island",
            "A",
            10.0,
            1
        );
        question1.setIdExamQuestion(1);
        
        ExamQuestion question2 = new ExamQuestion(
            testExamDefinition.getIdDefinitionExam(),
            "Explain polymorphism",
            ExerciseType.DISSERTATIVA,
            "",
            "Polymorphism is the ability of an object to take many forms",
            10.0,
            2
        );
        question2.setIdExamQuestion(2);
        
        testQuestions.add(question1);
        testQuestions.add(question2);
    }

    @Test
    void exibirPaginaRealizarProva_withStudentLoggedIn_shouldDisplayExamPage() throws SQLException {
        // Arrange
        int courseId = testCourse.getIdCourse();
        int examDefId = testExamDefinition.getIdDefinitionExam();
        
        when(session.getAttribute("usuarioLogado")).thenReturn(studentUser);
        when(courseService.searchCourseByIdComplete(courseId)).thenReturn(testCourse);
        when(examDefinitionService.getExamDefinitionById(examDefId)).thenReturn(testExamDefinition);
        when(enrollService.checkEnrolledStudents(studentUser.getIdUser(), courseId)).thenReturn(true);
        when(examQuestionService.getQuestionsForStudentToTakeExam(examDefId)).thenReturn(testQuestions);
        
        // Act
        String viewName = testeController.exibirPaginaRealizarProva(courseId, examDefId, model, session, redirectAttributes);
        
        // Assert
        assertEquals("exam/realizar-prova", viewName);
        verify(model).addAttribute("curso", testCourse);
        verify(model).addAttribute("definicaoProva", testExamDefinition);
        verify(model).addAttribute("questoesDaProva", testQuestions);
        verify(courseService).searchCourseByIdComplete(courseId);
        verify(examDefinitionService).getExamDefinitionById(examDefId);
        verify(enrollService).checkEnrolledStudents(studentUser.getIdUser(), courseId);
        verify(examQuestionService).getQuestionsForStudentToTakeExam(examDefId);
    }
    
    @Test
    void exibirPaginaRealizarProva_withNonStudentUser_shouldRedirectToLogin() {
        // Arrange
        int courseId = testCourse.getIdCourse();
        int examDefId = testExamDefinition.getIdDefinitionExam();
        
        when(session.getAttribute("usuarioLogado")).thenReturn(professorUser);
        
        // Act
        String viewName = testeController.exibirPaginaRealizarProva(courseId, examDefId, model, session, redirectAttributes);
        
        // Assert
        assertEquals("redirect:/login", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("erro_geral"), anyString());
        verifyNoInteractions(courseService);
        verifyNoInteractions(examDefinitionService);
        verifyNoInteractions(enrollService);
        verifyNoInteractions(examQuestionService);
    }
    
    @Test
    void exibirPaginaRealizarProva_withInvalidCourseOrExam_shouldRedirectToCourse() throws SQLException {
        // Arrange
        int courseId = testCourse.getIdCourse();
        int examDefId = 999; // Non-existent exam definition ID
        
        when(session.getAttribute("usuarioLogado")).thenReturn(studentUser);
        when(courseService.searchCourseByIdComplete(courseId)).thenReturn(testCourse);
        when(examDefinitionService.getExamDefinitionById(examDefId)).thenReturn(null);
        
        // Act
        String viewName = testeController.exibirPaginaRealizarProva(courseId, examDefId, model, session, redirectAttributes);
        
        // Assert
        assertEquals("redirect:/cursos/" + courseId, viewName);
        verify(redirectAttributes).addFlashAttribute(eq("erro_geral"), anyString());
        verify(courseService).searchCourseByIdComplete(courseId);
        verify(examDefinitionService).getExamDefinitionById(examDefId);
        verifyNoInteractions(enrollService);
        verifyNoInteractions(examQuestionService);
    }
    
    @Test
    void submeterProva_withValidData_shouldSaveAndRedirect() throws SQLException {
        // Arrange
        int courseId = testCourse.getIdCourse();
        int examDefId = testExamDefinition.getIdDefinitionExam();
        Map<String, String> respostas = new HashMap<>();
        respostas.put("resposta_q1", "A");
        respostas.put("resposta_q2", "Polymorphism is the ability of an object to take many forms");
        
        when(session.getAttribute("usuarioLogado")).thenReturn(studentUser);
        when(examDefinitionService.getExamDefinitionById(examDefId)).thenReturn(testExamDefinition);
        when(examQuestionService.getQuestionsForStudentToTakeExam(examDefId)).thenReturn(testQuestions);
        when(examService.saveStudentExamAttempt(any(Exam.class))).thenAnswer(invocation -> {
            Exam exam = invocation.getArgument(0);
            exam.setExamId(1);
            return exam;
        });
        
        // Act
        String viewName = testeController.submeterProva(courseId, examDefId, respostas, session, redirectAttributes, model);
        
        // Assert
        assertEquals("redirect:/cursos/" + courseId, viewName);
        verify(examDefinitionService).getExamDefinitionById(examDefId);
        verify(examQuestionService).getQuestionsForStudentToTakeExam(examDefId);
        verify(examService).saveStudentExamAttempt(any(Exam.class));
        verify(redirectAttributes).addFlashAttribute(eq("sucesso_global"), anyString());
    }
    
    @Test
    void submeterProva_withExceptionDuringSubmission_shouldRedirectBackToExam() throws SQLException {
        // Arrange
        int courseId = testCourse.getIdCourse();
        int examDefId = testExamDefinition.getIdDefinitionExam();
        Map<String, String> respostas = new HashMap<>();
        
        when(session.getAttribute("usuarioLogado")).thenReturn(studentUser);
        when(examDefinitionService.getExamDefinitionById(examDefId)).thenReturn(testExamDefinition);
        when(examQuestionService.getQuestionsForStudentToTakeExam(examDefId)).thenThrow(new SQLException("Database error"));
        
        // Act
        String viewName = testeController.submeterProva(courseId, examDefId, respostas, session, redirectAttributes, model);
        
        // Assert
        assertEquals("redirect:/cursos/" + courseId + "/provas/" + examDefId + "/realizar", viewName);
        verify(examDefinitionService).getExamDefinitionById(examDefId);
        verify(examQuestionService).getQuestionsForStudentToTakeExam(examDefId);
        verify(redirectAttributes).addFlashAttribute(eq("erro_geral"), anyString());
    }
}