package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.Classroom;
import com.elearning.remoteensine.util.DatabaseConnector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClassroomDAOTest {

    @Mock
    private DatabaseConnector databaseConnector;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private ClassroomDAO classroomDAO;

    @BeforeEach
    void setUp() throws SQLException {
        classroomDAO = new ClassroomDAO(databaseConnector);
        when(databaseConnector.getConnection()).thenReturn(connection);
    }

    @Test
    void saveClassroom_shouldSaveAndReturnClassroomWithId() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);

        Classroom classroom = new Classroom(1, "Test Title", "Test Description", "http://test.url", 1);
        
        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(10);
        
        // Act
        Classroom savedClassroom = classroomDAO.saveClassroom(classroom);
        
        // Assert
        assertEquals(10, savedClassroom.getClassroomId());
        assertEquals("Test Title", savedClassroom.getTitle());
        assertEquals("Test Description", savedClassroom.getDescription());
        assertEquals("http://test.url", savedClassroom.getContentURL());
        assertEquals(1, savedClassroom.getSequence());
        assertEquals(1, savedClassroom.getCourseId());
        
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setString(2, "Test Title");
        verify(preparedStatement).setString(3, "Test Description");
        verify(preparedStatement).setString(4, "http://test.url");
        verify(preparedStatement).setInt(5, 1);
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).getGeneratedKeys();
    }

    @Test
    void searchById_shouldReturnClassroomWhenFound() throws SQLException {
        // Arrange
        int classroomId = 10;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("classroom_id")).thenReturn(classroomId);
        when(resultSet.getInt("course_id")).thenReturn(1);
        when(resultSet.getString("title")).thenReturn("Test Title");
        when(resultSet.getString("description")).thenReturn("Test Description");
        when(resultSet.getString("content_url")).thenReturn("http://test.url");
        when(resultSet.getInt("sequence")).thenReturn(1);
        
        // Act
        Classroom foundClassroom = classroomDAO.searchById(classroomId);
        
        // Assert
        assertNotNull(foundClassroom);
        assertEquals(classroomId, foundClassroom.getClassroomId());
        assertEquals(1, foundClassroom.getCourseId());
        assertEquals("Test Title", foundClassroom.getTitle());
        assertEquals("Test Description", foundClassroom.getDescription());
        assertEquals("http://test.url", foundClassroom.getContentURL());
        assertEquals(1, foundClassroom.getSequence());
        
        verify(preparedStatement).setInt(1, classroomId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void listClassesByCourse_shouldReturnListOfClassrooms() throws SQLException {
        // Arrange
        int courseId = 1;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // Return true twice for two classrooms, then false
        
        // First classroom
        when(resultSet.getInt("classroom_id")).thenReturn(10, 11);
        when(resultSet.getInt("course_id")).thenReturn(courseId, courseId);
        when(resultSet.getString("title")).thenReturn("Title 1", "Title 2");
        when(resultSet.getString("description")).thenReturn("Description 1", "Description 2");
        when(resultSet.getString("content_url")).thenReturn("http://url1", "http://url2");
        when(resultSet.getInt("sequence")).thenReturn(1, 2);
        
        // Act
        List<Classroom> classrooms = classroomDAO.listClassesByCourse(courseId);
        
        // Assert
        assertNotNull(classrooms);
        assertEquals(2, classrooms.size());
        
        // First classroom
        assertEquals(10, classrooms.get(0).getClassroomId());
        assertEquals(courseId, classrooms.get(0).getCourseId());
        assertEquals("Title 1", classrooms.get(0).getTitle());
        assertEquals("Description 1", classrooms.get(0).getDescription());
        assertEquals("http://url1", classrooms.get(0).getContentURL());
        assertEquals(1, classrooms.get(0).getSequence());
        
        // Second classroom
        assertEquals(11, classrooms.get(1).getClassroomId());
        assertEquals(courseId, classrooms.get(1).getCourseId());
        assertEquals("Title 2", classrooms.get(1).getTitle());
        assertEquals("Description 2", classrooms.get(1).getDescription());
        assertEquals("http://url2", classrooms.get(1).getContentURL());
        assertEquals(2, classrooms.get(1).getSequence());
        
        verify(preparedStatement).setInt(1, courseId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void upddateClass_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        Classroom classroom = new Classroom(1, "Updated Title", "Updated Description", "http://updated.url", 2);
        classroom.setClassroomId(10);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        when(preparedStatement.executeUpdate()).thenReturn(1);
        
        // Act
        boolean result = classroomDAO.upddateClass(classroom);
        
        // Assert
        assertTrue(result);
        
        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setString(2, "Updated Title");
        verify(preparedStatement).setString(3, "Updated Description");
        verify(preparedStatement).setString(4, "http://updated.url");
        verify(preparedStatement).setInt(5, 2);
        verify(preparedStatement).setInt(6, 10);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void deleteClass_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        int classroomId = 10;
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);
        
        // Act
        boolean result = classroomDAO.deleteClass(classroomId);
        
        // Assert
        assertTrue(result);
        
        verify(preparedStatement).setInt(1, classroomId);
        verify(preparedStatement).executeUpdate();
    }
}