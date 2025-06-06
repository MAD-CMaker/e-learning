package com.elearning.remoteensine.controller;

import com.elearning.remoteensine.model.Professor;
import com.elearning.remoteensine.model.Student;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.UserType;
import com.elearning.remoteensine.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private Model model;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpSession session;

    private UserController userController;
    private User studentUser;
    private User professorUser;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService);

        // Setup student user
        studentUser = new Student("Student Name", "student@example.com", "password");
        studentUser.setIdUser(1);

        // Setup professor user
        professorUser = new Professor("Professor Name", "professor@example.com", "password", "Computer Science");
        professorUser.setIdUser(2);

    }

    @Test
    void displayRegistrationForm_shouldReturnCadastroView() {
        // Act
        String viewName = userController.displayRegistrationForm(model);
        
        // Assert
        assertEquals("cadastro", viewName);
    }

    @Test
    void processFormRegistration_withValidStudentData_shouldRedirectToLogin() throws SQLException {
        // Arrange
        String nome = "New Student";
        String email = "newstudent@example.com";
        String senha = "password123";
        String tipoUsuarioStr = "STUDENT";
        
        Student createdStudent = new Student(nome, email, senha);
        createdStudent.setIdUser(3);
        
        when(userService.createUser(eq(nome), eq(email), eq(senha), eq(UserType.STUDENT), isNull()))
            .thenReturn(createdStudent);
        
        // Act
        String viewName = userController.processFormRegistration(
            nome, email, senha, tipoUsuarioStr, null, redirectAttributes, model);
        
        // Assert
        assertEquals("redirect:/login", viewName);
        verify(userService).createUser(eq(nome), eq(email), eq(senha), eq(UserType.STUDENT), isNull());
        verify(redirectAttributes).addFlashAttribute(eq("sucesso_cadastro"), anyString());
    }

    @Test
    void processFormRegistration_withValidProfessorData_shouldRedirectToLogin() throws SQLException {
        // Arrange
        String nome = "New Professor";
        String email = "newprofessor@example.com";
        String senha = "password123";
        String tipoUsuarioStr = "PROFESSOR";
        String especializacao = "Mathematics";
        
        Professor createdProfessor = new Professor(nome, email, senha, especializacao);
        createdProfessor.setIdUser(4);
        
        when(userService.createUser(eq(nome), eq(email), eq(senha), eq(UserType.PROFESSOR), eq(especializacao)))
            .thenReturn(createdProfessor);
        
        // Act
        String viewName = userController.processFormRegistration(
            nome, email, senha, tipoUsuarioStr, especializacao, redirectAttributes, model);
        
        // Assert
        assertEquals("redirect:/login", viewName);
        verify(userService).createUser(eq(nome), eq(email), eq(senha), eq(UserType.PROFESSOR), eq(especializacao));
        verify(redirectAttributes).addFlashAttribute(eq("sucesso_cadastro"), anyString());
    }

    @Test
    void displayRegistrationLogin_shouldReturnLoginView() {
        // Act
        String viewName = userController.displayRegistrationLogin(model);
        
        // Assert
        assertEquals("login", viewName);
    }

    @Test
    void processarFormularioLogin_withValidCredentials_shouldRedirectToCursos() throws SQLException {
        // Arrange
        String email = "student@example.com";
        String senha = "password";
        // Setup mock for HttpServletRequest to return the mock session
        when(request.getSession(anyBoolean())).thenReturn(session);
        
        when(userService.validateLogin(email, senha)).thenReturn(studentUser);
        
        // Act
        String viewName = userController.processarFormularioLogin(
            email, senha, redirectAttributes, model, request);
        
        // Assert
        assertEquals("redirect:/cursos", viewName);
        verify(session).setAttribute("usuarioLogado", studentUser);
        verify(session).setMaxInactiveInterval(30 * 60);
        verify(redirectAttributes).addFlashAttribute(eq("mensagemGlobal"), anyString());
    }


    @Test
    void exibirPaginaPerfil_withLoggedInUser_shouldReturnPerfilUsuarioView() {
        // Arrange
        when(session.getAttribute("usuarioLogado")).thenReturn(studentUser);
        
        // Act
        String viewName = userController.exibirPaginaPerfil(session, model, redirectAttributes);
        
        // Assert
        assertEquals("perfil-usuario", viewName);
        verify(model).addAttribute("usuarioPerfil", studentUser);
        verify(model).addAttribute("isProfessorProfile", false);
        verify(model).addAttribute("isStudentProfile", true);
    }

    @Test
    void processFormRegistration_withInvalidUserType_shouldReturnCadastroView() throws SQLException {
        // Arrange
        String nome = "Test User";
        String email = "testuser@example.com";
        String senha = "password123";
        String tipoUsuarioStr = "INVALID_TYPE";
        
        // Act
        String viewName = userController.processFormRegistration(
            nome, email, senha, tipoUsuarioStr, null, redirectAttributes, model);
        
        // Assert
        assertEquals("cadastro", viewName);
        verify(model).addAttribute(eq("erro_tipoUsuario"), anyString());
        verify(model).addAttribute("nome", nome);
        verify(model).addAttribute("email", email);
        verify(model).addAttribute("especializacao", null);
        verifyNoInteractions(userService);
    }

    @Test
    void processFormRegistration_professorWithoutSpecialization_shouldReturnCadastroView() throws SQLException {
        // Arrange
        String nome = "Professor Without Spec";
        String email = "professor@example.com";
        String senha = "password123";
        String tipoUsuarioStr = "PROFESSOR";
        String especializacao = "";
        
        // Act
        String viewName = userController.processFormRegistration(
            nome, email, senha, tipoUsuarioStr, especializacao, redirectAttributes, model);
        
        // Assert
        assertEquals("cadastro", viewName);
        verify(model).addAttribute(eq("erro_especializacao"), anyString());
        verify(model).addAttribute("nome", nome);
        verify(model).addAttribute("email", email);
        verify(model).addAttribute("tipoUsuarioSelecionado", tipoUsuarioStr);
        verifyNoInteractions(userService);
    }

    @Test
    void processFormRegistration_withExistingEmail_shouldReturnCadastroView() throws SQLException {
        // Arrange
        String nome = "Duplicate Email";
        String email = "existing@example.com";
        String senha = "password123";
        String tipoUsuarioStr = "STUDENT";
        
        when(userService.createUser(eq(nome), eq(email), eq(senha), eq(UserType.STUDENT), isNull()))
            .thenThrow(new IllegalArgumentException("This e-mail 'existing@example.com' is already registered."));
        
        // Act
        String viewName = userController.processFormRegistration(
            nome, email, senha, tipoUsuarioStr, null, redirectAttributes, model);
        
        // Assert
        assertEquals("cadastro", viewName);
        verify(model).addAttribute(eq("erro_email"), anyString());
        verify(model).addAttribute("nome", nome);
        verify(model).addAttribute("email", email);
        verify(model).addAttribute("especializacao", null);
        verify(model).addAttribute("tipoUsuarioSelecionado", tipoUsuarioStr);
    }

    @Test
    void processarFormularioLogin_withInvalidCredentials_shouldReturnLoginView() throws SQLException {
        // Arrange
        String email = "invalid@example.com";
        String senha = "wrongpassword";
        
        when(userService.validateLogin(email, senha)).thenReturn(null);
        
        // Act
        String viewName = userController.processarFormularioLogin(
            email, senha, redirectAttributes, model, request);
        
        // Assert
        assertEquals("login", viewName);
        verify(model).addAttribute(eq("erro_login"), anyString());
    }

    @Test
    void exibirPaginaPerfil_withoutLogin_shouldRedirectToLogin() {
        // Arrange
        when(session.getAttribute("usuarioLogado")).thenReturn(null);
        
        // Act
        String viewName = userController.exibirPaginaPerfil(session, model, redirectAttributes);
        
        // Assert
        assertEquals("redirect:/login", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("erro_login"), anyString());
        verifyNoInteractions(model);
    }
}