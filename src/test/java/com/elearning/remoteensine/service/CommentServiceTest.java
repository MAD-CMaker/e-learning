package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.CommentDAO;
import com.elearning.remoteensine.dao.CourseDAO;
import com.elearning.remoteensine.dao.UserDAO;
import com.elearning.remoteensine.dao.EnrollDAO;
import com.elearning.remoteensine.model.Comment;
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
public class CommentServiceTest {

    @Mock
    private CommentDAO commentDAO;

    @Mock
    private UserDAO userDAO;

    @Mock
    private CourseDAO courseDAO;

    @Mock
    private EnrollDAO enrollDAO;

    private CommentService commentService;
    private User student;
    private Course course;
    private Professor professor;
    private Comment comment;

    @BeforeEach
    void setUp() {
        commentService = new CommentService(commentDAO, userDAO, courseDAO, enrollDAO);
        
        // Setup student
        student = new User("John Student", "john@example.com", "password", UserType.STUDENT);
        student.setIdUser(1);
        
        // Setup professor
        professor = new Professor("Jane Professor", "jane@example.com", "password", "Computer Science");
        professor.setIdUser(2);
        
        // Setup course
        course = new Course("Java Programming", "Learn Java", professor, 99.99, "Programming", 40);
        course.setIdCourse(1);
        
        // Setup comment
        comment = new Comment(1, 1, "This is a test comment", "John Student");
        comment.setCommentId(1);
        comment.setHourDate(LocalDateTime.now());
    }

    @Test
    void createComment_shouldCreateValidComment() throws SQLException, IllegalArgumentException {
        // Arrange
        int courseId = 1;
        int studentId = 1;
        String text = "This is a test comment";
        
        when(userDAO.searchById(studentId)).thenReturn(student);
        when(courseDAO.searchCourseById(courseId)).thenReturn(course);
        when(enrollDAO.checkEnroll(studentId, courseId)).thenReturn(true);
        when(commentDAO.saveComment(any(Comment.class))).thenReturn(comment);
        
        // Act
        Comment result = commentService.createComment(courseId, studentId, text);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getCommentId());
        assertEquals(courseId, result.getCourseId());
        assertEquals(studentId, result.getStudentId());
        assertEquals(text, result.getText());
        assertEquals("John Student", result.getStudentName());
        
        verify(userDAO).searchById(studentId);
        verify(courseDAO).searchCourseById(courseId);
        verify(enrollDAO).checkEnroll(studentId, courseId);
        verify(commentDAO).saveComment(any(Comment.class));
    }

    @Test
    void listCoursesComments_shouldReturnListOfComments() throws SQLException {
        // Arrange
        int courseId = 1;
        List<Comment> expectedComments = new ArrayList<>();
        expectedComments.add(comment);
        
        Comment comment2 = new Comment(1, 1, "Another test comment", "John Student");
        comment2.setCommentId(2);
        expectedComments.add(comment2);
        
        when(courseDAO.searchCourseById(courseId)).thenReturn(course);
        when(commentDAO.listCoursesComments(courseId)).thenReturn(expectedComments);
        
        // Act
        List<Comment> result = commentService.listCoursesComments(courseId);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("This is a test comment", result.get(0).getText());
        assertEquals("Another test comment", result.get(1).getText());
        
        verify(courseDAO).searchCourseById(courseId);
        verify(commentDAO).listCoursesComments(courseId);
    }

    @Test
    void deleteComment_shouldDeleteCommentByAuthor() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int commentId = 1;
        int studentId = 1; // Same as the comment author
        
        when(commentDAO.searchCommentById(commentId)).thenReturn(comment);
        when(userDAO.searchById(studentId)).thenReturn(student);
        when(commentDAO.deleteComment(commentId)).thenReturn(true);
        
        // Act
        boolean result = commentService.deleteComment(commentId, studentId);
        
        // Assert
        assertTrue(result);
        
        verify(commentDAO).searchCommentById(commentId);
        verify(userDAO).searchById(studentId);
        verify(commentDAO).deleteComment(commentId);
    }

    @Test
    void deleteComment_shouldDeleteCommentByProfessor() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int commentId = 1;
        int professorId = 2; // Professor ID
        
        when(commentDAO.searchCommentById(commentId)).thenReturn(comment);
        when(userDAO.searchById(professorId)).thenReturn(professor);
        when(courseDAO.searchCourseById(comment.getCourseId())).thenReturn(course);
        when(commentDAO.deleteComment(commentId)).thenReturn(true);
        
        // Act
        boolean result = commentService.deleteComment(commentId, professorId);
        
        // Assert
        assertTrue(result);
        
        verify(commentDAO).searchCommentById(commentId);
        verify(userDAO).searchById(professorId);
        verify(courseDAO).searchCourseById(comment.getCourseId());
        verify(commentDAO).deleteComment(commentId);
    }

    @Test
    void updateCommentText_shouldUpdateCommentText() throws SQLException, IllegalArgumentException, IllegalAccessException {
        // Arrange
        int commentId = 1;
        int studentId = 1; // Same as the comment author
        String newText = "Updated comment text";
        
        when(commentDAO.searchCommentById(commentId)).thenReturn(comment);
        when(commentDAO.updateComment(any(Comment.class))).thenReturn(true);
        
        // Act
        boolean result = commentService.updateCommentText(commentId, studentId, newText);
        
        // Assert
        assertTrue(result);
        assertEquals(newText, comment.getText());
        
        verify(commentDAO).searchCommentById(commentId);
        verify(commentDAO).updateComment(any(Comment.class));
    }

    @Test
    void createComment_shouldThrowExceptionForEmptyText() {
        // Arrange
        int courseId = 1;
        int studentId = 1;
        String emptyText = "";
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(courseId, studentId, emptyText);
        });
        
        assertEquals("Comment text cannot be empty.", exception.getMessage());
        
        // Verify no interactions with DAOs except for validation
        verifyNoInteractions(commentDAO);
        verifyNoInteractions(courseDAO);
        verifyNoInteractions(userDAO);
        verifyNoInteractions(enrollDAO);
    }

    @Test
    void createComment_shouldThrowExceptionForNonEnrolledStudent() throws SQLException {
        // Arrange
        int courseId = 1;
        int studentId = 1;
        String text = "This is a test comment";
        
        when(userDAO.searchById(studentId)).thenReturn(student);
        when(courseDAO.searchCourseById(courseId)).thenReturn(course);
        when(enrollDAO.checkEnroll(studentId, courseId)).thenReturn(false);
        
        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            commentService.createComment(courseId, studentId, text);
        });
        
        assertEquals("Student is not enrolled in the course and cannot comment.", exception.getMessage());
        
        verify(userDAO).searchById(studentId);
        verify(courseDAO).searchCourseById(courseId);
        verify(enrollDAO).checkEnroll(studentId, courseId);
        verifyNoInteractions(commentDAO);
    }
}