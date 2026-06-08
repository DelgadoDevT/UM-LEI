package pt.uminho.taki.dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionManager {

    private static final HikariDataSource dataSource;

    static {
        try {
            Class.forName("pt.uminho.taki.dao.TestConnectionManager");
        } catch (ClassNotFoundException e) {
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(getEnvOrProp("TAKI_DB_URL", "jdbc:postgresql://db_local:5432/taki_db"));
        config.setUsername(getEnvOrProp("TAKI_DB_USER", "taki_app_user"));
        config.setPassword(getEnvOrProp("TAKI_DB_PASSWORD", "64f3f5ccecb7ed73430da23a208923f3e8a61630f0b70e812c458f36e2f390b962c64fa1b4aafec2956e0aa5a1df80d76a39af8aecd2aef6052c9a1b8281bab5"));
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setConnectionTimeout(30000);
        config.setIdleTimeout(600000);
        config.setMaxLifetime(1800000);
        config.setPoolName("TakiPool");
        dataSource = new HikariDataSource(config);
    }

    private static String getEnvOrProp(String key, String def) {
        String prop = System.getProperty(key);
        if (prop != null) return prop;
        String env = System.getenv(key);
        if (env != null) return env;
        return def;
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
