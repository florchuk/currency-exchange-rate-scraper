package org.example;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Main {
    public static void main(String[] args) {
        // Creating the logger.
        Logger logger = LogManager.getLogger(Main.class);

        try {
            // Run the application.
            Application.run(logger);
        } catch (Exception e) {
            logger.fatal(e.getMessage(), e);
        }
    }
}