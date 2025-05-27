package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.Professor;
import com.elearning.remoteensine.model.Course;
import com.elearning.remoteensine.util.DatabaseConnector;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CourseDAO {
  /**
   * Salva um novo curso no banco de dados.
   *
   * @param course O objeto Curso a ser salvo. O id_professor_responsavel deve estar setado no objeto Professor dentro de Curso.
   * @return O objeto Curso com o ID preenchido pelo banco de dados.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public Course saveCourse(Course course) throws SQLException {
    String sql = "INSERT INTO courses (title, description, responsible_professor_id , price, video_presentation_url, category, load_hour, creation_date) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      pstmt.setString(1, course.getTitle());;
      pstmt.setString(2, course.getDescription());
      if (course.getResponsibleProfessor() != null) {
        pstmt.setInt(3, course.getResponsibleProfessor().getIdUser());
      } else {
        throw new SQLException("Responsible teacher cannot be null to save the course.");
      }
      pstmt.setDouble(4, course.getPrice());
      pstmt.setString(5, course.getPresentationVideo());
      pstmt.setString(6, course.getCategory());
      pstmt.setInt(7, course.getHoursLoad());
      pstmt.setTimestamp(8, Timestamp.valueOf(course.getCreationDate() != null ? course.getCreationDate() : LocalDateTime.now()));


      int affectedRows = pstmt.executeUpdate();

      if (affectedRows == 0) {
        throw new SQLException("\n" +
            "Failed to create course, no rows affected.");
      }
      try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          course.setIdCourse(generatedKeys.getInt(1));
        } else {
          throw new SQLException("Failed to create course, no ID obtained.");
        }
      }
      return course;
    }
  }
  /**
   * Busca um curso pelo seu ID.
   * O objeto Professor dentro do Curso retornado terá apenas o ID do professor preenchido por este método.
   * O Service pode optar por carregar o objeto Professor completo.
   *
   * @param idCourse O ID do curso a ser buscado.
   * @return Um objeto Curso se encontrado, ou null caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public Course searchCourseById(int idCourse) throws SQLException {
    String sql = "SELECT * FROM courses WHERE course_id = ?";
    Course course = null;

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, idCourse);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        course = mapResultSetForCourse(rs);
      }
    }
    return course;
  }
  /**
   * Lista todos os cursos cadastrados.
   * O objeto Professor dentro de cada Curso terá apenas o ID do professor preenchido.
   *
   * @return Uma lista de Cursos.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public List<Course> listAllCourses() throws SQLException {
    List<Course> courses = new ArrayList<>();
    String sql = "SELECT * FROM courses ORDER BY title";
    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {
      int count = 0;
      while (rs.next()) {
        count++;
        Course course = mapResultSetForCourse(rs);
        courses.add(course);
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw e;
    }
    return courses;
  }
  /**
   * Lista todos os cursos criados por um professor específico.
   * O objeto Professor dentro de cada Curso terá apenas o ID do professor preenchido.
   *
   * @param idProfessor O ID do professor.
   * @return Uma lista de Cursos.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public List<Course> listCoursesByProfessor(int idProfessor) throws SQLException {
    List<Course> courses = new ArrayList<>();
    String sql = "SELECT * FROM courses WHERE responsible_professor_id = ? ORDER BY title";

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, idProfessor);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          courses.add(mapResultSetForCourse(rs));
        }
      }
    }
    return courses;
  }
  /**
   * Atualiza os dados de um curso existente no banco.
   *
   * @param course O objeto Curso com os dados atualizados.
   * @return true se a atualização foi bem-sucedida, false caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean attCourses(Course course) throws SQLException {
    String sql = "UPDATE courses SET title = ?, description = ?, responsible_professor_id = ?, price = ?, " +
        "video_presentation_url = ?, category = ?, load_hour = ?, update_date = CURRENT_TIMESTAMP " +
        "WHERE id_course = ?";

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, course.getTitle());
      pstmt.setString(2, course.getDescription());
      if (course.getResponsibleProfessor() != null) {
        pstmt.setInt(3, course.getResponsibleProfessor().getIdUser());
      } else {
        throw new SQLException("\n" +
            "Responsible teacher cannot be null to update the course.");
      }
      pstmt.setDouble(4, course.getPrice());
      pstmt.setString(5, course.getPresentationVideo());
      pstmt.setString(6, course.getCategory());
      pstmt.setInt(7, course.getHoursLoad());
      pstmt.setInt(8, course.getIdCourse());

      return pstmt.executeUpdate() > 0;
    }
  }
  /**
   * Deleta um curso do banco de dados pelo seu ID.
   *
   * @param idCourse O ID do curso a ser deletado.
   * @return true se a deleção foi bem-sucedida, false caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean deleteCourse(int idCourse) throws SQLException {
    String sql = "DELETE FROM courses WHERE id_course = ?";
    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idCourse);
      return pstmt.executeUpdate() > 0;
    }
  }
  /**
   * Método auxiliar para mapear um ResultSet para um objeto Curso.
   * O ProfessorResponsavel terá apenas o ID preenchido.
   */
  private Course mapResultSetForCourse(ResultSet rs) throws SQLException {
    Course course = new Course();
    course.setIdCourse(rs.getInt("course_id"));
    course.setTitle(rs.getString("title"));
    course.setDescription(rs.getString("description"));
    course.setPrice(rs.getDouble("price"));
    course.setPresentationVideo(rs.getString("video_presentation_url"));
    course.setCategory(rs.getString("category"));
    course.setHoursLoad(rs.getInt("load_hour"));
    course.setCreationDate(rs.getTimestamp("creation_date").toLocalDateTime());
    Timestamp updateDateTs = rs.getTimestamp("update_date");
    if (updateDateTs != null) {
      course.setUpdateDate(updateDateTs.toLocalDateTime());
    }

    Professor professor = new Professor();
    professor.setIdUser(rs.getInt("responsible_professor_id"));
    course.setResponsibleProfessor(professor);

    return course;
  }
}
