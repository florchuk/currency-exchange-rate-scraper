package org.example.migrators;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public interface Migrator {
    /**
     * (Re)creation of the database tables (if they are not created already).
     * @param connection Database connection.
     * @throws IOException If an I/O error occurs.
     * @throws SQLException If an SQL error occurs.
     * @throws NullPointerException If the migrator resource could not be found.
     */
    void up(Connection connection) throws IOException, SQLException, NullPointerException;

    /**
     * Dropping of the database tables (if they are not already dropped).
     * @param connection Database connection.
     * @throws IOException If an I/O error occurs.
     * @throws SQLException If an SQL error occurs.
     * @throws NullPointerException If the migrator resource could not be found.
     */
    void down(Connection connection) throws IOException, SQLException, NullPointerException;
}