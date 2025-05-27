package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.CommentDAO;
import com.elearning.remoteensine.dao.CourseDAO;
import com.elearning.remoteensine.dao.UserDAO;
import com.elearning.remoteensine.dao.EnrollDAO;
import com.elearning.remoteensine.model.Comment;
import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.Student;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.UserType;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class CommentService {

  private CommentDAO commentDAO;
  private UserDAO userDAO;
  private CourseDAO courseDAO;
  private EnrollDAO enrollDAO;

  public CommentService() {
    this.commentDAO = new CommentDAO();
    this.userDAO = new UserDAO();
    this.courseDAO = new CourseDAO();
    this.enrollDAO = new EnrollDAO();
  }

  /**
   * Permite que um aluno matriculado adicione um comentário a um curso.
   *
   * @param idCourse  ID do curso a ser comentado.
   * @param idStudent ID do aluno que está comentando.
   * @param text      Conteúdo do comentário.
   * @return O comentário criado.
   * @throws SQLException             Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se dados inválidos, curso/aluno não encontrado, ou aluno não matriculado.
   */
  public Comment createComment(int idCourse, int idStudent, String text)
      throws SQLException, IllegalArgumentException {

    if (text == null || text.trim().isEmpty()) {
      throw new IllegalArgumentException("Comment text cannot be empty.");
    }

    User student = userDAO.searchById(idStudent);
    if (student == null || student.getUserType() != UserType.STUDENT) {
      throw new IllegalArgumentException("Student ID " + idStudent + "not found.");
    }

    Course course = courseDAO.searchCourseById(idCourse);
    if (course == null) {
      throw new IllegalArgumentException("Course ID " + idCourse + " not found.");
    }

    if (!enrollDAO.checkEnroll(idStudent, idCourse)) {
      throw new IllegalArgumentException("Student is not enrolled in the course and cannot comment.");
    }

    Comment newComment = new Comment(idCourse, idStudent, text, student.getName());
    return commentDAO.saveComment(newComment);
  }

  /**
   * Lista todos os comentários de um curso.
   * Opcionalmente, poderia carregar os objetos Student completos para cada comentário.
   *
   * @param idCourse ID do curso.
   * @return Lista de comentários.
   * @throws SQLException Erro de banco.
   */
  public List<Comment> listCoursesComments(int idCourse) throws SQLException {
    if (courseDAO.searchCourseById(idCourse) == null) {
      throw new IllegalArgumentException("Course ID " + idCourse + " not found.");
    }

    List<Comment> comments = commentDAO.listCoursesComments(idCourse);
    return comments;
  }

  /**
   * Permite que o autor do comentário ou o professor do curso delete um comentário.
   *
   * @param idComment    ID do comentário a ser deletado.
   * @param idUserLogged ID do usuário tentando deletar.
   * @return true se deletado com sucesso.
   * @throws SQLException             Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se comentário não encontrado.
   * @throws IllegalAccessException   Se o usuário não tiver permissão para deletar.
   */
  public boolean deleteComment(int idComment, int idUserLogged)
      throws SQLException, IllegalArgumentException, IllegalAccessException {

    Comment comment = commentDAO.searchCommentById(idComment);
    if (comment == null) {
      throw new IllegalArgumentException("Comment ID " + idComment + " not found.");
    }

    User loggedUser = userDAO.searchById(idUserLogged);
    if (loggedUser == null) {
      throw new IllegalArgumentException("Logged in user not found.");
    }

    boolean authorized = false;
    if (comment.getStudentId() == idUserLogged) {
      authorized = true;
    } else if (loggedUser.getUserType() == UserType.PROFESSOR) {
      Course courseComment = courseDAO.searchCourseById(comment.getCourseId());
      if (courseComment != null && courseComment.getResponsibleProfessor() != null &&
          courseComment.getResponsibleProfessor().getIdUser() == idUserLogged) {
        authorized = true;
      }
    }

    if (!authorized) {
      throw new IllegalAccessException("User not authorized to delete this comment.");
    }

    return commentDAO.deleteComment(idComment);
  }

  /**
   * Permite que o autor do comentário atualize o texto.
   *
   * @param idComment    ID do comentário a ser atualizado.
   * @param idUserLogged ID do aluno que está tentando atualizar.
   * @param newText      Novo texto do comentário.
   * @return true se atualizado com sucesso.
   * @throws SQLException             Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se comentário não encontrado ou texto vazio.
   * @throws IllegalAccessException   Se o aluno não for o autor do comentário.
   */
  public boolean updateCommentText(int idComment, int idUserLogged, String newText)
      throws SQLException, IllegalArgumentException, IllegalAccessException {

    if (newText == null || newText.trim().isEmpty()) {
      throw new IllegalArgumentException("New comment text cannot be empty.");
    }

    Comment comment = commentDAO.searchCommentById(idComment);
    if (comment == null) {
      throw new IllegalArgumentException("Comment ID " + idComment + " not found.");
    }

    if (comment.getStudentId() != idUserLogged) {
      throw new IllegalAccessException("User not authorized to edit this comment.");
    }

    comment.setText(newText);
    return commentDAO.updateComment(comment);
  }

  /**
   * Lista todos os comentários de um curso, populando o nome do aluno em cada comentário.
   *
   * @param idCourse ID do curso.
   * @return Lista de comentários com nome do aluno.
   * @throws SQLException Erro de banco.
   * @throws IllegalArgumentException Se o curso não for encontrado.
   */
  public List<Comment> listStudentsCommentsInCourse(int idCourse) throws SQLException {
    if (courseDAO.searchCourseById(idCourse) == null) {
      throw new IllegalArgumentException("Curso com ID " + idCourse + " não encontrado.");
    }
    List<Comment> comments = commentDAO.listCoursesComments(idCourse);
    System.out.println("Service: Encontrados " + comments.size() + " comentários para o curso ID " + idCourse); // DEBUG
    for (Comment comment : comments) {
      if (comment.getStudentId() > 0) {
        User student = userDAO.searchById(comment.getStudentId());
        if (student != null) {

          comment.setStudentName(student.getName());
        } else {
          comment.setStudentName("Aluno Desconhecido");
        }
      } else {

        comment.setStudentName("ID de Aluno Inválido");
      }
    }
    return comments;
  }
}

