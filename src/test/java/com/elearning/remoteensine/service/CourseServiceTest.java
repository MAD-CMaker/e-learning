package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.CourseDAO;
import com.elearning.remoteensine.dao.UserDAO;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CourseServiceTest {

    @Mock
    private CourseDAO courseDAO;

    @Mock
    private UserDAO userDAO;

    private CourseService courseService;
    private Professor professor;
    private Course course;
    private User regularUser;

    @BeforeEach
    void setUp() {
        courseService = new CourseService(courseDAO, userDAO);
        
        // Setup professor
        professor = new Professor("John Doe", "john@example.com", "password", "Computer Science");
        professor.setIdUser(1);
        
        // Setup course
        course = new Course("Java Programming", "Learn Java", professor, 99.99, "Programming", 40);
        course.setIdCourse(1);
        
        // Setup regular user (non-professor)
        regularUser = new User("Jane Smith", "jane@example.com", "password", UserType.STUDENT);
        regularUser.setIdUser(2);
    }

    @Test
    void createCourse_withValidData_shouldCreateCourse() throws SQLException {
        // Arrange
        String title = "Java Programming";
        String description = "Learn Java";
        int professorId = 1;
        double price = 99.99;
        String category = "Programming";
        int loadHour = 40;
        String presentationVideoURL = "https://example.com/video";
        
        when(userDAO.searchById(professorId)).thenReturn(professor);
        when(courseDAO.saveCourse(any(Course.class))).thenReturn(course);
        
        // Act
        Course result = courseService.createCourse(title, description, professorId, price, category, loadHour, presentationVideoURL);
        
        // Assert
        assertNotNull(result);
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        assertEquals(professor, result.getResponsibleProfessor());
        assertEquals(price, result.getPrice());
        assertEquals(category, result.getCategory());
        assertEquals(loadHour, result.getHoursLoad());
//        assertEquals(presentationVideoURL, result.getPresentationVideo());
        
        verify(userDAO).searchById(professorId);
        verify(courseDAO).saveCourse(any(Course.class));
    }
    
    @Test
    void createCourse_withEmptyTitle_shouldThrowException() {
        // Arrange
        String title = "";
        String description = "Learn Java programming from scratch";
        int professorId = 1;
        double price = 99.99;
        String category = "Programming";
        int loadHour = 40;
        String presentationVideoURL = "https://example.com/video";
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            try {
                courseService.createCourse(title, description, professorId, price, category, loadHour, presentationVideoURL);
            } catch (SQLException e) {
                fail("Should not throw SQLException but threw " + e);
            }
        });
        
        assertEquals("\nCourse title cannot be empty.", exception.getMessage());
        
        verifyNoInteractions(courseDAO);
        verifyNoInteractions(userDAO);
    }
    
    @Test
    void createCourse_withInvalidProfessor_shouldThrowException() throws SQLException {
        // Arrange
        String title = "Java Programming";
        String description = "Learn Java programming from scratch";
        int professorId = 2; // ID of a non-professor user
        double price = 99.99;
        String category = "Programming";
        int loadHour = 40;
        String presentationVideoURL = "https://example.com/video";
        
        when(userDAO.searchById(professorId)).thenReturn(regularUser); // Return a non-professor user
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            try {
                courseService.createCourse(title, description, professorId, price, category, loadHour, presentationVideoURL);
            } catch (SQLException e) {
                fail("Should not throw SQLException but threw " + e);
            }
        });
        
        assertTrue(exception.getMessage().contains("não encontrado ou não é um professor"));
        
        verify(userDAO).searchById(professorId);
        verifyNoInteractions(courseDAO);
    }
    
    @Test
    void searchCourseByIdComplete_shouldReturnCourseWithProfessor() throws SQLException {
        // Arrange
        int courseId = 1;
        
        when(courseDAO.searchCourseById(courseId)).thenReturn(course);
        when(userDAO.searchById(professor.getIdUser())).thenReturn(professor);
        
        // Act
        Course result = courseService.searchCourseByIdComplete(courseId);
        
        // Assert
        assertNotNull(result);
        assertEquals(courseId, result.getIdCourse());
        assertNotNull(result.getResponsibleProfessor());
        assertEquals(professor.getIdUser(), result.getResponsibleProfessor().getIdUser());
        
        verify(courseDAO).searchCourseById(courseId);
        verify(userDAO).searchById(professor.getIdUser());
    }
    
    @Test
    void listAllCoursesWithProfessors_shouldReturnCoursesWithProfessors() throws SQLException {
        // Arrange
        List<Course> courses = new ArrayList<>();
        Course course1 = new Course("Java Programming", "Learn Java", professor, 99.99, "Programming", 40);
        course1.setIdCourse(1);
        
        Course course2 = new Course("Python Programming", "Learn Python", professor, 89.99, "Programming", 35);
        course2.setIdCourse(2);
        
        courses.add(course1);
        courses.add(course2);
        
        when(courseDAO.listAllCourses()).thenReturn(courses);
        when(userDAO.searchById(professor.getIdUser())).thenReturn(professor);
        
        // Act
        List<Course> result = courseService.listAllCoursesWithProfessors();
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Java Programming", result.get(0).getTitle());
        assertEquals("Python Programming", result.get(1).getTitle());
        assertEquals(professor, result.get(0).getResponsibleProfessor());
        assertEquals(professor, result.get(1).getResponsibleProfessor());
        
        verify(courseDAO).listAllCourses();
        verify(userDAO, times(2)).searchById(professor.getIdUser());
    }
    
    @Test
    void listCoursesOfProfessor_shouldReturnProfessorCourses() throws SQLException {
        // Arrange
        int professorId = 1;
        List<Course> professorCourses = new ArrayList<>();
        
        Course course1 = new Course("Java Programming", "Learn Java", professor, 99.99, "Programming", 40);
        course1.setIdCourse(1);
        
        Course course2 = new Course("Python Programming", "Learn Python", professor, 89.99, "Programming", 35);
        course2.setIdCourse(2);
        
        professorCourses.add(course1);
        professorCourses.add(course2);
        
        when(userDAO.searchById(professorId)).thenReturn(professor);
        when(courseDAO.listCoursesByProfessor(professorId)).thenReturn(professorCourses);
        
        // Act
        List<Course> result = courseService.listCoursesOfProfessor(professorId);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Java Programming", result.get(0).getTitle());
        assertEquals("Python Programming", result.get(1).getTitle());
        assertEquals(professor, result.get(0).getResponsibleProfessor());
        assertEquals(professor, result.get(1).getResponsibleProfessor());
        
        verify(userDAO).searchById(professorId);
        verify(courseDAO).listCoursesByProfessor(professorId);
    }
    
    @Test
    void attCourseInfo_shouldUpdateCourseInformation() throws SQLException, IllegalAccessException {
        // Arrange
        int courseId = 1;
        int professorId = 1;
        String newTitle = "Updated Java Programming";
        String newDescription = "Updated Java course description";
        double newPrice = 129.99;
        String newCategory = "Advanced Programming";
        int newLoadHour = 50;
        String newPresentationVideoURL = "https://example.com/updated-video";
        
        when(courseDAO.searchCourseById(courseId)).thenReturn(course);
        when(courseDAO.attCourses(any(Course.class))).thenReturn(true);
        
        // Act
        boolean result = courseService.attCourseInfo(courseId, newTitle, newDescription, newPrice, 
                                                    newCategory, newLoadHour, newPresentationVideoURL, professorId);
        
        // Assert
        assertTrue(result);
        
        verify(courseDAO).searchCourseById(courseId);
        verify(courseDAO).attCourses(any(Course.class));
    }
    
    @Test
    void removeCourse_shouldDeleteCourse() throws SQLException, IllegalAccessException {
        // Arrange
        int courseId = 1;
        int professorId = 1;
        
        when(courseDAO.searchCourseById(courseId)).thenReturn(course);
        when(courseDAO.deleteCourse(courseId)).thenReturn(true);
        
        // Act
        boolean result = courseService.removeCourse(courseId, professorId);
        
        // Assert
        assertTrue(result);
        
        verify(courseDAO).searchCourseById(courseId);
        verify(courseDAO).deleteCourse(courseId);
    }
}