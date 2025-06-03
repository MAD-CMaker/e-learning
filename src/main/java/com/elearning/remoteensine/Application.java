package com.elearning.remoteensine;

import com.elearning.remoteensine.util.DatabaseConnector;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.sql.Connection;
import java.sql.SQLException;

@SpringBootApplication
public class Application {
  public static void main(String[] args) {

    ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
    DatabaseConnector connector = context.getBean(DatabaseConnector.class);
    try (Connection conn = connector.getConnection()) {
      System.out.println("✅ Teste direto no main: conexão bem-sucedida!");
    } catch (SQLException e) {
      System.err.println("❌ Erro ao conectar no main: " + e.getMessage());
    }
  }
}