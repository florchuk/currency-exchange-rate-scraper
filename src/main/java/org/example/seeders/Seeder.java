package org.example.seeders;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

public interface Seeder {
    /**
     * (Re)seeding of the database tables (if they are not seeded already).
     * @param connection Database connection.
     * @throws IOException If an I/O error occurs.
     * @throws SQLException If an SQL error occurs.
     * @throws NullPointerException If the seeder resource could not be found.
     */
    void up(Connection connection) throws IOException, SQLException, NullPointerException;
}