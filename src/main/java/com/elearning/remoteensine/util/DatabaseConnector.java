package com.elearning.remoteensine.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Component
public class DatabaseConnector {

  @Value("${spring.datasource.url}")
  private String URL;

  @Value("${spring.datasource.username}")
  private String USER; //

  @Value("${spring.datasource.password}")
  private String PASSWORD;

  @Value("${spring.datasource.driver-class-name}")
  private String DRIVER_CLASSNAME = "com.mysql.cj.jdbc.Driver";

  public Connection getConnection() throws SQLException {
    try {
      // Registro o driver JDBC do MySQL.
      Class.forName(DRIVER_CLASSNAME);
      return DriverManager.getConnection(URL, USER, PASSWORD);
    } catch (ClassNotFoundException e) {
      throw new SQLException("Driver JDBC do MySQL não encontrado! Verifique o Classpath.", e);
    }
  }

//  // Testar conexão
//  public static void main(String[] args) {
//    try (Connection connection = getConnection()) {
//      if (connection != null) {
//        System.out.println("Conexão com o banco de dados estabelecida com sucesso!");
//      } else {
//        System.out.println("Falha ao estabelecer conexão com o banco de dados.");
//      }
//    } catch (SQLException e) {
//      System.err.println("Erro ao conectar com o banco de dados: " + e.getMessage());
//      e.printStackTrace();
//    }
//  }
}
