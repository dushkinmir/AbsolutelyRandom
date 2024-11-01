package ru.dushkinmir.absolutelyRandom.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class AbsRandSQLiteDatabase {
    private final Plugin plugin;
    private HikariDataSource dataSource;

    public AbsRandSQLiteDatabase(Plugin plugin) {
        this.plugin = plugin;
        setupDataSource();
    }

    private void setupDataSource() {
        try {
            // Создаем папку для базы данных, если она не существует
            File dataFolder = new File(plugin.getDataFolder(), "data");
            if (!dataFolder.exists()) {
                boolean created = dataFolder.mkdirs();
                if (created) {
                    plugin.getLogger().info("Папка данных успешно создана: " + dataFolder.getPath());
                } else {
                    plugin.getLogger().warning("Не удалось создать папку данных: " + dataFolder.getPath());
                }
            }

            // Путь к базе данных
            String url = "jdbc:sqlite:" + new File(dataFolder, "database.db").getPath();

            // Настройка HikariCP
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(url);
            config.setMaximumPoolSize(10); // Максимальное количество соединений в пуле
            config.setConnectionTimeout(30000); // Время ожидания соединения (30 секунд)

            dataSource = new HikariDataSource(config);
            plugin.getLogger().info("Пул соединений к базе данных успешно создан!");
        } catch (Exception e) {
            plugin.getLogger().severe("Не удалось создать пул соединений к базе данных!");
            plugin.getLogger().severe("Ошибка: " + e.getMessage());
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection(); // Получаем соединение из пула
    }

    public void close() {
        if (dataSource != null) {
            dataSource.close(); // Закрываем пул соединений при отключении плагина
            plugin.getLogger().info("Пул соединений к базе данных успешно закрыт!");
        }
    }
}
