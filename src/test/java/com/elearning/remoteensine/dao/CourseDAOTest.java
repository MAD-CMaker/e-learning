package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.Professor;
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
public class CourseDAOTest {

    @Mock
    private DatabaseConnector databaseConnector;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private CourseDAO courseDAO;

    @BeforeEach
    void setUp() throws SQLException {
        courseDAO = new CourseDAO(databaseConnector);
        when(databaseConnector.getConnection()).thenReturn(connection);
    }

    @Test
    void saveCourse_shouldSaveAndReturnCourseWithId() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);

        Professor professor = new Professor();
        professor.setIdUser(1);

        Course course = new Course();
        course.setTitle("Java Programming");
        course.setDescription("Learn Java programming from scratch");
        course.setResponsibleProfessor(professor);
        course.setPrice(99.99);
        course.setPresentationVideo("http://example.com/video");
        course.setCategory("Programming");
        course.setHoursLoad(40);
        course.setCreationDate(LocalDateTime.now());

        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(10);

        // Act
        Course savedCourse = courseDAO.saveCourse(course);

        // Assert
        assertEquals(10, savedCourse.getIdCourse());
        assertEquals("Java Programming", savedCourse.getTitle());
        assertEquals("Learn Java programming from scratch", savedCourse.getDescription());
        assertEquals(1, savedCourse.getResponsibleProfessor().getIdUser());
        assertEquals(99.99, savedCourse.getPrice());
        assertEquals("http://example.com/video", savedCourse.getPresentationVideo());
        assertEquals("Programming", savedCourse.getCategory());
        assertEquals(40, savedCourse.getHoursLoad());

        verify(preparedStatement).setString(1, "Java Programming");
        verify(preparedStatement).setString(2, "Learn Java programming from scratch");
        verify(preparedStatement).setInt(3, 1);
        verify(preparedStatement).setDouble(4, 99.99);
        verify(preparedStatement).setString(5, "http://example.com/video");
        verify(preparedStatement).setString(6, "Programming");
        verify(preparedStatement).setInt(7, 40);
        verify(preparedStatement).setTimestamp(eq(8), any(Timestamp.class));
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).getGeneratedKeys();
    }

    @Test
    void searchCourseById_shouldReturnCourseWhenFound() throws SQLException {
        // Arrange
        int courseId = 10;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        
        when(resultSet.getInt("course_id")).thenReturn(courseId);
        when(resultSet.getString("title")).thenReturn("Java Programming");
        when(resultSet.getString("description")).thenReturn("Learn Java programming from scratch");
        when(resultSet.getInt("responsible_professor_id")).thenReturn(1);
        when(resultSet.getDouble("price")).thenReturn(99.99);
        when(resultSet.getString("video_presentation_url")).thenReturn("http://example.com/video");
        when(resultSet.getString("category")).thenReturn("Programming");
        when(resultSet.getInt("load_hour")).thenReturn(40);
        when(resultSet.getTimestamp("creation_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getTimestamp("update_date")).thenReturn(null);

        // Act
        Course foundCourse = courseDAO.searchCourseById(courseId);

        // Assert
        assertNotNull(foundCourse);
        assertEquals(courseId, foundCourse.getIdCourse());
        assertEquals("Java Programming", foundCourse.getTitle());
        assertEquals("Learn Java programming from scratch", foundCourse.getDescription());
        assertEquals(1, foundCourse.getResponsibleProfessor().getIdUser());
        assertEquals(99.99, foundCourse.getPrice());
        assertEquals("http://example.com/video", foundCourse.getPresentationVideo());
        assertEquals("Programming", foundCourse.getCategory());
        assertEquals(40, foundCourse.getHoursLoad());

        verify(preparedStatement).setInt(1, courseId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void listAllCourses_shouldReturnListOfCourses() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // Return true twice for two courses, then false

        // First course
        when(resultSet.getInt("course_id")).thenReturn(10, 11);
        when(resultSet.getString("title")).thenReturn("Java Programming", "Python Programming");
        when(resultSet.getString("description")).thenReturn("Learn Java", "Learn Python");
        when(resultSet.getInt("responsible_professor_id")).thenReturn(1, 2);
        when(resultSet.getDouble("price")).thenReturn(99.99, 89.99);
        when(resultSet.getString("video_presentation_url")).thenReturn("http://example.com/java", "http://example.com/python");
        when(resultSet.getString("category")).thenReturn("Programming", "Programming");
        when(resultSet.getInt("load_hour")).thenReturn(40, 30);
        when(resultSet.getTimestamp("creation_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getTimestamp("update_date")).thenReturn(null);

        // Act
        List<Course> courses = courseDAO.listAllCourses();

        // Assert
        assertNotNull(courses);
        assertEquals(2, courses.size());

        // First course
        assertEquals(10, courses.get(0).getIdCourse());
        assertEquals("Java Programming", courses.get(0).getTitle());
        assertEquals("Learn Java", courses.get(0).getDescription());
        assertEquals(1, courses.get(0).getResponsibleProfessor().getIdUser());
        assertEquals(99.99, courses.get(0).getPrice());
        assertEquals("http://example.com/java", courses.get(0).getPresentationVideo());
        assertEquals("Programming", courses.get(0).getCategory());
        assertEquals(40, courses.get(0).getHoursLoad());

        // Second course
        assertEquals(11, courses.get(1).getIdCourse());
        assertEquals("Python Programming", courses.get(1).getTitle());
        assertEquals("Learn Python", courses.get(1).getDescription());
        assertEquals(2, courses.get(1).getResponsibleProfessor().getIdUser());
        assertEquals(89.99, courses.get(1).getPrice());
        assertEquals("http://example.com/python", courses.get(1).getPresentationVideo());
        assertEquals("Programming", courses.get(1).getCategory());
        assertEquals(30, courses.get(1).getHoursLoad());

        verify(preparedStatement).executeQuery();
    }

    @Test
    void listCoursesByProfessor_shouldReturnListOfCourses() throws SQLException {
        // Arrange
        int professorId = 1;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, false); // Return true once for one course, then false

        when(resultSet.getInt("course_id")).thenReturn(10);
        when(resultSet.getString("title")).thenReturn("Java Programming");
        when(resultSet.getString("description")).thenReturn("Learn Java");
        when(resultSet.getInt("responsible_professor_id")).thenReturn(professorId);
        when(resultSet.getDouble("price")).thenReturn(99.99);
        when(resultSet.getString("video_presentation_url")).thenReturn("http://example.com/java");
        when(resultSet.getString("category")).thenReturn("Programming");
        when(resultSet.getInt("load_hour")).thenReturn(40);
        when(resultSet.getTimestamp("creation_date")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(resultSet.getTimestamp("update_date")).thenReturn(null);

        // Act
        List<Course> courses = courseDAO.listCoursesByProfessor(professorId);

        // Assert
        assertNotNull(courses);
        assertEquals(1, courses.size());

        assertEquals(10, courses.get(0).getIdCourse());
        assertEquals("Java Programming", courses.get(0).getTitle());
        assertEquals("Learn Java", courses.get(0).getDescription());
        assertEquals(professorId, courses.get(0).getResponsibleProfessor().getIdUser());
        assertEquals(99.99, courses.get(0).getPrice());
        assertEquals("http://example.com/java", courses.get(0).getPresentationVideo());
        assertEquals("Programming", courses.get(0).getCategory());
        assertEquals(40, courses.get(0).getHoursLoad());

        verify(preparedStatement).setInt(1, professorId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void attCourses_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        Professor professor = new Professor();
        professor.setIdUser(1);

        Course course = new Course();
        course.setIdCourse(10);
        course.setTitle("Updated Java Programming");
        course.setDescription("Updated Java description");
        course.setResponsibleProfessor(professor);
        course.setPrice(129.99);
        course.setPresentationVideo("http://example.com/updated-video");
        course.setCategory("Advanced Programming");
        course.setHoursLoad(50);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = courseDAO.attCourses(course);

        // Assert
        assertTrue(result);

        verify(preparedStatement).setString(1, "Updated Java Programming");
        verify(preparedStatement).setString(2, "Updated Java description");
        verify(preparedStatement).setInt(3, 1);
        verify(preparedStatement).setDouble(4, 129.99);
        verify(preparedStatement).setString(5, "http://example.com/updated-video");
        verify(preparedStatement).setString(6, "Advanced Programming");
        verify(preparedStatement).setInt(7, 50);
        verify(preparedStatement).setInt(8, 10);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void deleteCourse_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        int courseId = 10;
        
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = courseDAO.deleteCourse(courseId);

        // Assert
        assertTrue(result);

        verify(preparedStatement).setInt(1, courseId);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void saveCourse_shouldThrowExceptionWhenProfessorIsNull() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);

        Course course = new Course();
        course.setTitle("Java Programming");
        course.setDescription("Learn Java programming from scratch");
        course.setResponsibleProfessor(null); // Null professor
        course.setPrice(99.99);
        course.setPresentationVideo("http://example.com/video");
        course.setCategory("Programming");
        course.setHoursLoad(40);
        course.setCreationDate(LocalDateTime.now());

        // Act & Assert
        SQLException exception = assertThrows(SQLException.class, () -> {
            courseDAO.saveCourse(course);
        });
        
        assertTrue(exception.getMessage().contains("Responsible teacher cannot be null"));
        
        // Verify that no update was executed
        verify(preparedStatement, never()).executeUpdate();
    }

    @Test
    void attCourses_shouldThrowExceptionWhenProfessorIsNull() throws SQLException {
        // Arrange
        Course course = new Course();
        course.setIdCourse(10);
        course.setTitle("Updated Java Programming");
        course.setDescription("Updated Java description");
        course.setResponsibleProfessor(null); // Null professor
        course.setPrice(129.99);
        course.setPresentationVideo("http://example.com/updated-video");
        course.setCategory("Advanced Programming");
        course.setHoursLoad(50);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        // Act & Assert
        SQLException exception = assertThrows(SQLException.class, () -> {
            courseDAO.attCourses(course);
        });
        
        assertTrue(exception.getMessage().contains("Responsible teacher cannot be null"));
        
        // Verify that no update was executed
        verify(preparedStatement, never()).executeUpdate();
    }
}