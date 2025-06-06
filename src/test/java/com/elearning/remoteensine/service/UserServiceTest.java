package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.UserDAO;
import com.elearning.remoteensine.model.Professor;
import com.elearning.remoteensine.model.Student;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userDAO);
    }

    @Test
    void createUser_shouldCreateStudentSuccessfully() throws SQLException {
        // Arrange
        String name = "John Doe";
        String email = "john@example.com";
        String password = "password123";
        UserType type = UserType.STUDENT;
        
        // Mock the userDAO behavior
        when(userDAO.searchByEmail(email)).thenReturn(null);
        
        Student expectedStudent = new Student(name, email, "hashedPassword");
        expectedStudent.setIdUser(1);
        when(userDAO.saveUser(any(Student.class))).thenReturn(expectedStudent);
        
        // Act
        User result = userService.createUser(name, email, password, type, null);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getIdUser());
        assertEquals(name, result.getName());
        assertEquals(email, result.getEmail());
        assertEquals(UserType.STUDENT, result.getUserType());

        // Verify interactions with the DAO
        verify(userDAO).searchByEmail(email);
        verify(userDAO).saveUser(any(Student.class));
    }

    @Test
    void createUser_shouldCreateProfessorSuccessfully() throws SQLException {
        // Arrange
        String name = "Jane Smith";
        String email = "jane@example.com";
        String password = "password456";
        UserType type = UserType.PROFESSOR;
        String specialization = "Computer Science";
        
        // Mock the userDAO behavior
        when(userDAO.searchByEmail(email)).thenReturn(null);
        
        Professor expectedProfessor = new Professor(name, email, "hashedPassword", specialization);
        expectedProfessor.setIdUser(2);
        when(userDAO.saveUser(any(Professor.class))).thenReturn(expectedProfessor);
        
        // Act
        User result = userService.createUser(name, email, password, type, specialization);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.getIdUser());
        assertEquals(name, result.getName());
        assertEquals(email, result.getEmail());
        assertEquals(UserType.PROFESSOR, result.getUserType());
        assertTrue(result instanceof Professor);
        assertEquals(specialization, ((Professor) result).getSpecialization());

        // Verify interactions with the DAO
        verify(userDAO).searchByEmail(email);
        verify(userDAO).saveUser(any(Professor.class));
    }

    @Test
    void validateLogin_shouldReturnUserWithCorrectCredentials() throws SQLException {
        // Arrange
        String email = "user@example.com";
        String password = "correctPassword";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        
        User mockUser = new User("Test User", email, hashedPassword, UserType.STUDENT);
        mockUser.setIdUser(3);
        
        when(userDAO.searchByEmail(email)).thenReturn(mockUser);
        
        // Act
        User result = userService.validateLogin(email, password);
        
        // Assert
        assertNotNull(result);
        assertEquals(3, result.getIdUser());
        assertEquals("Test User", result.getName());
        assertEquals(email, result.getEmail());
        
        // Verify interactions with the DAO
        verify(userDAO).searchByEmail(email);
    }

    @Test
    void searchUserById_shouldReturnUserWhenFound() throws SQLException {
        // Arrange
        int userId = 4;
        User mockUser = new User("Found User", "found@example.com", "hashedPassword", UserType.STUDENT);
        mockUser.setIdUser(userId);
        
        when(userDAO.searchById(userId)).thenReturn(mockUser);
        
        // Act
        User result = userService.searchUserById(userId);
        
        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getIdUser());
        assertEquals("Found User", result.getName());
        assertEquals("found@example.com", result.getEmail());
        
        // Verify interactions with the DAO
        verify(userDAO).searchById(userId);
    }

    @Test
    void createUser_shouldThrowExceptionForExistingEmail() throws SQLException {
        // Arrange
        String name = "Existing User";
        String email = "existing@example.com";
        String password = "password789";
        UserType type = UserType.STUDENT;
        
        User existingUser = new User(name, email, "hashedPassword", type);
        when(userDAO.searchByEmail(email)).thenReturn(existingUser);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(name, email, password, type, null);
        });
        
        assertTrue(exception.getMessage().contains("already registered"));
        
        // Verify interactions with the DAO
        verify(userDAO).searchByEmail(email);
        verify(userDAO, never()).saveUser(any(User.class));
    }

    @Test
    void createUser_shouldThrowExceptionForProfessorWithoutSpecialization() throws SQLException {
        // Arrange
        String name = "Professor Without Specialization";
        String email = "prof@example.com";
        String password = "password789";
        UserType type = UserType.PROFESSOR;
        String specialization = null; // Empty specialization
        
        when(userDAO.searchByEmail(email)).thenReturn(null);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(name, email, password, type, specialization);
        });
        
        assertEquals("Especialização é obrigatória para professores.", exception.getMessage());
        
        // Verify interactions with the DAO
        verify(userDAO).searchByEmail(email);
        verify(userDAO, never()).saveUser(any(User.class));
    }

    @Test
    void validateLogin_shouldReturnNullWithIncorrectPassword() throws SQLException {
        // Arrange
        String email = "user@example.com";
        String correctPassword = "correctPassword";
        String incorrectPassword = "wrongPassword";
        String hashedPassword = BCrypt.hashpw(correctPassword, BCrypt.gensalt());
        
        User mockUser = new User("Test User", email, hashedPassword, UserType.STUDENT);
        mockUser.setIdUser(3);
        
        when(userDAO.searchByEmail(email)).thenReturn(mockUser);
        
        // Act
        User result = userService.validateLogin(email, incorrectPassword);
        
        // Assert
        assertNull(result);
        
        // Verify interactions with the DAO
        verify(userDAO).searchByEmail(email);
    }

    @Test
    void createUser_shouldThrowExceptionForInvalidUserType() throws SQLException {
        // Arrange
        String name = "Invalid Type User";
        String email = "invalid@example.com";
        String password = "password123";
        UserType type = null; // Invalid user type
        
        when(userDAO.searchByEmail(email)).thenReturn(null);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            userService.createUser(name, email, password, type, null);
        });
        
        assertEquals("Unknown user type: " + type, exception.getMessage());
        
        // Verify interactions with the DAO
        verify(userDAO).searchByEmail(email);
        verify(userDAO, never()).saveUser(any(User.class));
    }
}