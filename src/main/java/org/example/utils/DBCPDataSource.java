package org.example.utils;

import org.apache.commons.dbcp2.BasicDataSource;

import java.io.IOException;

public class DBCPDataSource {
    private static BasicDataSource dataSource;

    private DBCPDataSource() {
        //
    }

    /**
     * Getting instance of the data source.
     * @return Instance of the data source.
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If a null value was provided, when the Object are expected.
     */
    public static BasicDataSource getDataSource() throws IOException, NullPointerException {
        if (DBCPDataSource.dataSource == null || DBCPDataSource.dataSource.isClosed()) {
            DBCPDataSource.createDataSource();
        }

        return DBCPDataSource.dataSource;
    }

    /**
     * Creating the instance of the data source, if there is no one, or data source is already closed.
     * @throws IOException If an I/O error occurs.
     * @throws NullPointerException If a null value was provided, when the Object are expected.
     */
    private static void createDataSource() throws IOException, NullPointerException {
        if (DBCPDataSource.dataSource == null || DBCPDataSource.dataSource.isClosed()) {
            DBCPDataSource.dataSource = new BasicDataSource();

            DBCPDataSource.dataSource.setUrl(Config.getProperty("database.url"));
            DBCPDataSource.dataSource.setUsername(Config.getProperty("database.username"));
            DBCPDataSource.dataSource.setPassword(Config.getProperty("database.password"));
            DBCPDataSource.dataSource.setDefaultAutoCommit(false);
        }
    }
}