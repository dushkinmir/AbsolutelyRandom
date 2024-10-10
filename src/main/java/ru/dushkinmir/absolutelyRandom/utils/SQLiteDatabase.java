package ru.dushkinmir.absolutelyRandom.utils;

import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLiteDatabase {
    private final Plugin plugin;
    private Connection connection;

    public SQLiteDatabase(Plugin plugin) {
        this.plugin = plugin;
        open();
    }

    public void open() {
        try {
            // Создаем папку для базы данных, если она не существует
            File dataFolder = new File(plugin.getDataFolder(), "data");
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }

            // Путь к базе данных
            String url = "jdbc:sqlite:" + new File(dataFolder, "database.db").getPath();
            connection = DriverManager.getConnection(url);
            plugin.getLogger().info("База данных успешно открыта!");
            createTable(); // Создаем таблицы, если они не существуют
        } catch (SQLException e) {
            plugin.getLogger().severe("Не удалось открыть базу данных!");
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        return connection;
    }

    public void close() {
        try {
            if (connection != null) {
                connection.close();
                plugin.getLogger().info("База данных успешно закрыта!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS analFissures ("
                + "playerName TEXT PRIMARY KEY,"
                + "sleeps INTEGER DEFAULT 0"
                + ");";
        connection.createStatement().execute(sql);
    }
}
