package com.meretskiy.cloud.storage.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.concurrent.*;

public class DataBaseAuthService {

    private static Connection conn;
    private static Statement statement;
    private static final Logger logger = LogManager.getLogger(DataBaseAuthService.class);
    private ExecutorService exServ = Executors.newFixedThreadPool(1);

    public void stop() {
        exServ.shutdown();
    }

    private static void connection() {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:cloud-server/nsAuthBase.db");
            statement = conn.createStatement();
            logger.info("Service of authentication is run");
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
            logger.warn(e);

        }
    }

    private static void disconnect() {
        try {
            conn.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            logger.warn(throwables);
        }
    }

    private static class DirectoryOfUser implements Callable<String> {
        String loginEntry;
        String passEntry;

        public DirectoryOfUser(String loginEntry, String passEntry) {
            this.loginEntry = loginEntry;
            this.passEntry = passEntry;
        }

        @Override
        public String call() {
            String directory = null;
            try {
                connection();
                ResultSet rs = statement.executeQuery("SELECT * FROM users_info " +
                        "WHERE login = '" + loginEntry + "' AND password = '" + passEntry + "' LIMIT 1");
                while (rs.next()) {
                    directory = rs.getString("login");
                    rs.close();
                }
                disconnect();
            } catch (SQLException e) {
                e.printStackTrace();
                logger.warn(e);
            }
            return directory;
        }
    }

    public String getDirectoryByLoginPass(String loginEntry, String passEntry) {
        String directory = null;
        Future<String> future = exServ.submit(new DirectoryOfUser(loginEntry, passEntry));
        try {
            directory = future.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            logger.warn(e);
        }
        return directory;
    }

}
