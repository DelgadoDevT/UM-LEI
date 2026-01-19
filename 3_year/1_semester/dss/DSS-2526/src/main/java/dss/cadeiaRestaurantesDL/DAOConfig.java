package dss.cadeiaRestaurantesDL;

/**
 * Configuration class for database connection settings.
 * Contains database credentials and connection URL for MariaDB.
 */
public class DAOConfig {
    /** Database username for authentication */
    static final String USERNAME = "funcionario";

    /** Database password for authentication */
    static final String PASSWORD = "";

    /** Database name */
    private static final String DATABASE = "cadeiaRestaurantes";

    /** JDBC driver prefix for MariaDB */
    private static final String DRIVER = "jdbc:mariadb";

    /** Complete JDBC connection URL */
    static final String URL = DRIVER + "://localhost:3306/" + DATABASE;
}
