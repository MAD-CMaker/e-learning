package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.util.DatabaseConnector;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractDAO {

    protected final DatabaseConnector databaseConnector;

    @Autowired
    public AbstractDAO(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
    }

    protected Connection getConnection() throws SQLException {
        return databaseConnector.getConnection();
    }
}
