package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.Professor;
import com.elearning.remoteensine.model.Student;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.UserType;
import com.elearning.remoteensine.util.DatabaseConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserDAOTest {

    @Mock
    private DatabaseConnector databaseConnector;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private UserDAO userDAO;

    @BeforeEach
    void setUp() throws SQLException {
        userDAO = new UserDAO(databaseConnector);
        when(databaseConnector.getConnection()).thenReturn(connection);
    }

    @Test
    void saveUser_shouldSaveAndReturnStudentWithId() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);

        Student student = new Student("John Doe", "john.doe@example.com", "hashedPassword123");
        LocalDateTime registerDate = LocalDateTime.now();
        student.setRegisterDate(registerDate);

        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(10);

        // Act
        User savedUser = userDAO.saveUser(student);

        // Assert
        assertEquals(10, savedUser.getIdUser());
        assertEquals("John Doe", savedUser.getName());
        assertEquals("john.doe@example.com", savedUser.getEmail());
        assertEquals("hashedPassword123", savedUser.getPassword());
        assertEquals(UserType.STUDENT, savedUser.getUserType());
        assertEquals(registerDate, savedUser.getRegisterDate());

        verify(preparedStatement).setString(1, "John Doe");
        verify(preparedStatement).setString(2, "john.doe@example.com");
        verify(preparedStatement).setString(3, "hashedPassword123");
        verify(preparedStatement).setString(4, "STUDENT");
        verify(preparedStatement).setNull(5, Types.VARCHAR);
        verify(preparedStatement).setTimestamp(eq(6), any(Timestamp.class));
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).getGeneratedKeys();
    }

    @Test
    void saveUser_shouldSaveAndReturnProfessorWithId() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);

        Professor professor = new Professor("Jane Smith", "jane.smith@example.com", "hashedPassword456", "Computer Science");
        LocalDateTime registerDate = LocalDateTime.now();
        professor.setRegisterDate(registerDate);

        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(20);

        // Act
        User savedUser = userDAO.saveUser(professor);

        // Assert
        assertEquals(20, savedUser.getIdUser());
        assertEquals("Jane Smith", savedUser.getName());
        assertEquals("jane.smith@example.com", savedUser.getEmail());
        assertEquals("hashedPassword456", savedUser.getPassword());
        assertEquals(UserType.PROFESSOR, savedUser.getUserType());
        assertEquals("Computer Science", ((Professor) savedUser).getSpecialization());
        assertEquals(registerDate, savedUser.getRegisterDate());

        verify(preparedStatement).setString(1, "Jane Smith");
        verify(preparedStatement).setString(2, "jane.smith@example.com");
        verify(preparedStatement).setString(3, "hashedPassword456");
        verify(preparedStatement).setString(4, "PROFESSOR");
        verify(preparedStatement).setString(5, "Computer Science");
        verify(preparedStatement).setTimestamp(eq(6), any(Timestamp.class));
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).getGeneratedKeys();
    }

    @Test
    void searchByEmail_shouldReturnStudentWhenFound() throws SQLException {
        // Arrange
        String email = "john.doe@example.com";
        LocalDateTime registerDate = LocalDateTime.now();

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        
        when(resultSet.getInt("user_id")).thenReturn(10);
        when(resultSet.getString("name")).thenReturn("John Doe");
        when(resultSet.getString("email")).thenReturn(email);
        when(resultSet.getString("password")).thenReturn("hashedPassword123");
        when(resultSet.getString("user_type")).thenReturn("STUDENT");
        when(resultSet.getTimestamp("register_hour")).thenReturn(Timestamp.valueOf(registerDate));

        // Act
        User foundUser = userDAO.searchByEmail(email);

        // Assert
        assertNotNull(foundUser);
        assertEquals(10, foundUser.getIdUser());
        assertEquals("John Doe", foundUser.getName());
        assertEquals(email, foundUser.getEmail());
        assertEquals("hashedPassword123", foundUser.getPassword());
        assertEquals(UserType.STUDENT, foundUser.getUserType());
        assertEquals(registerDate, foundUser.getRegisterDate());

        verify(preparedStatement).setString(1, email);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void searchById_shouldReturnProfessorWhenFound() throws SQLException {
        // Arrange
        int userId = 20;
        LocalDateTime registerDate = LocalDateTime.now();

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        
        when(resultSet.getInt("user_id")).thenReturn(userId);
        when(resultSet.getString("name")).thenReturn("Jane Smith");
        when(resultSet.getString("email")).thenReturn("jane.smith@example.com");
        when(resultSet.getString("password")).thenReturn("hashedPassword456");
        when(resultSet.getString("user_type")).thenReturn("PROFESSOR");
        when(resultSet.getString("specialization")).thenReturn("Computer Science");
        when(resultSet.getTimestamp("register_hour")).thenReturn(Timestamp.valueOf(registerDate));

        // Act
        User foundUser = userDAO.searchById(userId);

        // Assert
        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getIdUser());
        assertEquals("Jane Smith", foundUser.getName());
        assertEquals("jane.smith@example.com", foundUser.getEmail());
        assertEquals("hashedPassword456", foundUser.getPassword());
        assertEquals(UserType.PROFESSOR, foundUser.getUserType());
        assertTrue(foundUser instanceof Professor);
        assertEquals("Computer Science", ((Professor) foundUser).getSpecialization());
        assertEquals(registerDate, foundUser.getRegisterDate());

        verify(preparedStatement).setInt(1, userId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void attUser_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        Professor professor = new Professor("Jane Updated", "jane.updated@example.com", "newHashedPassword", "Data Science");
        professor.setIdUser(20);
        LocalDateTime registerDate = LocalDateTime.now();
        professor.setRegisterDate(registerDate);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = userDAO.attUser(professor);

        // Assert
        assertTrue(result);

        verify(preparedStatement).setString(1, "Jane Updated");
        verify(preparedStatement).setString(2, "jane.updated@example.com");
        verify(preparedStatement).setString(3, "newHashedPassword");
        verify(preparedStatement).setString(4, "PROFESSOR");
        verify(preparedStatement).setString(5, "Data Science");
        verify(preparedStatement).setTimestamp(eq(6), any(Timestamp.class));
        verify(preparedStatement).setInt(7, 20);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void deleteUser_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        int userId = 10;
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = userDAO.deleteUser(userId);

        // Assert
        assertTrue(result);

        verify(preparedStatement).setInt(1, userId);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void searchByEmail_shouldReturnNullWhenUserNotFound() throws SQLException {
        // Arrange
        String email = "nonexistent@example.com";

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // Act
        User foundUser = userDAO.searchByEmail(email);

        // Assert
        assertNull(foundUser);

        verify(preparedStatement).setString(1, email);
        verify(preparedStatement).executeQuery();
    }
}