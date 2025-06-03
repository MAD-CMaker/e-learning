package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.Professor;
import com.elearning.remoteensine.model.Student;
import com.elearning.remoteensine.model.User;
import com.elearning.remoteensine.model.enums.UserType;
import com.elearning.remoteensine.util.DatabaseConnector;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDateTime;

@Repository
public class UserDAO extends AbstractDAO {

  public UserDAO(DatabaseConnector databaseConnector) {
    super(databaseConnector);
  }

  /**
   * Salva um novo usuário (Student ou Professor) no banco de dados.
   * A senha do usuário já deve vir hasheada para este método.
   *
   * @param user O objeto Usuario (Student ou Professor) a ser salvo.
   * @return O objeto Usuario com o ID preenchido pelo banco de dados.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public User saveUser(User user) throws SQLException {
    String sql = "INSERT INTO users (name, email, password, user_type, specialization, register_hour) VALUES (?, ?, ?, ?, ?, ?)";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      pstmt.setString(1, user.getName());
      pstmt.setString(2, user.getEmail());
      pstmt.setString(3, user.getPassword()); // IMPORTANTE: A senha DEVE ser hasheada ANTES de chegar aqui.
      pstmt.setString(4, user.getUserType().name());

      if (user instanceof Professor) {
        pstmt.setString(5, ((Professor) user).getSpecialization());
      } else {
        pstmt.setNull(5, Types.VARCHAR);
      }

      if (user.getRegisterDate() != null) {
        pstmt.setTimestamp(6, Timestamp.valueOf(user.getRegisterDate()));
      } else {
        pstmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
      }

      int affectedRows = pstmt.executeUpdate();

      if (affectedRows == 0) {
        throw new SQLException("Failed to create user, no rows affected.");
      }

      // Recupera o ID gerado pelo banco
      try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          user.setIdUser(generatedKeys.getInt(1));
        } else {
          throw new SQLException("Failed to create user, no ID obtained.");
        }
      }
      return user;
    }
  }

  /**
   * Busca um usuário pelo seu endereço de e-mail.
   *
   * @param email O e-mail do usuário a ser buscado.
   * @return Um objeto Usuario (Student ou Professor) se encontrado, ou null caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public User searchByEmail(String email) throws SQLException {
    String sql = "SELECT * FROM users WHERE email = ?";
    User user = null;

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, email);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        user = mapResultSetParaUser(rs);
      }
    }
    return user;
  }

  /**
   * Busca um usuário pelo seu ID.
   *
   * @param idUser O ID do usuário a ser buscado.
   * @return Um objeto Usuario (Student ou Professor) se encontrado, ou null caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public User searchById(int idUser) throws SQLException {
    String sql = "SELECT * FROM users WHERE user_id = ?";
    User user = null;

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, idUser);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        user = mapResultSetParaUser(rs);
      }
    }
    return user;
  }

  /**
   * Método auxiliar para mapear um ResultSet para um objeto Usuario (Student ou Professor).
   * @param rs O ResultSet contendo os dados do usuário.
   * @return Um objeto Usuario (Student ou Professor).
   * @throws SQLException Se ocorrer um erro ao ler o ResultSet.
   */
  private User mapResultSetParaUser(ResultSet rs) throws SQLException {
    UserType tipo = UserType.valueOf(rs.getString("user_type"));
    User user;

    int id = rs.getInt("user_id");
    String name = rs.getString("name");
    String email = rs.getString("email");
    String password = rs.getString("password"); // Lembre-se que esta é a senha hasheada
    LocalDateTime registerHour = rs.getTimestamp("register_hour").toLocalDateTime();

    if (tipo == UserType.STUDENT) {
      Student student = new Student(name, email, password);
      student.setIdUser(id);
      student.setRegisterDate(registerHour);
      // Aqui você poderia carregar outros dados específicos do Student, como cursos matriculados,
      // chamando outros DAOs (ex: StudentCursoDAO)
      user = student;
    } else {
      String specialization = rs.getString("specialization");
      Professor professor = new Professor(name, email, password, specialization); // Construtor pode precisar de ajustes
      professor.setIdUser(id);
      professor.setRegisterDate(registerHour);
      user = professor;
    }
    return user;
  }
  /**
   * Atualiza os dados de um usuário existente no banco.
   * A senha, se modificada, já deve vir hasheada.
   *
   * @param user O objeto Usuario com os dados atualizados.
   * @return true se a atualização foi bem-sucedida, false caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean attUser(User user) throws SQLException {
    String sql = "UPDATE users SET name = ?, email = ?, password = ?, user_type = ?, specialization = ?, register_hour = ? WHERE user_id = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, user.getName());
      pstmt.setString(2, user.getEmail());
      pstmt.setString(3, user.getPassword());
      pstmt.setString(4, user.getUserType().name());

      if (user instanceof Professor) {
        pstmt.setString(5, ((Professor) user).getSpecialization());
      } else {
        pstmt.setNull(5, Types.VARCHAR);
      }
      pstmt.setTimestamp(6, Timestamp.valueOf(user.getRegisterDate() != null ? user.getRegisterDate() : LocalDateTime.now()));
      pstmt.setInt(7, user.getIdUser());

      return pstmt.executeUpdate() > 0;
    }
  }

  /**
   * Deleta um usuário do banco de dados pelo seu ID.
   *
   * @param idUser O ID do usuário a ser deletado.
   * @return true se a deleção foi bem-sucedida, false caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean deleteUser(int idUser) throws SQLException {
    String sql = "DELETE FROM users WHERE user_id = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idUser);
      return pstmt.executeUpdate() > 0;
    }
  }
}
