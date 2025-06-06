package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.ClassroomDAO;
import com.elearning.remoteensine.dao.CourseDAO;
import com.elearning.remoteensine.dao.UserDAO;
import com.elearning.remoteensine.model.Classroom;
import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.Professor;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.UserType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClassroomServiceTest {

    @Mock
    private ClassroomDAO classroomDAO;

    @Mock
    private CourseDAO courseDAO;

    @Mock
    private UserDAO userDAO;

    private ClassroomService classroomService;
    private Professor professor;
    private Course course;
    private User user;

    @BeforeEach
    void setUp() {
        classroomService = new ClassroomService(classroomDAO, courseDAO, userDAO);
        
        // Setup professor
        professor = new Professor("John Doe", "john@example.com", "password", "Computer Science");
        professor.setIdUser(1);
        
        // Setup course
        course = new Course("Java Programming", "Learn Java", professor, 99.99, "Programming", 40);
        course.setIdCourse(1);
        
        // Setup user
        user = new User("Jane Smith", "jane@example.com", "password", UserType.PROFESSOR);
        user.setIdUser(2);
    }

    @Test
    void addClassToCourse_shouldAddValidClassToCourse() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int courseId = 1;
        int professorId = 1;
        String title = "Introduction to Java";
        String description = "Basic Java concepts";
        String contentURL = "http://example.com/java-intro";
        int sequence = 1;
        
        when(courseDAO.searchCourseById(courseId)).thenReturn(course);
        
        Classroom expectedClassroom = new Classroom(courseId, title, description, contentURL, sequence);
        expectedClassroom.setClassroomId(1);
        when(classroomDAO.saveClassroom(any(Classroom.class))).thenReturn(expectedClassroom);
        
        // Act
        Classroom result = classroomService.addClassToCourse(courseId, professorId, title, description, contentURL, sequence);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getClassroomId());
        assertEquals(courseId, result.getCourseId());
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        assertEquals(contentURL, result.getContentURL());
        assertEquals(sequence, result.getSequence());
        
        verify(courseDAO).searchCourseById(courseId);
        verify(classroomDAO).saveClassroom(any(Classroom.class));
    }

    @Test
    void searchClass_shouldReturnClassroomWhenFound() throws SQLException {
        // Arrange
        int classroomId = 1;
        Classroom expectedClassroom = new Classroom(1, "Java Basics", "Introduction to Java", "http://example.com/java", 1);
        expectedClassroom.setClassroomId(classroomId);
        
        when(classroomDAO.searchById(classroomId)).thenReturn(expectedClassroom);
        
        // Act
        Classroom result = classroomService.searchClass(classroomId);
        
        // Assert
        assertNotNull(result);
        assertEquals(classroomId, result.getClassroomId());
        assertEquals("Java Basics", result.getTitle());
        assertEquals("Introduction to Java", result.getDescription());
        assertEquals("http://example.com/java", result.getContentURL());
        assertEquals(1, result.getSequence());
        
        verify(classroomDAO).searchById(classroomId);
    }

    @Test
    void listCoursesClasses_shouldReturnListOfClassrooms() throws SQLException, IllegalArgumentException {
        // Arrange
        int courseId = 1;
        List<Classroom> expectedClassrooms = new ArrayList<>();
        
        Classroom classroom1 = new Classroom(courseId, "Java Basics", "Introduction to Java", "http://example.com/java1", 1);
        classroom1.setClassroomId(1);
        
        Classroom classroom2 = new Classroom(courseId, "Java Advanced", "Advanced Java concepts", "http://example.com/java2", 2);
        classroom2.setClassroomId(2);
        
        expectedClassrooms.add(classroom1);
        expectedClassrooms.add(classroom2);
        
        when(courseDAO.searchCourseById(courseId)).thenReturn(course);
        when(classroomDAO.listClassesByCourse(courseId)).thenReturn(expectedClassrooms);
        
        // Act
        List<Classroom> result = classroomService.listCoursesClasses(courseId);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Java Basics", result.get(0).getTitle());
        assertEquals("Java Advanced", result.get(1).getTitle());
        
        verify(courseDAO).searchCourseById(courseId);
        verify(classroomDAO).listClassesByCourse(courseId);
    }

    @Test
    void updateClassesInfo_shouldUpdateClassInformation() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int classroomId = 1;
        int professorId = 1;
        String newTitle = "Updated Java Basics";
        String newDescription = "Updated Introduction to Java";
        String newContentURL = "http://example.com/updated-java";
        int newSequence = 2;
        
        Classroom existingClassroom = new Classroom(1, "Java Basics", "Introduction to Java", "http://example.com/java", 1);
        existingClassroom.setClassroomId(classroomId);
        
        when(classroomDAO.searchById(classroomId)).thenReturn(existingClassroom);
        when(courseDAO.searchCourseById(1)).thenReturn(course);
        when(classroomDAO.upddateClass(any(Classroom.class))).thenReturn(true);
        
        // Act
        boolean result = classroomService.updateClassesInfo(classroomId, professorId, newTitle, newDescription, newContentURL, newSequence);
        
        // Assert
        assertTrue(result);
        
        verify(classroomDAO).searchById(classroomId);
        verify(courseDAO).searchCourseById(1);
        verify(classroomDAO).upddateClass(any(Classroom.class));
    }

    @Test
    void deleteAula_shouldDeleteClassroom() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int classroomId = 1;
        int courseId = 1;
        int professorId = 1;
        
        Classroom classroomToDelete = new Classroom(courseId, "Java Basics", "Introduction to Java", "http://example.com/java", 1);
        classroomToDelete.setClassroomId(classroomId);
        
        when(classroomDAO.searchById(classroomId)).thenReturn(classroomToDelete);
        when(courseDAO.searchCourseById(courseId)).thenReturn(course);
        when(classroomDAO.deleteClass(classroomId)).thenReturn(true);
        
        // Act
        boolean result = classroomService.deleteAula(classroomId, courseId, professorId);
        
        // Assert
        assertTrue(result);
        
        verify(classroomDAO).searchById(classroomId);
        verify(courseDAO).searchCourseById(courseId);
        verify(classroomDAO).deleteClass(classroomId);
    }

    @Test
    void saveOrUpdateClass_shouldSaveNewClassroom() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int courseId = 1;
        int professorId = 1;
        
        Classroom newClassroom = new Classroom(courseId, "Java Basics", "Introduction to Java", "http://example.com/java", 1);
        Classroom savedClassroom = new Classroom(courseId, "Java Basics", "Introduction to Java", "http://example.com/java", 1);
        savedClassroom.setClassroomId(1);
        
        when(courseDAO.searchCourseById(courseId)).thenReturn(course);
        when(classroomDAO.saveClassroom(any(Classroom.class))).thenReturn(savedClassroom);
        
        // Act
        Classroom result = classroomService.saveOrUpdateClass(newClassroom, courseId, professorId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getClassroomId());
        assertEquals("Java Basics", result.getTitle());
        
        verify(courseDAO).searchCourseById(courseId);
        verify(classroomDAO).saveClassroom(any(Classroom.class));
    }

    @Test
    void saveOrUpdateClass_shouldUpdateExistingClassroom() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int courseId = 1;
        int professorId = 1;
        
        Classroom existingClassroom = new Classroom(courseId, "Java Basics", "Introduction to Java", "http://example.com/java", 1);
        existingClassroom.setClassroomId(1);
        
        when(courseDAO.searchCourseById(courseId)).thenReturn(course);
        when(classroomDAO.upddateClass(any(Classroom.class))).thenReturn(true);
        
        // Act
        Classroom result = classroomService.saveOrUpdateClass(existingClassroom, courseId, professorId);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getClassroomId());
        assertEquals("Java Basics", result.getTitle());
        
        verify(courseDAO).searchCourseById(courseId);
        verify(classroomDAO).upddateClass(any(Classroom.class));
    }

    @Test
    void addClassToCourse_shouldThrowExceptionForEmptyTitle() {
        // Arrange
        int courseId = 1;
        int professorId = 1;
        String title = "";
        String description = "Basic Java concepts";
        String contentURL = "http://example.com/java-intro";
        int sequence = 1;
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            classroomService.addClassToCourse(courseId, professorId, title, description, contentURL, sequence);
        });
        
        assertEquals("Class title cannot be empty.", exception.getMessage());
        
        // Verify no interactions with DAOs
        verifyNoInteractions(classroomDAO);
        verifyNoInteractions(courseDAO);
    }

    @Test
    void addClassToCourse_shouldThrowExceptionForNonexistentCourse() throws SQLException {
        // Arrange
        int courseId = 999; // Non-existent course ID
        int professorId = 1;
        String title = "Java Basics";
        String description = "Basic Java concepts";
        String contentURL = "http://example.com/java-intro";
        int sequence = 1;
        
        when(courseDAO.searchCourseById(courseId)).thenReturn(null);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            try {
                classroomService.addClassToCourse(courseId, professorId, title, description, contentURL, sequence);
            } catch (SQLException | IllegalAccessException e) {
                fail("Should not throw SQLException or IllegalAccessException but threw " + e);
            }
        });
        
        assertEquals("Course ID " + courseId + " not found.", exception.getMessage());
        
        try {
            verify(courseDAO).searchCourseById(courseId);
        } catch (SQLException e) {
            fail("Should not throw SQLException during verification");
        }
        verifyNoInteractions(classroomDAO);
    }
}