package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.model.NewsLetterInscription;
import com.elearning.remoteensine.util.DatabaseConnector;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NewsletterDAO {

  /**
   * Salva uma nova inscrição de e-mail na newsletter.
   *
   * @param inscription O objeto NewsletterInscricao a ser salvo.
   * @return O objeto NewsletterInscricao com o ID preenchido.
   * @throws SQLException Se ocorrer um erro, incluindo violação da constraint UNIQUE para e-mail.
   */
  public NewsLetterInscription saveInscription(NewsLetterInscription inscription) throws SQLException {
    String sql = "INSERT INTO newsletter_inscription(email, inscription_hour, active) VALUES (?, ?, ?)";

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

      pstmt.setString(1, inscription.getEmail());
      pstmt.setTimestamp(2, Timestamp.valueOf(inscription.getInscriptionDate() != null ? inscription.getInscriptionDate() : LocalDateTime.now()));
      pstmt.setBoolean(3, inscription.isActive());

      int affectedRows = pstmt.executeUpdate();

      if (affectedRows == 0) {
          throw new SQLException("Failed to save newsletter subscription, no rows affected.");
      }

      try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          inscription.setInscriptionId(generatedKeys.getInt(1));
        } else {
          throw new SQLException("Failed to save newsletter subscription, no ID obtained.");
        }
      }
      return inscription;
    }
  }

  /**
   * Busca uma inscrição na newsletter pelo e-mail.
   *
   * @param email O e-mail a ser buscado.
   * @return A NewsletterInscricao se encontrada, ou null.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public NewsLetterInscription searchByEmail(String email) throws SQLException {
    String sql = "SELECT * FROM newsletter_inscription WHERE email = ?";
    NewsLetterInscription inscription = null;

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {

      pstmt.setString(1, email);
      ResultSet rs = pstmt.executeQuery();

      if (rs.next()) {
        inscription = mapResultSetForInscriptions(rs);
      }
    }
    return inscription;
  }

  /**
   * Atualiza o status de uma inscrição (ex: para desativar).
   *
   * @param inscription O objeto NewsletterInscricao com o status atualizado.
   * @return true se a atualização foi bem-sucedida, false caso contrário.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public boolean updateInscriptionStatus(NewsLetterInscription inscription) throws SQLException {
    String sql = "UPDATE newsletter_inscription SET active = ? WHERE inscription_id = ?";
    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
      pstmt.setBoolean(1, inscription.isActive());
      pstmt.setInt(2, inscription.getInscriptionId());
      return pstmt.executeUpdate() > 0;
    }
  }

  /**
   * Lista todos os e-mails inscritos (opcionalmente, apenas os ativos).
   *
   * @param activeOnly Se true, lista apenas inscrições ativas.
   * @return Lista de NewsletterInscricao.
   * @throws SQLException Se ocorrer um erro de acesso ao banco de dados.
   */
  public List<NewsLetterInscription> listAllInscriptions(boolean activeOnly) throws SQLException {
    List<NewsLetterInscription> inscriptions = new ArrayList<>();
    String sql = "SELECT * FROM newsletter_inscription";
    if (activeOnly) {
      sql += " WHERE active = TRUE";
    }
    sql += " ORDER BY inscription_hour DESC";

    try (Connection conn = DatabaseConnector.getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql);
         ResultSet rs = pstmt.executeQuery()) {

      while (rs.next()) {
        inscriptions.add(mapResultSetForInscriptions(rs));
      }
    }
    return inscriptions;
  }


  private NewsLetterInscription mapResultSetForInscriptions(ResultSet rs) throws SQLException {
    NewsLetterInscription inscription = new NewsLetterInscription();
    inscription.setInscriptionId(rs.getInt("inscription_id"));
    inscription.setEmail(rs.getString("email"));
    Timestamp tsCreationDate = rs.getTimestamp("inscription_hour");
    if (tsCreationDate != null) {
      inscription.setInscriptionDate(tsCreationDate.toLocalDateTime());
    }
    inscription.setActive(rs.getBoolean("active"));
    return inscription;
  }
}

