package com.elearning.remoteensine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
    System.out.println("\nSua aplicação de Ensino Remoto está rodando!");
    System.out.println("Acesse em seu navegador: http://localhost:8080");
  }
}