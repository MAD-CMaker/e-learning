package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.Exercise;
import com.elearning.remoteensine.model.enums.ExerciseType;
import com.elearning.remoteensine.util.DatabaseConnector;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ExerciseDAO extends AbstractDAO {

  public ExerciseDAO(DatabaseConnector databaseConnector) {
    super(databaseConnector);
  }

  /**
   * Salva um novo exercício no banco de dados.
   *
   * @param exercise O objeto Exercicio a ser salvo. O idAula deve estar preenchido.
   * @return O objeto Exercicio com o ID preenchido pelo banco de dados.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public Exercise saveExercise(Exercise exercise) throws SQLException {
    String sql = "INSERT INTO exercises (classroom_id, statement, exercise_type, correct_answer) " +
        "VALUES (?, ?, ?, ?)";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      if (exercise.getClassroomId() <= 0) {
        throw new SQLException("\n" +
            "Invalid or not defined class ID for the exercise.");
      }
      pstmt.setInt(1, exercise.getClassroomId());
      pstmt.setString(2, exercise.getStatement());
      pstmt.setString(3, exercise.getExerciseType().name());
      pstmt.setString(4, exercise.getCorrectAnswer());

      int affectedRows = pstmt.executeUpdate();

      if (affectedRows == 0) {
        throw new SQLException("Failed to create exercise, no rows affected.");
      }

      try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          exercise.setExerciseId(generatedKeys.getInt(1));
        } else {
          throw new SQLException("Failed to create exercise, no ID obtained.");
        }
      }
      return exercise;
    }
  }

  /**
   * Busca um exercício pelo seu ID.
   *
   * @param idExercise O ID do exercício a ser buscado.
   * @return Um objeto Exercicio se encontrado, ou null caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public Exercise searchExerciseById(int idExercise) throws SQLException {
    String sql = "SELECT * FROM exercises WHERE exercise_id = ?";
    Exercise exercise = null;

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, idExercise);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        exercise = mapearResultSetParaExercicio(rs);
      }
    }
    return exercise;
  }

  /**
   * Lista todos os exercícios de uma aula específica.
   *
   * @param idClassroom O ID da aula.
   * @return Uma lista de Exercicios.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public List<Exercise> listExercisesByClass(int idClassroom) throws SQLException {
    List<Exercise> exercises = new ArrayList<>();
    String sql = "SELECT * FROM exercises WHERE classroom_id = ? ORDER BY exercise_id ASC"; // Ou alguma coluna de ordem se existir

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setInt(1, idClassroom);
      try (ResultSet rs = pstmt.executeQuery()) {
        while (rs.next()) {
          exercises.add(mapearResultSetParaExercicio(rs));
        }
      }
    }
    return exercises;
  }

  /**
   * Atualiza os dados de um exercício existente no banco.
   *
   * @param exercise O objeto Exercicio com os dados atualizados.
   * @return true se a atualização foi bem-sucedida, false caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean updateExercise(Exercise exercise) throws SQLException {
    String sql = "UPDATE exercises SET classroom_id = ?, statement = ?, exercise_type = ?, correct_answer = ? " +
        "WHERE exercise_id = ?";

    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      if (exercise.getClassroomId() <= 0) {
        throw new SQLException("Invalid or not defined class ID for the exercise.");
      }
      pstmt.setInt(1, exercise.getClassroomId());
      pstmt.setString(2, exercise.getStatement());
      pstmt.setString(3, exercise.getExerciseType().name());
      pstmt.setString(4, exercise.getCorrectAnswer());
      pstmt.setInt(5, exercise.getExerciseId());

      return pstmt.executeUpdate() > 0;
    }
  }

  /**
   * Deleta um exercício do banco de dados pelo seu ID.
   *
   * @param idExercise O ID do exercício a ser deletado.
   * @return true se a deleção foi bem-sucedida, false caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean deleteExercise(int idExercise) throws SQLException {
    String sql = "DELETE FROM exercises WHERE exercise_id = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idExercise);
      return pstmt.executeUpdate() > 0;
    }
  }

  /**
   * Deleta todos os exercícios de uma aula específica.
   *
   * @param idClassroom O ID da aula cujos exercícios serão deletados.
   * @return O número de exercícios deletados.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public int deleteExerciseByClass(int idClassroom) throws SQLException {
    String sql = "DELETE FROM exercises WHERE classroom_id = ?";
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setInt(1, idClassroom);
      return pstmt.executeUpdate();
    }
  }

  /**
   * Método auxiliar para mapear um ResultSet para um objeto Exercicio.
   */
  private Exercise mapearResultSetParaExercicio(ResultSet rs) throws SQLException {
    Exercise exercise = new Exercise();
    exercise.setExerciseId(rs.getInt("exercise_id"));
    exercise.setClassroomId(rs.getInt("classroom_id"));
    exercise.setStatement(rs.getString("statement"));
    exercise.setExerciseType(ExerciseType.valueOf(rs.getString("exercise_type")));
    exercise.setCorrectAnswer(rs.getString("correct_answer"));
    return exercise;
  }
}
