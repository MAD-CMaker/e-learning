package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.Doubt;
import com.elearning.remoteensine.model.Professor;
import com.elearning.remoteensine.model.enums.DoubtStatus;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoubtDAOTest {

    @Mock
    private DatabaseConnector databaseConnector;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private DoubtDAO doubtDAO;

    @BeforeEach
    void setUp() throws SQLException {
        doubtDAO = new DoubtDAO(databaseConnector);
        when(databaseConnector.getConnection()).thenReturn(connection);
    }

    @Test
    void saveDoubt_shouldSaveAndReturnDoubtWithId() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);

        Doubt doubt = new Doubt();
        doubt.setCourseId(1);
        doubt.setStudentId(2);
        doubt.setTitle("Java Exception Handling");
        doubt.setDescription("How to handle exceptions in Java?");
        doubt.setCreationHour(LocalDateTime.now());
        doubt.setStatus(DoubtStatus.ABERTA);

        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(10);

        // Act
        Doubt savedDoubt = doubtDAO.saveDoubt(doubt);

        // Assert
        assertEquals(10, savedDoubt.getDoubtId());
        assertEquals(1, savedDoubt.getCourseId());
        assertEquals(2, savedDoubt.getStudentId());
        assertEquals("Java Exception Handling", savedDoubt.getTitle());
        assertEquals("How to handle exceptions in Java?", savedDoubt.getDescription());
        assertEquals(DoubtStatus.ABERTA, savedDoubt.getStatus());

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
        verify(preparedStatement).setString(3, "Java Exception Handling");
        verify(preparedStatement).setString(4, "How to handle exceptions in Java?");
        verify(preparedStatement).setTimestamp(eq(5), any(Timestamp.class));
        verify(preparedStatement).setString(6, DoubtStatus.ABERTA.name());
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).getGeneratedKeys();
    }

    @Test
    void searchDoubtById_shouldReturnDoubtWhenFound() throws SQLException {
        // Arrange
        int doubtId = 10;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        
        when(resultSet.getInt("doubt_id")).thenReturn(doubtId);
        when(resultSet.getInt("course_id")).thenReturn(1);
        when(resultSet.getInt("student_id")).thenReturn(2);
        when(resultSet.getString("title")).thenReturn("Java Exception Handling");
        when(resultSet.getString("description")).thenReturn("How to handle exceptions in Java?");
        when(resultSet.getTimestamp("creation_hour_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getString("answer")).thenReturn(null);
        when(resultSet.getInt("answer_professor_id")).thenReturn(0);
        when(resultSet.wasNull()).thenReturn(true);
        when(resultSet.getString("status")).thenReturn(DoubtStatus.ABERTA.name());

        // Act
        Doubt foundDoubt = doubtDAO.searchDoubtById(doubtId);

        // Assert
        assertNotNull(foundDoubt);
        assertEquals(doubtId, foundDoubt.getDoubtId());
        assertEquals(1, foundDoubt.getCourseId());
        assertEquals(2, foundDoubt.getStudentId());
        assertEquals("Java Exception Handling", foundDoubt.getTitle());
        assertEquals("How to handle exceptions in Java?", foundDoubt.getDescription());
        assertEquals(DoubtStatus.ABERTA, foundDoubt.getStatus());
        assertNull(foundDoubt.getProfessorId());

        verify(preparedStatement).setInt(1, doubtId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void lisCoursesDoubt_shouldReturnListOfDoubts() throws SQLException {
        // Arrange
        int courseId = 1;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // Return true twice for two doubts, then false

        // First doubt
        when(resultSet.getInt("doubt_id")).thenReturn(10, 11);
        when(resultSet.getInt("course_id")).thenReturn(courseId, courseId);
        when(resultSet.getInt("student_id")).thenReturn(2, 3);
        when(resultSet.getString("title")).thenReturn("Java Exception Handling", "Spring Boot Question");
        when(resultSet.getString("description")).thenReturn("How to handle exceptions?", "How to configure Spring Boot?");
        when(resultSet.getTimestamp("creation_hour_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getString("answer")).thenReturn(null);
        when(resultSet.getInt("answer_professor_id")).thenReturn(0);
        when(resultSet.wasNull()).thenReturn(true);
        when(resultSet.getString("status")).thenReturn(DoubtStatus.ABERTA.name(), DoubtStatus.ABERTA.name());

        // Act
        List<Doubt> doubts = doubtDAO.lisCoursesDoubt(courseId);

        // Assert
        assertNotNull(doubts);
        assertEquals(2, doubts.size());

        // First doubt
        assertEquals(10, doubts.get(0).getDoubtId());
        assertEquals(courseId, doubts.get(0).getCourseId());
        assertEquals(2, doubts.get(0).getStudentId());
        assertEquals("Java Exception Handling", doubts.get(0).getTitle());
        assertEquals("How to handle exceptions?", doubts.get(0).getDescription());
        assertEquals(DoubtStatus.ABERTA, doubts.get(0).getStatus());

        // Second doubt
        assertEquals(11, doubts.get(1).getDoubtId());
        assertEquals(courseId, doubts.get(1).getCourseId());
        assertEquals(3, doubts.get(1).getStudentId());
        assertEquals("Spring Boot Question", doubts.get(1).getTitle());
        assertEquals("How to configure Spring Boot?", doubts.get(1).getDescription());
        assertEquals(DoubtStatus.ABERTA, doubts.get(1).getStatus());

        verify(preparedStatement).setInt(1, courseId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void listStudentsDoubts_shouldReturnListOfDoubts() throws SQLException {
        // Arrange
        int studentId = 2;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false); // Return true once for one doubt, then false

        when(resultSet.getInt("doubt_id")).thenReturn(10);
        when(resultSet.getInt("course_id")).thenReturn(1);
        when(resultSet.getInt("student_id")).thenReturn(studentId);
        when(resultSet.getString("title")).thenReturn("Java Exception Handling");
        when(resultSet.getString("description")).thenReturn("How to handle exceptions?");
        when(resultSet.getTimestamp("creation_hour_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getString("answer")).thenReturn(null);
        when(resultSet.getInt("answer_professor_id")).thenReturn(0);
        when(resultSet.wasNull()).thenReturn(true);
        when(resultSet.getString("status")).thenReturn(DoubtStatus.ABERTA.name());

        // Act
        List<Doubt> doubts = doubtDAO.listStudentsDoubts(studentId);

        // Assert
        assertNotNull(doubts);
        assertEquals(1, doubts.size());

        assertEquals(10, doubts.get(0).getDoubtId());
        assertEquals(1, doubts.get(0).getCourseId());
        assertEquals(studentId, doubts.get(0).getStudentId());
        assertEquals("Java Exception Handling", doubts.get(0).getTitle());
        assertEquals("How to handle exceptions?", doubts.get(0).getDescription());
        assertEquals(DoubtStatus.ABERTA, doubts.get(0).getStatus());

        verify(preparedStatement).setInt(1, studentId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void updateDouts_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        Professor professor = new Professor();
        professor.setIdUser(3);

        Doubt doubt = new Doubt();
        doubt.setDoubtId(10);
        doubt.setAnswer("You should use try-catch blocks to handle exceptions in Java.");
        doubt.setAnswerHour(LocalDateTime.now());
        doubt.setProfessorId(professor.getIdUser());
        doubt.setStatus(DoubtStatus.RESPONDIDA);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = doubtDAO.updateDouts(doubt);

        // Assert
        assertTrue(result);

        verify(preparedStatement).setString(1, "You should use try-catch blocks to handle exceptions in Java.");
        verify(preparedStatement).setTimestamp(eq(2), any(Timestamp.class));
        verify(preparedStatement).setInt(3, 3);
        verify(preparedStatement).setString(4, DoubtStatus.RESPONDIDA.name());
        verify(preparedStatement).setInt(5, 10);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void deleteDoubt_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        int doubtId = 10;
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = doubtDAO.deleteDoubt(doubtId);

        // Assert
        assertTrue(result);

        verify(preparedStatement).setInt(1, doubtId);
        verify(preparedStatement).executeUpdate();
    }
}