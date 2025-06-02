package com.elearning.remoteensine.service;

import com.elearning.remoteensine.dao.ExamDAO;
import com.elearning.remoteensine.dao.CourseDAO;
import com.elearning.remoteensine.dao.UserDAO;
import com.elearning.remoteensine.dao.EnrollDAO;
import com.elearning.remoteensine.dao.ExamDefinitionDAO;
import com.elearning.remoteensine.model.Exam;
import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.ExamDefinition;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.UserType;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExamService {

  private ExamDAO examDAO;
  private UserDAO userDAO;
  private CourseDAO courseDAO;
  private EnrollDAO enrollDAO;
  private ExamDefinitionDAO examDefinitionDAO;

  public ExamService() {
    this.examDAO = new ExamDAO();
    this.userDAO = new UserDAO();
    this.courseDAO = new CourseDAO();
    this.enrollDAO = new EnrollDAO();
    this.examDefinitionDAO = new ExamDefinitionDAO();
  }

  public Exam submitCourseEvaluation(int idCourse, int idStudent, int grade, String comment)
      throws SQLException, IllegalArgumentException {

    User student = userDAO.searchById(idStudent);
    if (student == null || student.getUserType() != UserType.STUDENT) {
      throw new IllegalArgumentException("Aluno com ID " + idStudent + " não encontrado ou não é um estudante.");
    }

    Course course = courseDAO.searchCourseById(idCourse);
    if (course == null) {
      throw new IllegalArgumentException("Curso com ID " + idCourse + " não encontrado.");
    }


    if (!enrollDAO.checkEnroll(idStudent, idCourse)) {
      throw new IllegalArgumentException("Aluno não está matriculado no curso e não pode avaliá-lo.");
    }

    if (examDAO.searchExamByStundentACourse(idStudent, idCourse) != null) {
      throw new IllegalArgumentException("Você já avaliou este curso.");
    }
    Exam newExam = new Exam(idCourse, idStudent, grade, comment);
    return examDAO.saveExam(newExam);
  }

  /**
   * Verifica se um aluno já avaliou um curso específico.
   *
   * @param studentId ID do aluno.
   * @param courseId ID do curso.
   * @return true se o aluno já avaliou, false caso contrário.
   * @throws SQLException Se ocorrer erro no banco.
   */
  public boolean hasStudentEvaluatedCourse(int studentId, int courseId) throws SQLException {
    return examDAO.searchExamByStundentACourse(studentId, courseId) != null;
  }

  /**
   * Lista todas as avaliações de um curso, populando o nome do aluno em cada avaliação.
   *
   * @param idCourse ID do curso.
   * @return Lista de objetos Exam com studentName populado.
   * @throws SQLException Erro de banco.
   * @throws IllegalArgumentException Se o curso não for encontrado.
   */
  public List<Exam> getEvaluationsForCourseWithStudentNames(int idCourse) throws SQLException, IllegalArgumentException {
    if (courseDAO.searchCourseById(idCourse) == null) {
      throw new IllegalArgumentException("Curso com ID " + idCourse + " não encontrado.");
    }

    List<Exam> exams = examDAO.listCoursesExams(idCourse);

    if (exams == null) {
      return new ArrayList<>();
    }

    for (Exam exam : exams) {
      if (exam.getStudentId() > 0) {
        User student = userDAO.searchById(exam.getStudentId());
        if (student != null) {
          exam.setStudentName(student.getName()); // Usa o setter adicionado em Exam.java
        } else {
          System.err.println("Service (Exam): Aluno NÃO encontrado para ID " + exam.getStudentId() + " na avaliação ID " + exam.getExamId());
          exam.setStudentName("Aluno Desconhecido");
        }
      } else {
        exam.setStudentName("ID de Aluno Inválido");
      }
    }
    return exams;
  }

  /**
   * Permite que um aluno matriculado adicione uma avaliação a um curso.
   *
   * @param idCourse ID do curso a ser avaliado.
   * @param idStudent ID do aluno que está avaliando.
   * @param grade Nota da avaliação (ex: 1 a 5).
   * @param comment Comentário textual (opcional).
   * @return A avaliação criada.
   * @throws SQLException Se ocorrer erro no banco, incluindo se o aluno já avaliou o curso.
   * @throws IllegalArgumentException Se dados inválidos, curso/aluno não encontrado, ou aluno não matriculado.
   */
  public Exam createExam(int idCourse, int idStudent, int grade, String comment)
      throws SQLException, IllegalArgumentException {

    User student = userDAO.searchById(idStudent);
    if (student == null || student.getUserType() != UserType.STUDENT) {
      throw new IllegalArgumentException("Student ID " + idStudent + " not found.");
    }

    Course course = courseDAO.searchCourseById(idCourse);
    if (course == null) {
      throw new IllegalArgumentException("Course ID " + idCourse + " not found.");
    }

    if (!enrollDAO.checkEnroll(idStudent, idCourse)) {
      throw new IllegalArgumentException("Student is not enrolled in the course and cannot evaluate it.");
    }

    if (examDAO.searchExamByStundentACourse(idStudent, idCourse) != null) {
      throw new IllegalArgumentException("Você já avaliou este curso.");
    }

    Exam newExam = new Exam(idCourse, idStudent, grade, comment);
    return examDAO.saveExam(newExam);
  }

  /**
   * Lista todas as avaliações de um curso.
   *
   * @param idCourse ID do curso.
   * @return Lista de avaliações.
   * @throws SQLException Erro de banco.
   * @throws IllegalArgumentException Se o curso não for encontrado.
   */
  public List<Exam> listCoursesExam(int idCourse) throws SQLException, IllegalArgumentException {
    if (courseDAO.searchCourseById(idCourse) == null) {
      throw new IllegalArgumentException("Course ID " + idCourse + " not found.");
    }
    return examDAO.listCoursesExams(idCourse);
  }

  /**
   * Obtém a média das notas de um curso.
   * @param idCourse ID do curso.
   * @return A média das notas.
   * @throws SQLException Erro de banco.
   * @throws IllegalArgumentException Se o curso não for encontrado.
   */
  public double getCoursesMedia(int idCourse) throws SQLException, IllegalArgumentException {
    if (courseDAO.searchCourseById(idCourse) == null) {
      throw new IllegalArgumentException("Course ID " + idCourse + " not found.");
    }
    return examDAO.mediaCoursesGrade(idCourse);
  }

  /**
   * Permite que o autor da avaliação atualize sua nota ou comentário.
   *
   * @param idExam ID da avaliação a ser atualizada.
   * @param idStudentLogged ID do aluno que está tentando atualizar.
   * @param newGrade Nova nota.
   * @param newComment Novo comentário.
   * @return true se atualizado com sucesso.
   * @throws SQLException Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se avaliação não encontrada ou dados inválidos.
   * @throws IllegalAccessException Se o aluno não for o autor da avaliação.
   */
  public boolean updateMyExam(int idExam, int idStudentLogged, int newGrade, String newComment)
      throws SQLException, IllegalArgumentException, IllegalAccessException {

    Exam existingExam = examDAO.searchExamById(idExam);
    if (existingExam == null) {
      throw new IllegalArgumentException("Exam ID " + idExam + " not foubd.");
    }

    if (existingExam.getStudentId() != idStudentLogged) {
        throw new IllegalAccessException("User not authorized to edit this review.");
    }

    existingExam.setGrade(newGrade);
    existingExam.setComment(newComment);

    return examDAO.updateExam(existingExam);
  }


  /**
   * Permite que o autor da avaliação ou o professor do curso delete uma avaliação.
   * (A lógica de permissão pode ser ajustada conforme necessário).
   *
   * @param idExam ID da avaliação a ser deletada.
   * @param idUserLogged ID do usuário tentando deletar.
   * @return true se deletado com sucesso.
   * @throws SQLException Se ocorrer erro no banco.
   * @throws IllegalArgumentException Se avaliação não encontrada.
   * @throws IllegalAccessException Se o usuário não tiver permissão para deletar.
   */
  public boolean deleteExam(int idExam, int idUserLogged)
      throws SQLException, IllegalArgumentException, IllegalAccessException {

    Exam exam = examDAO.searchExamById(idExam);
    if (exam == null) {
      throw new IllegalArgumentException("Exam ID " + idExam + " not found.");
    }

    User loggedUser = userDAO.searchById(idUserLogged);
    if (loggedUser == null) {
      throw new IllegalArgumentException("\n" +
          "Logged in user not found.");
    }

    boolean autorizado = false;
    if (exam.getStudentId() == idUserLogged) {
      autorizado = true;
    } else if (loggedUser.getUserType() == UserType.PROFESSOR) {
      Course examCourse = courseDAO.searchCourseById(exam.getCourseId());
      if (examCourse != null && examCourse.getResponsibleProfessor() != null &&
          examCourse.getResponsibleProfessor().getIdUser() == idUserLogged) {
        autorizado = true;
      }
    }

    if (!autorizado) {
      throw new IllegalAccessException("\n" +
          "User not authorized to delete this review.");
    }

    return examDAO.deleteExam(idExam);
  }
  /**
   * Salva a tentativa/resultado de um exame realizado por um aluno.
   * @param examAttempt O objeto Exam contendo os dados da tentativa.
   * @return O objeto Exam salvo (com ID, se gerado).
   * @throws SQLException Se houver erro no banco.
   * @throws IllegalArgumentException Se dados inválidos.
   */
  public Exam saveStudentExamAttempt(Exam examAttempt) throws SQLException, IllegalArgumentException {
    if (examAttempt == null) {
      throw new IllegalArgumentException("Tentativa de exame não pode ser nula.");
    }

    System.out.println("SERVICE (ExamAttempt): Validando aluno ID: " + examAttempt.getStudentId());
    User student = userDAO.searchById(examAttempt.getStudentId());
    if (student == null || student.getUserType() != UserType.STUDENT) {
      System.err.println("SERVICE (ExamAttempt): Aluno inválido ou não é estudante. ID: " + examAttempt.getStudentId());
      throw new IllegalArgumentException("Aluno inválido para esta tentativa de exame.");
    }

    ExamDefinition examDefValidation = examDefinitionDAO.findExamDefinitionById(examAttempt.getIdExamDefinition());

    if (examDefValidation == null) {
      throw new IllegalArgumentException("Definição de exame inválida para esta tentativa.");
    }

    if (examAttempt.getCourseId() != examDefValidation.getIdCourse()){
      System.out.printf("SERVICE (ExamAttempt): INCONSISTÊNCIA - ID do curso da tentativa (%d) não corresponde ao ID do curso da definição do exame (%d)%n",
              examAttempt.getCourseId(), examDefValidation.getIdCourse());
      throw new IllegalArgumentException("Inconsistência entre o curso da tentativa e o curso da definição do exame.");
    }


    System.out.printf("SERVICE (Exam): Salvando tentativa de exame para Aluno ID: %d, Definição de Exame ID: %d, Nota %d%n",
            examAttempt.getStudentId(),
            examAttempt.getIdExamDefinition(),
            examAttempt.getGrade());

    return examDAO.saveExam(examAttempt);
  }
}
