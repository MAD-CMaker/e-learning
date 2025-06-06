package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.CourseDAO;
import com.elearning.remoteensine.dao.DoubtDAO;
import com.elearning.remoteensine.dao.UserDAO;
import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.Doubt;
import com.elearning.remoteensine.model.Professor;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.DoubtStatus;
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
public class DoubtServiceTest {

    @Mock
    private DoubtDAO doubtDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private CourseDAO courseDAO;

    private DoubtService doubtService;
    private User student;
    private Professor professor;
    private Course course;
    private Doubt doubt;

    @BeforeEach
    void setUp() {
        doubtService = new DoubtService(doubtDAO, userDAO, courseDAO);

        // Setup student
        student = new User("John Student", "john@student.com", "password", UserType.STUDENT);
        student.setIdUser(1);

        // Setup professor
        professor = new Professor("Jane Professor", "jane@professor.com", "password", "Computer Science");
        professor.setIdUser(2);

        // Setup course with professor
        course = new Course("Java Programming", "Learn Java", professor, 99.99, "Programming", 40);
        course.setIdCourse(1);

        // Setup doubt
        doubt = new Doubt(1, 1, "Java Question", "How does inheritance work?", "John Student");
        doubt.setDoubtId(1);
        doubt.setStatus(DoubtStatus.ABERTA);
    }

    @Test
    void createDoubt_shouldCreateDoubtSuccessfully() throws SQLException {
        // Arrange
        int courseId = 1;
        int studentId = 1;
        String title = "Java Question";
        String description = "How does inheritance work?";

        when(courseDAO.searchCourseById(courseId)).thenReturn(course);
        when(userDAO.searchById(studentId)).thenReturn(student);
        when(doubtDAO.saveDoubt(any(Doubt.class))).thenReturn(doubt);

        // Act
        Doubt result = doubtService.createDoubt(courseId, studentId, title, description);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getDoubtId());
        assertEquals(courseId, result.getCourseId());
        assertEquals(studentId, result.getStudentId());
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        assertEquals(DoubtStatus.ABERTA, result.getStatus());

        verify(courseDAO).searchCourseById(courseId);
        verify(userDAO).searchById(studentId);
        verify(doubtDAO).saveDoubt(any(Doubt.class));
    }

    @Test
    void answerDoubt_shouldAnswerDoubtSuccessfully() throws SQLException, IllegalAccessException {
        // Arrange
        int doubtId = 1;
        int professorId = 2;
        String answerText = "Inheritance is a mechanism where a new class inherits properties and behaviors from an existing class.";

        when(doubtDAO.searchDoubtById(doubtId)).thenReturn(doubt);
        when(userDAO.searchById(professorId)).thenReturn(professor);
        when(courseDAO.searchCourseById(doubt.getCourseId())).thenReturn(course);
        when(doubtDAO.updateDouts(any(Doubt.class))).thenReturn(true);

        // Act
        Doubt result = doubtService.answerDoubt(doubtId, professorId, answerText);

        // Assert
        assertNotNull(result);
        assertEquals(doubtId, result.getDoubtId());
        assertEquals(answerText, result.getAnswer());
        assertEquals(professorId, result.getProfessorId());
        assertEquals(DoubtStatus.RESPONDIDA, result.getStatus());
        assertNotNull(result.getAnswerHour());

        verify(doubtDAO).searchDoubtById(doubtId);
        verify(userDAO).searchById(professorId);
        verify(courseDAO).searchCourseById(doubt.getCourseId());
        verify(doubtDAO).updateDouts(any(Doubt.class));
    }

    @Test
    void closeDoubts_shouldCloseDoubtSuccessfully() throws SQLException, IllegalAccessException {
        // Arrange
        int doubtId = 1;
        int studentId = 1; // Student who created the doubt

        when(doubtDAO.searchDoubtById(doubtId)).thenReturn(doubt);
        when(userDAO.searchById(studentId)).thenReturn(student);
        when(doubtDAO.updateDouts(any(Doubt.class))).thenReturn(true);

        // Act
        boolean result = doubtService.closeDoubts(doubtId, studentId);

        // Assert
        assertTrue(result);
        assertEquals(DoubtStatus.FECHADA, doubt.getStatus());

        verify(doubtDAO).searchDoubtById(doubtId);
        verify(userDAO).searchById(studentId);
        verify(doubtDAO).updateDouts(any(Doubt.class));
    }

    @Test
    void listCourseDoubts_shouldReturnListOfDoubts() throws SQLException {
        // Arrange
        int courseId = 1;
        List<Doubt> expectedDoubts = new ArrayList<>();
        expectedDoubts.add(doubt);

        Doubt anotherDoubt = new Doubt(courseId, 3, "Another Question", "What is polymorphism?", "Another Student");
        anotherDoubt.setDoubtId(2);
        expectedDoubts.add(anotherDoubt);

        when(courseDAO.searchCourseById(courseId)).thenReturn(course);
        when(doubtDAO.lisCoursesDoubt(courseId)).thenReturn(expectedDoubts);

        // Act
        List<Doubt> result = doubtService.listCourseDoubts(courseId);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Java Question", result.get(0).getTitle());
        assertEquals("Another Question", result.get(1).getTitle());

        verify(courseDAO).searchCourseById(courseId);
        verify(doubtDAO).lisCoursesDoubt(courseId);
    }

    @Test
    void createDoubt_shouldThrowExceptionForEmptyTitle() {
        // Arrange
        int courseId = 1;
        int studentId = 1;
        String title = "";
        String description = "How does inheritance work?";

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            doubtService.createDoubt(courseId, studentId, title, description);
        });

        assertEquals("Question title and description cannot be empty.", exception.getMessage());
        
        // Verify no interactions with DAOs for saving
        verifyNoInteractions(doubtDAO);
    }
}