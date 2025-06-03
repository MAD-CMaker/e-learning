package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.UserDAO;
import com.elearning.remoteensine.dao.DoubtDAO;
import com.elearning.remoteensine.dao.CourseDAO;
import com.elearning.remoteensine.model.Professor;
import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.Doubt;
import com.elearning.remoteensine.model.enums.UserType;
import com.elearning.remoteensine.model.enums.DoubtStatus;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DoubtService {
    private DoubtDAO doubtDAO;
    private UserDAO userDAO;
    private CourseDAO courseDAO;

  public DoubtService(DoubtDAO doubtDAO, UserDAO userDAO, CourseDAO courseDAO) {
    this.doubtDAO = doubtDAO;
    this.userDAO = userDAO;
    this.courseDAO = courseDAO;
  }

  /**
   * Cria uma nova dúvida para um curso.
   *
   * @param idCourse ID do curso relacionado à dúvida.
   * @param idStudent ID do aluno que está postando a dúvida.
   * @param title Título da dúvida.
   * @param description Descrição detalhada da dúvida.
   * @return A dúvida criada.
   * @throws SQLException Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se dados inválidos, curso ou aluno não encontrado.
   */
  public Doubt createDoubt(int idCourse, int idStudent, String title, String description)
      throws SQLException, IllegalArgumentException {

    if (title == null || title.trim().isEmpty() || description == null || description.trim().isEmpty()) {
      throw new IllegalArgumentException("Question title and description cannot be empty.");
    }

    Course course = courseDAO.searchCourseById(idCourse);
    if (course == null) {
      throw new IllegalArgumentException("Course ID " + idCourse + " not found.");
    }

    User student = userDAO.searchById(idStudent);
    if (student == null || student.getUserType() != UserType.STUDENT) {
      throw new IllegalArgumentException("Student ID " + idStudent + " not found.");
    }

    Doubt newDoubt = new Doubt(idCourse, idStudent, title, description, student.getName());
    return doubtDAO.saveDoubt(newDoubt);
  }

  /**
   * Permite que um professor responda a uma dúvida.
   *
   * @param idDoubt ID da dúvida a ser respondida.
   * @param idProfessor ID do professor que está respondendo.
   * @param answerText Texto da resposta.
   * @return A dúvida atualizada com a resposta.
   * @throws SQLException Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se dados inválidos, dúvida ou professor não encontrado.
   * @throws IllegalAccessException Se o professor não for o responsável pelo curso da dúvida.
   */
  public Doubt answerDoubt(int idDoubt, int idProfessor, String answerText)
      throws SQLException, IllegalArgumentException, IllegalAccessException {

    if (answerText == null || answerText.trim().isEmpty()) {
      throw new IllegalArgumentException("\n" +
          "Response text cannot be empty.");
    }

    Doubt doubt = doubtDAO.searchDoubtById(idDoubt);
    if (doubt == null) {
      throw new IllegalArgumentException("Doubt ID " + idDoubt + " not found.");
    }

    if (doubt.getStatus() == DoubtStatus.RESPONDIDA || doubt.getStatus() == DoubtStatus.FECHADA) {
      throw new IllegalArgumentException("This question has already been answered or closed.");
    }

    User userProfessor = userDAO.searchById(idProfessor);
    if (userProfessor == null || userProfessor.getUserType() != UserType.PROFESSOR) {
      throw new IllegalArgumentException("Professor ID " + idProfessor + " not found");
    }
    Professor professor = (Professor) userProfessor;

    Course doubtCourse = courseDAO.searchCourseById(doubt.getCourseId());
    if (doubtCourse == null) {
      throw new IllegalArgumentException("Course associated with doubt (ID: " + doubt.getCourseId() + ") not found.");
    }

    if (doubtCourse.getResponsibleProfessor() == null || doubtCourse.getResponsibleProfessor().getIdUser() != professor.getIdUser()) {
      throw new IllegalAccessException("Professor not authorized to answer questions about this course.");
    }

    doubt.answerDoubt(answerText, professor);
    if (doubtDAO.updateDouts(doubt)) {
      return doubt;
    } else {
      throw new SQLException("Failed to update question with answer.");
    }
  }

  /**
   * Busca uma dúvida pelo ID.
   * Opcionalmente, pode-se carregar os objetos Student e Professor completos aqui.
   *
   * @param idDoubt ID da dúvida.
   * @return A dúvida encontrada, ou null.
   * @throws SQLException Erro de banco.
   */
  public Doubt seacrhDoubt(int idDoubt) throws SQLException {
    Doubt doubt = doubtDAO.searchDoubtById(idDoubt);
    return doubt;
  }

  /**
   * Lista todas as dúvidas de um curso.
   *
   * @param idCourse ID do curso.
   * @return Lista de dúvidas.
   * @throws SQLException Erro de banco.
   */
  public List<Doubt> listCourseDoubts(int idCourse) throws SQLException {
    if (courseDAO.searchCourseById(idCourse) == null) {
      throw new IllegalArgumentException("Curso com ID " + idCourse + " não encontrado.");
    }
    return doubtDAO.lisCoursesDoubt(idCourse);
  }

  /**
   * Lista todas as dúvidas de um aluno.
   *
   * @param idStudent ID do aluno.
   * @return Lista de dúvidas.
   * @throws SQLException Erro de banco.
   */
  public List<Doubt> listarDuvidasDoAluno(int idStudent) throws SQLException {
    return doubtDAO.listStudentsDoubts(idStudent);
  }

  /**
   * Permite que o criador da dúvida ou o professor do curso a feche.
   *
   * @param idDoubt ID da dúvida.
   * @param idUserLogged ID do usuário tentando fechar (aluno dono ou professor do curso).
   * @return true se fechada.
   * @throws SQLException Erro de banco.
   * @throws IllegalArgumentException Se dúvida não encontrada.
   * @throws IllegalAccessException Se usuário não autorizado.
   */
  public boolean closeDoubts(int idDoubt, int idUserLogged) throws SQLException, IllegalArgumentException, IllegalAccessException {
    Doubt doubt = doubtDAO.searchDoubtById(idDoubt);
    if (doubt == null) {
      throw new IllegalArgumentException("Doubt ID " + idDoubt + " not found.");
    }
    if (doubt.getStatus() == DoubtStatus.FECHADA) {
      return true;
    }

    User loggedUser = userDAO.searchById(idUserLogged);
    if (loggedUser == null) {
      throw new IllegalArgumentException("\n" +
          "Logged in user not found.");
    }

    boolean authorized = false;
    if (doubt.getStudentId() == idUserLogged) {
      authorized = true;
    } else if (loggedUser.getUserType() == UserType.PROFESSOR) {
      Course doubtCourse = courseDAO.searchCourseById(doubt.getCourseId());
      if (doubtCourse != null && doubtCourse.getResponsibleProfessor() != null &&
          doubtCourse.getResponsibleProfessor().getIdUser() == idUserLogged) {
        authorized = true;
      }
    }
    if (!authorized) {
        throw new IllegalAccessException("User not authorized to close this question.");
    }
    doubt.setStatus(DoubtStatus.FECHADA);
    return doubtDAO.updateDouts(doubt);
  }

  /**
   * Lista todas as dúvidas de um curso, populando o nome do aluno e do professor (se houver resposta).
   *
   * @param idCourse ID do curso.
   * @return Lista de dúvidas com detalhes.
   * @throws SQLException Erro de banco.
   */
  public List<Doubt> listarDuvidasDoCursoComDetalhes(int idCourse) throws SQLException {
    if (courseDAO.searchCourseById(idCourse) == null) {
      throw new IllegalArgumentException("Curso com ID " + idCourse + " não encontrado.");
    }

    List<Doubt> doubts = doubtDAO.lisCoursesDoubt(idCourse);

    for (Doubt doubt : doubts) {
      if (doubt.getStudentId() > 0) {
        User aluno = userDAO.searchById(doubt.getStudentId());
        if (aluno != null) {
          doubt.setStudentName(aluno.getName());
        } else {
          doubt.setStudentName("Aluno Desconhecido");
        }
      }

      if (doubt.getProfessorId() != null && doubt.getProfessorId() > 0) {
        User professor = userDAO.searchById(doubt.getProfessorId());
        if (professor != null && professor.getUserType() == UserType.PROFESSOR) {
          doubt.setProfessorName(professor.getName());
        } else {
          doubt.setProfessorName("Professor Desconhecido");
        }
      }
    }
    return doubts;
}
}
