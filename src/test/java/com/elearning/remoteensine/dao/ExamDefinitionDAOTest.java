
package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.ExamDefinition;
import com.elearning.remoteensine.model.GradeStudent;
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
public class ExamDefinitionDAOTest {

    @Mock
    private DatabaseConnector databaseConnector;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private ExamDefinitionDAO examDefinitionDAO;

    @BeforeEach
    void setUp() throws SQLException {
        examDefinitionDAO = new ExamDefinitionDAO(databaseConnector);
        when(databaseConnector.getConnection()).thenReturn(connection);
    }

    @Test
    void saveExamDefinition_shouldSaveAndReturnExamDefinitionWithId() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);

        ExamDefinition examDefinition = new ExamDefinition();
        examDefinition.setIdCourse(1);
        examDefinition.setTitle("Final Exam");
        examDefinition.setDescription("Comprehensive final exam");
        LocalDateTime creationDate = LocalDateTime.now();
        examDefinition.setCreationData(creationDate);
        examDefinition.setUpdateData(creationDate);

        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(10);

        // Act
        ExamDefinition savedExamDefinition = examDefinitionDAO.saveExamDefinition(examDefinition);

        // Assert
        assertEquals(10, savedExamDefinition.getIdDefinitionExam());
        assertEquals(1, savedExamDefinition.getIdCourse());
        assertEquals("Final Exam", savedExamDefinition.getTitle());
        assertEquals("Comprehensive final exam", savedExamDefinition.getDescription());
        assertEquals(creationDate, savedExamDefinition.getCreationData());
        assertEquals(creationDate, savedExamDefinition.getUpdateData());

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setString(2, "Final Exam");
        verify(preparedStatement).setString(3, "Comprehensive final exam");
        verify(preparedStatement).setTimestamp(eq(4), any(Timestamp.class));
        verify(preparedStatement).setTimestamp(eq(5), any(Timestamp.class));
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).getGeneratedKeys();
    }

    @Test
    void findExamDefinitionById_shouldReturnExamDefinitionWhenFound() throws SQLException {
        // Arrange
        int examDefinitionId = 10;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);

        when(resultSet.getInt("id_exam_definition")).thenReturn(examDefinitionId);
        when(resultSet.getInt("id_course")).thenReturn(1);
        when(resultSet.getString("title")).thenReturn("Final Exam");
        when(resultSet.getString("description")).thenReturn("Comprehensive final exam");
        when(resultSet.getBoolean("published")).thenReturn(true);
        LocalDateTime creationDate = LocalDateTime.now();
        when(resultSet.getTimestamp("creation_date")).thenReturn(Timestamp.valueOf(creationDate));
        when(resultSet.getTimestamp("update_date")).thenReturn(Timestamp.valueOf(creationDate));

        // Act
        ExamDefinition foundExamDefinition = examDefinitionDAO.findExamDefinitionById(examDefinitionId);

        // Assert
        assertNotNull(foundExamDefinition);
        assertEquals(examDefinitionId, foundExamDefinition.getIdDefinitionExam());
        assertEquals(1, foundExamDefinition.getIdCourse());
        assertEquals("Final Exam", foundExamDefinition.getTitle());
        assertEquals("Comprehensive final exam", foundExamDefinition.getDescription());
        assertTrue(foundExamDefinition.isPublished());
        assertEquals(creationDate, foundExamDefinition.getCreationData());
        assertEquals(creationDate, foundExamDefinition.getUpdateData());

        verify(preparedStatement).setInt(1, examDefinitionId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void findExamDefinitionsByCourseId_shouldReturnListOfExamDefinitions() throws SQLException {
        // Arrange
        int courseId = 1;
        boolean onlyPublished = true;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // Return true twice for two exam definitions, then false

        // First exam definition
        when(resultSet.getInt("id_exam_definition")).thenReturn(10, 11);
        when(resultSet.getInt("id_course")).thenReturn(courseId, courseId);
        when(resultSet.getString("title")).thenReturn("Final Exam", "Midterm Exam");
        when(resultSet.getString("description")).thenReturn("Comprehensive final exam", "Midterm assessment");
        when(resultSet.getBoolean("published")).thenReturn(true, true);

        LocalDateTime creationDate1 = LocalDateTime.now().minusDays(10);
        LocalDateTime creationDate2 = LocalDateTime.now().minusDays(5);

        when(resultSet.getTimestamp("creation_date")).thenReturn(
                Timestamp.valueOf(creationDate1),
                Timestamp.valueOf(creationDate2)
        );

        when(resultSet.getTimestamp("update_date")).thenReturn(
                Timestamp.valueOf(creationDate1),
                Timestamp.valueOf(creationDate2)
        );

        // Act
        List<ExamDefinition> examDefinitions = examDefinitionDAO.findExamDefinitionsByCourseId(courseId, onlyPublished);

        // Assert
        assertNotNull(examDefinitions);
        assertEquals(2, examDefinitions.size());

        // First exam definition
        assertEquals(10, examDefinitions.get(0).getIdDefinitionExam());
        assertEquals(courseId, examDefinitions.get(0).getIdCourse());
        assertEquals("Final Exam", examDefinitions.get(0).getTitle());
        assertEquals("Comprehensive final exam", examDefinitions.get(0).getDescription());
        assertTrue(examDefinitions.get(0).isPublished());
        assertEquals(creationDate1, examDefinitions.get(0).getCreationData());

        // Second exam definition
        assertEquals(11, examDefinitions.get(1).getIdDefinitionExam());
        assertEquals(courseId, examDefinitions.get(1).getIdCourse());
        assertEquals("Midterm Exam", examDefinitions.get(1).getTitle());
        assertEquals("Midterm assessment", examDefinitions.get(1).getDescription());
        assertTrue(examDefinitions.get(1).isPublished());
        assertEquals(creationDate2, examDefinitions.get(1).getCreationData());

        verify(preparedStatement).setInt(1, courseId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void findExamDefinitionsNotSubmittedByCourseId_shouldReturnListOfNotSubmittedExamDefinitions() throws SQLException {
        // Arrange
        int courseId = 1;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false); // Return true once for one exam definition, then false

        // Exam definition
        when(resultSet.getInt("id_exam_definition")).thenReturn(10);
        when(resultSet.getInt("id_course")).thenReturn(courseId);
        when(resultSet.getString("title")).thenReturn("Final Exam");
        when(resultSet.getString("description")).thenReturn("Comprehensive final exam");
        when(resultSet.getBoolean("published")).thenReturn(true);

        LocalDateTime creationDate = LocalDateTime.now().minusDays(10);
        when(resultSet.getTimestamp("creation_date")).thenReturn(Timestamp.valueOf(creationDate));
        when(resultSet.getTimestamp("update_date")).thenReturn(Timestamp.valueOf(creationDate));

        // Act
        List<ExamDefinition> examDefinitions = examDefinitionDAO.findExamDefinitionsNotSubmittedByCourseId(courseId);

        // Assert
        assertNotNull(examDefinitions);
        assertEquals(1, examDefinitions.size());

        // Exam definition
        assertEquals(10, examDefinitions.get(0).getIdDefinitionExam());
        assertEquals(courseId, examDefinitions.get(0).getIdCourse());
        assertEquals("Final Exam", examDefinitions.get(0).getTitle());
        assertEquals("Comprehensive final exam", examDefinitions.get(0).getDescription());
        assertTrue(examDefinitions.get(0).isPublished());
        assertEquals(creationDate, examDefinitions.get(0).getCreationData());

        verify(preparedStatement).setInt(1, courseId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void findGradeExamDefitionByCourse_shouldReturnListOfGradeStudents() throws SQLException {
        // Arrange
        int courseId = 1;
        int studentId = 5;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false); // Return true once for one grade, then false

        // Grade
        when(resultSet.getInt("id_exam_definition")).thenReturn(10);
        when(resultSet.getString("title")).thenReturn("Final Exam");
        when(resultSet.getString("description")).thenReturn("Comprehensive final exam");
        when(resultSet.getBoolean("published")).thenReturn(true);
        when(resultSet.getInt("exam_id")).thenReturn(20);
        when(resultSet.getInt("student_grade")).thenReturn(85);
        when(resultSet.getInt("total_exam_value")).thenReturn(100);

        LocalDateTime creationDate = LocalDateTime.now().minusDays(10);
        when(resultSet.getTimestamp("creation_date")).thenReturn(Timestamp.valueOf(creationDate));

        // Act
        List<GradeStudent> grades = examDefinitionDAO.findGradeExamDefitionByCourse(courseId, studentId);

        // Assert
        assertNotNull(grades);
        assertEquals(1, grades.size());

        // Grade
        assertEquals(10, grades.get(0).getIdDefinitionExam());
        assertEquals("Final Exam", grades.get(0).getTitle());
        assertEquals("Comprehensive final exam", grades.get(0).getDescription());
        assertTrue(grades.get(0).isPublished());
        assertEquals(20, grades.get(0).getExamId());
        assertEquals(85, grades.get(0).getGrade());
        assertEquals(100, grades.get(0).getTotalValueExam());
        assertEquals(creationDate, grades.get(0).getCreationDate());

        verify(preparedStatement).setInt(1, courseId);
        verify(preparedStatement).setInt(2, studentId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void updateExamDefinition_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        ExamDefinition examDefinition = new ExamDefinition();
        examDefinition.setIdDefinitionExam(10);
        examDefinition.setTitle("Updated Exam");
        examDefinition.setDescription("Updated description");
        examDefinition.setPublished(true);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = examDefinitionDAO.updateExamDefinition(examDefinition);

        // Assert
        assertTrue(result);

        verify(preparedStatement).setString(1, "Updated Exam");
        verify(preparedStatement).setString(2, "Updated description");
        verify(preparedStatement).setBoolean(3, true);
        verify(preparedStatement).setInt(4, 10);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void deleteExamDefinition_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        int examDefinitionId = 10;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = examDefinitionDAO.deleteExamDefinition(examDefinitionId);

        // Assert
        assertTrue(result);

        verify(preparedStatement).setInt(1, examDefinitionId);
        verify(preparedStatement).executeUpdate();
    }
}