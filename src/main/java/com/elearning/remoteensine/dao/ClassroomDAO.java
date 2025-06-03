package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.Classroom;
import com.elearning.remoteensine.util.DatabaseConnector;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ClassroomDAO extends AbstractDAO{

  public ClassroomDAO(DatabaseConnector databaseConnector) {
    super(databaseConnector);
  }

  /**
   * Salva uma nova aula no banco de dados.
   *
   * @param classroom O objeto Aula a ser salvo. O idCurso deve estar preenchido.
   * @return O objeto Aula com o ID preenchido pelo banco de dados.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public Classroom saveClassroom(Classroom classroom) throws SQLException {
    String sql = "INSERT INTO classes (course_id, title, description, content_url, sequence) VALUES (?, ?, ?, ?, ?)";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      if (classroom.getCourseId() <= 0) {
        throw new SQLException("Invalid or not defined course ID for class.");
      }
      pstmt.setInt(1, classroom.getCourseId());
      pstmt.setString(2, classroom.getTitle());
      pstmt.setString(3, classroom.getDescription());
      pstmt.setString(4, classroom.getContentURL());
      pstmt.setInt(5, classroom.getSequence());

      int affectedRows = pstmt.executeUpdate();

      if (affectedRows == 0) {
        throw new SQLException("Failed to create class, no rows affected.");
      }

      try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          classroom.setClassroomId(generatedKeys.getInt(1));
        } else {
          throw new SQLException("\n" +
              "Failed to create class, no ID obtained.");
        }
      }
      return classroom;
    }
  }

  /**
   * Busca uma aula pelo seu ID.
   *
   * @param idClassroom O ID da aula a ser buscada.
   * @return Um objeto Aula se encontrado, ou null caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public Classroom searchById(int idClassroom) throws SQLException {
    String sql = "SELECT * FROM classes WHERE classroom_id = ?";
    Classroom classroom = null;

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, idClassroom);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        classroom = mapResultSetByClass(rs);
      }
    }
    return classroom;
  }

  /**
   * Lista todas as aulas de um curso específico, ordenadas pela coluna 'ordem'.
   *
   * @param idCourse O ID do curso.
   * @return Uma lista de Aulas.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public List<Classroom> listClassesByCourse(int idCourse) throws SQLException {
    List<Classroom> classrooms = new ArrayList<>();
    String sql = "SELECT * FROM classes WHERE course_id = ? ORDER BY sequence ASC, classroom_id ASC";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, idCourse);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          classrooms.add(mapResultSetByClass(rs));
        }
      }
    }
    return classrooms;
  }

  /**
   * Atualiza os dados de uma aula existente no banco.
   *
   * @param classroom O objeto Aula com os dados atualizados.
   * @return true se a atualização foi bem-sucedida, false caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean upddateClass(Classroom classroom) throws SQLException {
    String sql = "UPDATE classes SET course_id = ?, title = ?, description = ?, content_url = ?, sequence = ? " +
        "WHERE classroom_id = ?";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      if (classroom.getCourseId() <= 0) {
        throw new SQLException("Invalid course ID or not defined for the class.");
      }
      pstmt.setInt(1, classroom.getCourseId());
      pstmt.setString(2, classroom.getTitle());
      pstmt.setString(3, classroom.getDescription());
      pstmt.setString(4, classroom.getContentURL());
      pstmt.setInt(5, classroom.getSequence());
      pstmt.setInt(6, classroom.getClassroomId());

      return pstmt.executeUpdate() > 0;
    }
  }

  /**
   * Deleta uma aula do banco de dados pelo seu ID.
   *
   * @param idClassroom O ID da aula a ser deletada.
   * @return true se a deleção foi bem-sucedida, false caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean deleteClass(int idClassroom) throws SQLException {
    String sql = "DELETE FROM classes WHERE classroom_id = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idClassroom);
      return pstmt.executeUpdate() > 0;
    }
  }

  /**
   * Deleta todas as aulas de um curso específico.
   * Útil quando um curso é deletado, para limpar as aulas associadas se o ON DELETE CASCADE não for usado
   * ou se for necessário um controle mais granular.
   *
   * @param idCourse O ID do curso cujas aulas serão deletadas.
   * @return O número de aulas deletadas.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public int deleteClassesByCourse(int idCourse) throws SQLException {
    String sql = "DELETE FROM classes WHERE classroom_id = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idCourse);
      return pstmt.executeUpdate();
    }
  }

  /**
   * Método auxiliar para mapear um ResultSet para um objeto Aula.
   */
  private Classroom mapResultSetByClass(ResultSet rs) throws SQLException {
    Classroom classroom = new Classroom();
    classroom.setClassroomId(rs.getInt("classroom_id"));
    classroom.setCourseId(rs.getInt("course_id"));
    classroom.setTitle(rs.getString("title"));
    classroom.setDescription(rs.getString("description"));
    classroom.setContentURL(rs.getString("content_url"));
    classroom.setSequence(rs.getInt("sequence"));
    return classroom;
  }
}
