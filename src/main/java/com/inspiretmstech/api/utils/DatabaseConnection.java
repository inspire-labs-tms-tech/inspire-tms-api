package com.inspiretmstech.api.utils;

import com.inspiretmstech.api.constants.Environment;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;

public class DatabaseConnection {

    private final String host;
    private final int port;
    private final String username;
    private final String password;
    private final String database;
    private final Connection connection;
    private final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    private static DatabaseConnection instance = null;

    private DatabaseConnection() throws SQLException {
        this.host = Environment.get(Environment.Variables.DB_HOST);
        this.port = Integer.parseInt(Environment.get(Environment.Variables.DB_PORT));
        this.username = Environment.get(Environment.Variables.DB_USER);
        this.password = Environment.get(Environment.Variables.DB_PASS);
        this.database = Environment.get(Environment.Variables.DB_NAME);
        this.connection = DriverManager.getConnection(this.getConnectionString(), this.username, this.password);

        // test the connection
        if(this.connection.isValid(5)) logger.info("validated connection for {}", this);
        else throw new RuntimeException("unable to validate connection for " + this);
    }

    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if(Objects.isNull(instance)) instance = new DatabaseConnection();
        return instance;
    }

    public <T> Optional<T> with(DatabaseExecutor<T> executor) {
        try {
            return Optional.ofNullable(executor.with(DSL.using(this.connection, SQLDialect.POSTGRES)));
        } catch (Exception e) {
            this.logger.warn("a database connection failed to execute an executor: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private String getConnectionString() {
        return "jdbc:postgresql://" + this.host + ":" + this.port + "/" + this.database + "?prepareThreshold=0";
    }

    @Override
    public String toString() {
        return "DatabaseConnection{" +
               "host='" + this.host + '\'' +
               ", port=" + this.port +
               ", username='" + this.username + '\'' +
               ", password='" + ("*").repeat(this.password.length()) + '\'' +
               ", database='" + this.database + '\'' +
               '}';
    }
}
