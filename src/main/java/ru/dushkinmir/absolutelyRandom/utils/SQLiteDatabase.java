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
                boolean created = dataFolder.mkdirs(); // Сохраняем результат
                if (created) {
                    plugin.getLogger().info("Папка данных успешно создана: " + dataFolder.getPath());
                } else {
                    plugin.getLogger().warning("Не удалось создать папку данных: " + dataFolder.getPath());
                }
            }

            // Путь к базе данных
            String url = "jdbc:sqlite:" + new File(dataFolder, "database.db").getPath();
            connection = DriverManager.getConnection(url);
            plugin.getLogger().info("База данных успешно открыта!");
        } catch (SQLException e) {
            plugin.getLogger().severe("Не удалось открыть базу данных!");
            plugin.getLogger().severe("Ошибка: " + e.getMessage()); // Логируем сообщение об ошибке
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
            plugin.getLogger().severe("Не удалось закрыть базу данных!");
            plugin.getLogger().severe("Ошибка: " + e.getMessage()); // Логируем сообщение об ошибке
        }
    }
}
