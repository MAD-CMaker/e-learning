package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.model.Enroll;
import com.elearning.remoteensine.model.Professor;
import com.elearning.remoteensine.model.Student;
import com.elearning.remoteensine.util.DatabaseConnector;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
public class EnrollDAO extends AbstractDAO{

  public EnrollDAO(DatabaseConnector databaseConnector) {
    super(databaseConnector);
  }

  /**
   * Matricula um aluno em um curso.
   *
   * @param idStudent ID do aluno.
   * @param idCourse ID do curso.
   * @return true se a matrícula foi bem-sucedida, false caso contrário (ex: já matriculado).
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean enrollmentStudent(int idStudent, int idCourse) throws SQLException {
    String sql = "INSERT INTO students_courses (student_id, course_id, enrollment_date, progress) VALUES (?, ?, ?, ?)";
    if (checkEnroll(idStudent, idCourse)) {
      System.out.println("Student ID " + idStudent + " is already enrolled in the course ID " + idCourse);
      return false;
    }

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, idStudent);
      pstmt.setInt(2, idCourse);
      pstmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
      pstmt.setDouble(4, 0.0);

      return pstmt.executeUpdate() > 0;
    }
  }

  /**
   * Verifica se um aluno está matriculado em um curso específico.
   *
   * @param idStudent ID do aluno.
   * @param idCourse ID do curso.
   * @return true se o aluno estiver matriculado, false caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean checkEnroll(int idStudent, int idCourse) throws SQLException {
    String sql = "SELECT COUNT(*) FROM students_courses WHERE student_id = ? AND course_id = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idStudent);
      pstmt.setInt(2, idCourse);
      try (ResultSet rs = pstmt.executeQuery()) {
        if (rs.next()) {
          return rs.getInt(1) > 0;
        }
      }
    }
    return false;
  }

  /**
   * Lista todos os cursos em que um aluno está matriculado.
   * Retorna uma lista de objetos Matricula, onde cada Matricula contém o Curso.
   * O objeto Student dentro da Matricula terá apenas o ID.
   *
   * @param idStudent ID do aluno.
   * @return Lista de matrículas (com cursos).
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public List<Enroll> listCoursesByStudents(int idStudent) throws SQLException {
    List<Enroll> enrolls = new ArrayList<>();
    String sql = "SELECT sc.student_id, sc.course_id, sc.enrollment_date, sc.progress, " +
        "c.title AS course_title, c.description AS description_course, c.price AS price_course, " +
        "c.category AS category_course, c.load_hour AS load_hour_course, " +
        "c.responsible_professor_id AS professor_id_course " +
        "FROM students_courses sc " +
        "JOIN courses c ON sc.course_id = c.course_id " +
        "WHERE sc.student_id = ?";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idStudent);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          Student student = new Student();
          student.setIdUser(rs.getInt("student_id"));

          Course course = new Course();
          course.setIdCourse(rs.getInt("course_id"));
          course.setTitle(rs.getString("course_title"));
          course.setDescription(rs.getString("description_course"));
          course.setPrice(rs.getDouble("price_course"));
          course.setCategory(rs.getString("category_course"));
          course.setHoursLoad(rs.getInt("load_hour_course"));

          Professor courseProfessor = new Professor();
          courseProfessor.setIdUser(rs.getInt("professor_id_course"));
          course.setResponsibleProfessor(courseProfessor);


          Enroll enroll = new Enroll();
          enroll.setStudent(student);
          enroll.setCourse(course);
          enroll.setEnrollDate(rs.getTimestamp("enrollment_date").toLocalDateTime());
          enroll.setProgress(rs.getDouble("progress"));

          enrolls.add(enroll);
        }
      }
    }
    return enrolls;
  }

  /**
   * Lista todos os alunos matriculados em um curso específico.
   * Retorna uma lista de objetos Matricula, onde cada Matricula contém o Student.
   * O objeto Curso dentro da Matricula terá apenas o ID.
   *
   * @param idCourse ID do curso.
   * @return Lista de matrículas (com alunos).
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public List<Enroll> lisStudentByCourse(int idCourse) throws SQLException {
    List<Enroll> enrolls = new ArrayList<>();
    String sql = "SELECT sc.student_id, sc.course_id, sc.enrollment_date, sc.progress, " +
        "u.name AS student_name, u.email AS student_email " +
        "FROM students_courses sc " +
        "JOIN users u ON sc.student_id = u.user_id " +
        "WHERE sc.course_id = ? AND u.user_type = 'STUDENT'";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idCourse);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          Course course = new Course();
          course.setIdCourse(rs.getInt("course_id"));

          Student student = new Student();
          student.setIdUser(rs.getInt("student_id"));
          student.setName(rs.getString("student_name"));
          student.setEmail(rs.getString("student_email"));

          Enroll enroll = new Enroll();
          enroll.setStudent(student);
          enroll.setCourse(course);
          enroll.setEnrollDate(rs.getTimestamp("enrollment_date").toLocalDateTime());
          enroll.setProgress(rs.getDouble("progress"));

          enrolls.add(enroll);
        }
      }
    }
    return enrolls;
  }

  /**
   * Atualiza o progresso de um aluno em um curso.
   *
   * @param idStudent ID do aluno.
   * @param idCourse ID do curso.
   * @param progress Novo valor do progresso (0.0 a 1.0).
   * @return true se o progresso foi atualizado, false caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean updateProgress(int idStudent, int idCourse, double progress) throws SQLException {
    String sql = "UPDATE students_courses SET progress = ? WHERE student_id = ? AND course_id = ?";

    if (progress < 0.0 || progress > 1.0) {
      System.err.println("Attempt to update progress to invalid value: " + progress);
    }

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setDouble(1, progress);
      pstmt.setInt(2, idStudent);
      pstmt.setInt(3, idCourse);
      return pstmt.executeUpdate() > 0;
    }
  }

  /**
   * Cancela a matrícula de um aluno em um curso.
   *
   * @param idStudent ID do aluno.
   * @param idCourse ID do curso.
   * @return true se a matrícula foi cancelada, false caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean cancelEnroll(int idStudent, int idCourse) throws SQLException {
    String sql = "DELETE FROM students_courses WHERE student_id = ? AND course_id = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idStudent);
      pstmt.setInt(2, idCourse);
      return pstmt.executeUpdate() > 0;
    }
  }
}

