package com.elearning.remoteensine.controller;

import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.VisitorQuestion;
import com.elearning.remoteensine.model.enums.UserType;
import com.elearning.remoteensine.service.UserService;
import com.elearning.remoteensine.service.VisitorQuestionService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VisitorQuestionControllerTest {

    @Mock
    private VisitorQuestionService visitorQuestionService;

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private HttpSession session;

    private VisitorQuestionController visitorQuestionController;
    private User professorUser;
    private User regularUser;
    private VisitorQuestion sampleQuestion;

    @BeforeEach
    void setUp() {
        visitorQuestionController = new VisitorQuestionController(visitorQuestionService, userService);
        
        // Setup a professor user for testing
        professorUser = new User("Test Professor", "professor@example.com", "password", UserType.PROFESSOR);
        professorUser.setIdUser(1);
        
        // Setup a regular user for testing
        regularUser = new User("Test Student", "student@example.com", "password", UserType.STUDENT);
        regularUser.setIdUser(2);
        
        // Setup a sample question
        sampleQuestion = new VisitorQuestion("Test Visitor", "visitor@example.com", "How do I enroll in a course?");
        sampleQuestion.setQuestionId(1);
    }

    @Test
    void exibirFormularioPergunta_shouldReturnContactFormView() {
        // Act
        String viewName = visitorQuestionController.exibirFormularioPergunta(model);
        
        // Assert
        assertEquals("form-pergunta-visitante", viewName);
    }

    @Test
    void processarFormularioPergunta_withValidData_shouldRedirectToContactPage() throws SQLException {
        // Arrange
        String nomeVisitante = "Test Visitor";
        String emailVisitante = "visitor@example.com";
        String textoPergunta = "How do I enroll in a course?";
        
        VisitorQuestion submittedQuestion = new VisitorQuestion(nomeVisitante, emailVisitante, textoPergunta);
        submittedQuestion.setQuestionId(1);
        
        when(visitorQuestionService.submitQuestion(nomeVisitante, emailVisitante, textoPergunta))
            .thenReturn(submittedQuestion);
        
        // Act
        String result = visitorQuestionController.processarFormularioPergunta(
            nomeVisitante, emailVisitante, textoPergunta, redirectAttributes, model);
        
        // Assert
        assertEquals("redirect:/contato", result);
        verify(visitorQuestionService).submitQuestion(nomeVisitante, emailVisitante, textoPergunta);
        verify(redirectAttributes).addFlashAttribute(
            eq("sucesso_pergunta"), 
            eq("Sua pergunta foi enviada com sucesso! Responderemos em breve.")
        );
    }

    @Test
    void listarPerguntasVisitantesParaProfessor_withLoggedInProfessor_shouldDisplayQuestions() throws Exception {
        // Arrange
        List<VisitorQuestion> questionsList = new ArrayList<>();
        questionsList.add(sampleQuestion);
        
        when(session.getAttribute("usuarioLogado")).thenReturn(professorUser);
        when(visitorQuestionService.listAllQuestions(false)).thenReturn(questionsList);
        
        // Act
        String result = visitorQuestionController.listarPerguntasVisitantesParaProfessor(model, session, redirectAttributes);
        
        // Assert
        assertEquals("professor/lista-perguntas-visitantes", result);
        verify(visitorQuestionService).listAllQuestions(false);
        verify(model).addAttribute("listaPerguntasVisitantes", questionsList);
    }

    @Test
    void listarPerguntasVisitantesParaProfessor_withUnauthorizedUser_shouldRedirectToLogin() {
        // Arrange
        when(session.getAttribute("usuarioLogado")).thenReturn(regularUser);
        
        // Act
        String result = visitorQuestionController.listarPerguntasVisitantesParaProfessor(model, session, redirectAttributes);
        
        // Assert
        assertEquals("redirect:/login", result);
        verify(redirectAttributes).addFlashAttribute(
            eq("erro_geral"), 
            eq("Acesso restrito a professores.")
        );
        verifyNoInteractions(visitorQuestionService);
    }

    @Test
    void responderPerguntaVisitante_withLoggedInProfessor_shouldAnswerSuccessfully() throws Exception {
        // Arrange
        int idPergunta = 1;
        String textoResposta = "You can enroll through our website by clicking on the 'Enroll' button.";
        
        when(session.getAttribute("usuarioLogado")).thenReturn(professorUser);
        when(visitorQuestionService.answerQuestion(idPergunta, professorUser.getIdUser(), textoResposta))
            .thenReturn(sampleQuestion);
        
        // Act
        String result = visitorQuestionController.responderPerguntaVisitante(
            idPergunta, textoResposta, session, redirectAttributes);
        
        // Assert
        assertEquals("redirect:/professor/perguntas-visitantes", result);
        verify(visitorQuestionService).answerQuestion(idPergunta, professorUser.getIdUser(), textoResposta);
        verify(redirectAttributes).addFlashAttribute(
            eq("sucesso_resposta_" + idPergunta), 
            eq("Resposta enviada com sucesso!")
        );
    }
}