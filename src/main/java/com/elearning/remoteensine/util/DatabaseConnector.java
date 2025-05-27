package com.elearning.remoteensine.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
  // URL de conexão:
  private static final String URL = "jdbc:mysql://localhost:3306/ensinoremoto_tcc?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
  private static final String USER = "leonardo"; // Substitua pelo seu usuário do MySQL
  private static final String PASSWORD = "leonardo"; // Substitua pela sua senha do MySQL

  public static Connection getConnection() throws SQLException {
    try {
      // Registro o driver JDBC do MySQL.
      Class.forName("com.mysql.cj.jdbc.Driver");
      return DriverManager.getConnection(URL, USER, PASSWORD);
    } catch (ClassNotFoundException e) {
      throw new SQLException("Driver JDBC do MySQL não encontrado! Verifique o Classpath.", e);
    }
  }

  // Testar conexão
  public static void main(String[] args) {
    try (Connection connection = getConnection()) {
      if (connection != null) {
        System.out.println("Conexão com o banco de dados estabelecida com sucesso!");
      } else {
        System.out.println("Falha ao estabelecer conexão com o banco de dados.");
      }
    } catch (SQLException e) {
      System.err.println("Erro ao conectar com o banco de dados: " + e.getMessage());
      e.printStackTrace();
    }
  }
}
