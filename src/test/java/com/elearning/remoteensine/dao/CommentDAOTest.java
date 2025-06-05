package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.Comment;
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
public class CommentDAOTest {

    @Mock
    private DatabaseConnector databaseConnector;

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    private CommentDAO commentDAO;

    @BeforeEach
    void setUp() throws SQLException {
        commentDAO = new CommentDAO(databaseConnector);
        when(databaseConnector.getConnection()).thenReturn(connection);
    }

    @Test
    void saveComment_shouldSaveAndReturnCommentWithId() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);

        LocalDateTime now = LocalDateTime.now();
        Comment comment = new Comment();
        comment.setCourseId(1);
        comment.setStudentId(2);
        comment.setText("Test Comment");
        comment.setHourDate(now);

        when(preparedStatement.executeUpdate()).thenReturn(1);
        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt(1)).thenReturn(10);

        // Act
        Comment savedComment = commentDAO.saveComment(comment);

        // Assert
        assertEquals(10, savedComment.getCommentId());
        assertEquals(1, savedComment.getCourseId());
        assertEquals(2, savedComment.getStudentId());
        assertEquals("Test Comment", savedComment.getText());
        assertEquals(now, savedComment.getHourDate());

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
        verify(preparedStatement).setString(3, "Test Comment");
        verify(preparedStatement).setTimestamp(eq(4), any(Timestamp.class));
        verify(preparedStatement).executeUpdate();
        verify(preparedStatement).getGeneratedKeys();
    }

    @Test
    void searchCommentById_shouldReturnCommentWhenFound() throws SQLException {
        // Arrange
        int commentId = 10;
        LocalDateTime now = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(now);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("comment_id")).thenReturn(commentId);
        when(resultSet.getInt("course_id")).thenReturn(1);
        when(resultSet.getInt("student_id")).thenReturn(2);
        when(resultSet.getString("text")).thenReturn("Test Comment");
        when(resultSet.getTimestamp("hour_date")).thenReturn(timestamp);

        // Act
        Comment foundComment = commentDAO.searchCommentById(commentId);

        // Assert
        assertNotNull(foundComment);
        assertEquals(commentId, foundComment.getCommentId());
        assertEquals(1, foundComment.getCourseId());
        assertEquals(2, foundComment.getStudentId());
        assertEquals("Test Comment", foundComment.getText());
        assertEquals(now, foundComment.getHourDate());

        verify(preparedStatement).setInt(1, commentId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void listCoursesComments_shouldReturnListOfComments() throws SQLException {
        // Arrange
        int courseId = 1;
        LocalDateTime now = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(now);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // Return true twice for two comments, then false

        // First comment
        when(resultSet.getInt("comment_id")).thenReturn(10, 11);
        when(resultSet.getInt("course_id")).thenReturn(courseId, courseId);
        when(resultSet.getInt("student_id")).thenReturn(2, 3);
        when(resultSet.getString("text")).thenReturn("Comment 1", "Comment 2");
        when(resultSet.getTimestamp("hour_date")).thenReturn(timestamp, timestamp);

        // Act
        List<Comment> comments = commentDAO.listCoursesComments(courseId);

        // Assert
        assertNotNull(comments);
        assertEquals(2, comments.size());

        // First comment
        assertEquals(10, comments.get(0).getCommentId());
        assertEquals(courseId, comments.get(0).getCourseId());
        assertEquals(2, comments.get(0).getStudentId());
        assertEquals("Comment 1", comments.get(0).getText());
        assertEquals(now, comments.get(0).getHourDate());

        // Second comment
        assertEquals(11, comments.get(1).getCommentId());
        assertEquals(courseId, comments.get(1).getCourseId());
        assertEquals(3, comments.get(1).getStudentId());
        assertEquals("Comment 2", comments.get(1).getText());
        assertEquals(now, comments.get(1).getHourDate());

        verify(preparedStatement).setInt(1, courseId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void listStudentsComments_shouldReturnListOfComments() throws SQLException {
        // Arrange
        int studentId = 2;
        LocalDateTime now = LocalDateTime.now();
        Timestamp timestamp = Timestamp.valueOf(now);

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true, true, false); // Return true twice for two comments, then false

        // Comments data
        when(resultSet.getInt("comment_id")).thenReturn(10, 11);
        when(resultSet.getInt("course_id")).thenReturn(1, 2);
        when(resultSet.getInt("student_id")).thenReturn(studentId, studentId);
        when(resultSet.getString("text")).thenReturn("Comment 1", "Comment 2");
        when(resultSet.getTimestamp("hour_date")).thenReturn(timestamp, timestamp);

        // Act
        List<Comment> comments = commentDAO.listStudentsComments(studentId);

        // Assert
        assertNotNull(comments);
        assertEquals(2, comments.size());

        // First comment
        assertEquals(10, comments.get(0).getCommentId());
        assertEquals(1, comments.get(0).getCourseId());
        assertEquals(studentId, comments.get(0).getStudentId());
        assertEquals("Comment 1", comments.get(0).getText());
        assertEquals(now, comments.get(0).getHourDate());

        // Second comment
        assertEquals(11, comments.get(1).getCommentId());
        assertEquals(2, comments.get(1).getCourseId());
        assertEquals(studentId, comments.get(1).getStudentId());
        assertEquals("Comment 2", comments.get(1).getText());
        assertEquals(now, comments.get(1).getHourDate());

        verify(preparedStatement).setInt(1, studentId);
        verify(preparedStatement).executeQuery();
    }

    @Test
    void updateComment_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        Comment comment = new Comment();
        comment.setCommentId(10);
        comment.setText("Updated Comment");

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = commentDAO.updateComment(comment);

        // Assert
        assertTrue(result);

        verify(preparedStatement).setString(1, "Updated Comment");
        verify(preparedStatement).setTimestamp(eq(2), any(Timestamp.class));
        verify(preparedStatement).setInt(3, 10);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void deleteComment_shouldReturnTrueWhenSuccessful() throws SQLException {
        // Arrange
        int commentId = 10;
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        boolean result = commentDAO.deleteComment(commentId);

        // Assert
        assertTrue(result);

        verify(preparedStatement).setInt(1, commentId);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void saveComment_shouldThrowExceptionWhenInsertFails() throws SQLException {
        // Arrange
        when(connection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(preparedStatement);

        Comment comment = new Comment();
        comment.setCourseId(1);
        comment.setStudentId(2);
        comment.setText("Test Comment");

        when(preparedStatement.executeUpdate()).thenReturn(0);

        // Act & Assert
        assertThrows(SQLException.class, () -> commentDAO.saveComment(comment));

        verify(preparedStatement).setInt(1, 1);
        verify(preparedStatement).setInt(2, 2);
        verify(preparedStatement).setString(3, "Test Comment");
        verify(preparedStatement).setTimestamp(eq(4), any(Timestamp.class));
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void searchCommentById_shouldReturnNullWhenNotFound() throws SQLException {
        // Arrange
        int commentId = 10;

        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // Act
        Comment foundComment = commentDAO.searchCommentById(commentId);

        // Assert
        assertNull(foundComment);

        verify(preparedStatement).setInt(1, commentId);
        verify(preparedStatement).executeQuery();
    }
}