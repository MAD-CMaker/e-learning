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

  public Connection getConnection() throws SQLException {
    System.out.println("URL utilizada: " + URL);
    System.out.println("USER: " + USER);
    System.out.println("Tentando conectar...");
    Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
    System.out.println("✅ Conectado com sucesso!");
    return conn;
//      return DriverManager.getConnection(URL, USER, PASSWORD);
  }
}
