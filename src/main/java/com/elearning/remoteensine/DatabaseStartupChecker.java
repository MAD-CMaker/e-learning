package com.elearning.remoteensine;

import com.elearning.remoteensine.util.DatabaseConnector;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;

@Component
public class DatabaseStartupChecker {

    @Autowired
    private DatabaseConnector databaseConnector;

    @PostConstruct
    public void init() {
        try (Connection conn = databaseConnector.getConnection()) {
            System.out.println("Conexão com banco OK.");
        } catch (SQLException e) {
            System.err.println("Erro na conexão: " + e.getMessage());
        }
    }
}
