package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.EnrollDAO;
import com.elearning.remoteensine.dao.UserDAO;
import com.elearning.remoteensine.dao.CourseDAO;
import com.elearning.remoteensine.model.Enroll;
import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.Student;
import com.elearning.remoteensine.model.enums.UserType;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class EnrollService {
  private EnrollDAO enrollDAO;
  private UserDAO userDAO;
  private CourseDAO courseDAO;

  public EnrollService() {
    this.enrollDAO = new EnrollDAO();
    this.userDAO = new UserDAO();
    this.courseDAO = new CourseDAO();
  }

  /**
   * Realiza a matrícula de um aluno em um curso.
   *
   * @param idStudent ID do aluno.
   * @param idCourse ID do curso.
   * @return true se a matrícula for bem-sucedida.
   * @throws SQLException Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se o aluno ou curso não existirem, o aluno não for do tipo STUDENT,
   * ou se o aluno já estiver matriculado.
   */
  public boolean makeEnroll(int idStudent, int idCourse) throws SQLException, IllegalArgumentException {
    User student = userDAO.searchById(idStudent);
    if (student == null || student.getUserType() != UserType.STUDENT) {
      throw new IllegalArgumentException("Student ID " + idStudent + " not found.");
    }

    Course course = courseDAO.searchCourseById(idCourse);
    if (course == null) {
      throw new IllegalArgumentException("Course com ID " + idCourse + " not found.");
    }

    if (enrollDAO.checkEnroll(idStudent, idCourse)) {
      throw new IllegalArgumentException("Student already enrolled in this course.");
    }

    return enrollDAO.enrollmentStudent(idStudent, idCourse);
  }

  /**
   * Lista os cursos em que um aluno está matriculado.
   * Opcionalmente, carrega os detalhes completos do professor de cada curso.
   *
   * @param idStudent ID do aluno.
   * @return Lista de objetos Matricula, onde cada um contém informações do curso e do progresso.
   * @throws SQLException Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se o aluno não for encontrado.
   */
  public List<Enroll> enrollmentCoursesCheck(int idStudent) throws SQLException, IllegalArgumentException {
    User student = userDAO.searchById(idStudent);
    if (student == null || student.getUserType() != UserType.STUDENT) {
      throw new IllegalArgumentException("Student ID " + idStudent + " not found.");
    }

    List<Enroll> enrolls = enrollDAO.listCoursesByStudents(idStudent);
    return enrolls;
  }

  /**
   * Lista os alunos matriculados em um curso específico.
   *
   * @param idCourse ID do curso.
   * @return Lista de objetos Matricula, onde cada um contém informações do aluno e do progresso.
   * @throws SQLException Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se o curso não for encontrado.
   */
  public List<Enroll> enrolledStudentsCourses(int idCourse) throws SQLException, IllegalArgumentException {
    Course course = courseDAO.searchCourseById(idCourse);
    if (course == null) {
      throw new IllegalArgumentException("Course ID " + idCourse + " not found.");
    }
    return enrollDAO.lisStudentByCourse(idCourse);
  }

  /**
   * Atualiza o progresso do aluno em um curso.
   *
   * @param idStudent ID do aluno.
   * @param idCourse ID do curso.
   * @param progress Novo progresso (0.0 a 1.0).
   * @return true se o progresso foi atualizado.
   * @throws SQLException Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se o progresso for inválido ou a matrícula não existir.
   */
  public boolean updateStudentProgress(int idStudent, int idCourse, double progress) throws SQLException, IllegalArgumentException {
    if (progress < 0.0 || progress > 1.0) {
      throw new IllegalArgumentException("\n" +
          "Progress must be between 0.0 and 1.0.");
    }
    if (!enrollDAO.checkEnroll(idStudent, idCourse)) {
      throw new IllegalArgumentException("Student is not enrolled in this course to update progress.");
    }
    return enrollDAO.updateProgress(idStudent, idCourse, progress);
  }

  /**
   * Cancela a matrícula de um aluno em um curso.
   *
   * @param idStudent ID do aluno.
   * @param idCourse ID do curso.
   * @return true se a matrícula foi cancelada com sucesso.
   * @throws SQLException Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se a matrícula não existir.
   */
  public boolean cancelEnrollStudent(int idStudent, int idCourse) throws SQLException, IllegalArgumentException {
    if (!enrollDAO.cancelEnroll(idStudent, idCourse)) {
      throw new IllegalArgumentException("Enroll not found for cancellation.");
    }
    return enrollDAO.cancelEnroll(idStudent, idCourse);
  }

  public boolean checkEnrolledStudents(int idStudent, int idCurso) throws SQLException {
    return enrollDAO.checkEnroll(idStudent, idCurso);
  }
}

