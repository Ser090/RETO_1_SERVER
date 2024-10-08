package dbserver;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.Stack;

public class PostgresConnectionPool {

    private Stack<Connection> connectionPool = new Stack<>();
    private String url;
    private String user;
    private String password;

    // Constructor para inicializar el pool de conexiones
    public PostgresConnectionPool(int poolSize) {
        // Cargar los datos de conexión desde el archivo de propiedades
        loadProperties();

        try {
            // Asegurarse de que el controlador de PostgreSQL esté cargado
            Class.forName("org.postgresql.Driver");

            for (int i = 0; i < poolSize; i++) {
                Connection conn = DriverManager.getConnection(url, user, password);
                if (conn != null) {
                    System.out.println("Conexión " + (i + 1) + " creada y añadida al pool.");
                    connectionPool.push(conn);
                } else {
                    System.out.println("Error al crear la conexión " + (i + 1));
                }
            }
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    // Método para cargar propiedades de un archivo
    private void loadProperties() {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("db.properties")) {
            properties.load(input);
            url = properties.getProperty("db.url");
            user = properties.getProperty("db.user");
            password = properties.getProperty("db.password");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Método para obtener una conexión del pool
    public synchronized Connection getConnection() throws SQLException {
        if (connectionPool.isEmpty()) {
            System.out.println("No hay conexiones en el pool, creando una nueva...");
            return DriverManager.getConnection(url, user, password);
        } else {
            System.out.println("Conexiones disponibles: " + connectionPool.size());
            return connectionPool.pop();
        }
    }

    // Método para liberar una conexión y devolverla al pool
    public synchronized void releaseConnection(Connection connection) {
        connectionPool.push(connection);
        System.out.println("Conexión liberada de vuelta al pool. Quedan: " + connectionPool.size());
    }
}
