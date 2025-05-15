package com.onyell.database;

/**
 * @author oNyell
 * GitHub: https://github.com/oNyell
 * Discord: onyell
 */

import com.onyell.Main;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

public class MySQLDatabase extends Database {

    private Connection connection;
    private ExecutorService executor;
    private String host;
    private String port;
    private String database;
    private String username;
    private String password;

    public MySQLDatabase() {
        this.host = Main.getInstance().getConfig().getString("database.mysql.host");
        this.port = Main.getInstance().getConfig().getString("database.mysql.porta");
        this.database = Main.getInstance().getConfig().getString("database.mysql.nome");
        this.username = Main.getInstance().getConfig().getString("database.mysql.usuario");
        this.password = Main.getInstance().getConfig().getString("database.mysql.senha");

        this.executor = Executors.newCachedThreadPool();
        openConnection();
        update("CREATE TABLE IF NOT EXISTS `profiles` (`name` VARCHAR(16) NOT NULL, `discord` VARCHAR(30) DEFAULT NULL) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE utf8_bin;");
        
        try {
            update("UPDATE `profiles` SET `discord` = NULL WHERE `discord` = ''");
        } catch (Exception e) {
            Main.getInstance().getLogger().log(Level.WARNING, "Não foi possível converter registros vazios para NULL: ", e);
        }
    }

    public void openConnection() {
        if (!isConnected()) {
            try {
                boolean bol = connection == null;
                connection = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + "/" + database
                                + "?verifyServerCertificate=false&useSSL=false&useUnicode=yes&characterEncoding=UTF-8",
                        username, password);
                if (bol) {
                    Main.getInstance().getLogger().info("Conectado ao MySQL!");
                    return;
                }

                Main.getInstance().getLogger().info("Reconectado ao MySQL!");
            } catch (SQLException e) {
                Main.getInstance().getLogger().log(Level.SEVERE, "Could not open MySQL connection: ", e);
            }
        }
    }

    @Override
    public void closeConnection() {
        if (isConnected()) {
            try {
                connection.close();
            } catch (SQLException e) {
                Main.getInstance().getLogger().log(Level.SEVERE, "Could not close MySQL connection: ", e);
            }
        }
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            Main.getInstance().getLogger().log(Level.SEVERE, "MySQL error: ", e);
        }

        return false;
    }

    @Override
    public void update(String sql, Object... vars) {
        try {
            PreparedStatement ps = prepareStatement(sql, vars);
            ps.execute();
            ps.close();
        } catch (SQLException e) {
            Main.getInstance().getLogger().log(Level.WARNING, "Could not execute SQL: ", e);
        }
    }

    @Override
    public void execute(String sql, Object... vars) {
        executor.execute(() -> {
            update(sql, vars);
        });
    }

    public PreparedStatement prepareStatement(String query, Object... vars) {
        try {
            PreparedStatement ps = getConnection().prepareStatement(query);
            for (int i = 0; i < vars.length; i++) {
                ps.setObject(i + 1, vars[i]);
            }
            return ps;
        } catch (SQLException e) {
            Main.getInstance().getLogger().log(Level.WARNING, "Could not Prepare Statement: ", e);
        }

        return null;
    }

    @Override
    public CachedRowSet query(String query, Object... vars) {
        CachedRowSet rowSet = null;
        try {
            Future<CachedRowSet> future = executor.submit(() -> {
                try {
                    PreparedStatement ps = prepareStatement(query, vars);

                    ResultSet rs = ps.executeQuery();
                    CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
                    crs.populate(rs);
                    rs.close();
                    ps.close();

                    if (crs.next()) {
                        return crs;
                    }
                } catch (Exception e) {
                    Main.getInstance().getLogger().log(Level.WARNING, "Could not Execute Query: ", e);
                }

                return null;
            });

            if (future.get() != null) {
                rowSet = future.get();
            }
        } catch (Exception e) {
            Main.getInstance().getLogger().log(Level.WARNING, "Could not Call FutureTask: ", e);
        }

        return rowSet;
    }

    @Override
    public Connection getConnection() {
        if (!isConnected()) {
            openConnection();
        }

        return connection;
    }

    @Override
    public List<String[]> getUsers(String table, String... columns) {
        return null;
    }
}
