package com.elearning.remoteensine;

import com.elearning.remoteensine.util.DatabaseConnector;
import jakarta.annotation.PostConstruct;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseStartupChecker {

    private final DatabaseConnector databaseConnector;

    public DatabaseStartupChecker(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void checkDatabaseConnection() {
        try (Connection conn = databaseConnector.getConnection()) {
            System.out.println("✅ Conexão com o banco estabelecida com sucesso no startup.");
        } catch (SQLException e) {
            System.err.println("❌ Falha ao conectar com o banco no startup: " + e.getMessage());
        }
    }
}
