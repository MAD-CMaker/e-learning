package com.elearning.remoteensine.dao;

import com.elearning.remoteensine.util.DatabaseConnector;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractDAO {

    protected final DatabaseConnector databaseConnector;

    public AbstractDAO(DatabaseConnector databaseConnector) {
        this.databaseConnector = databaseConnector;
    }

    protected Connection getConnection() throws SQLException {
        return databaseConnector.getConnection();
    }
}
