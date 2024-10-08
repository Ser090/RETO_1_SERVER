package dbserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import dbserver.PostgresConnectionPool; // Cambiado a PostgresConnectionPool
import java.util.ArrayList;
import java.util.List;
import utilidades.Message;
import utilidades.MessageType;
import utilidades.Signable;
import utilidades.User;

public class Dao implements Signable {

    private PostgresConnectionPool pool;

    private final String sqlInsertUser = "INSERT INTO res_users (login, password, 1, 1, active) VALUES (?, ?, ?, ?, true) RETURNING id";
    private final String sqlInsertDatosUsuarios = "INSERT INTO datos_usuarios (res_user_id, nombre, apellido, telefono, localidad, provincia, fecha_nacimiento) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private final String sqlSignIn = "SELECT * FROM res_users WHERE login = ? AND password = ?";
    private final String sqlLoginExist = "SELECT * FROM res_users WHERE login = ?";
    private final String sqlCountries = "SELECT name FROM res_country_state WHERE country_id = (SELECT id FROM res_country WHERE code = 'ES')";

    public Dao(PostgresConnectionPool pool) {
        this.pool = pool;
    }

    // Método para registrar un nuevo usuario
    public Message signUp(User user) {

        Connection conn = null;
        PreparedStatement stmtInsertUser = null;
        PreparedStatement stmtInsertDatosUsuarios = null;
        ResultSet rs = null;

        try {
            // Obtener una conexión del pool
            conn = pool.getConnection();

            // Verificar si la conexión es válida
            if (conn == null || !conn.isValid(2)) {
                System.out.println("Error: No se pudo obtener una conexión válida.");
                return new Message(MessageType.SIGNUP_ERROR, user);
            }

            // Desactivar autocommit para manejar la transacción manualmente
            conn.setAutoCommit(false);

            // Insertar en res_user y obtener el ID generado
            stmtInsertUser = conn.prepareStatement(sqlInsertUser);
            stmtInsertUser.setString(1, user.getLogin());
            stmtInsertUser.setString(2, user.getPass());
            rs = stmtInsertUser.executeQuery();

            if (rs.next()) {
                int resUserId = rs.getInt("id");
                user.setResUserId(resUserId);  // Actualizar el ID en el objeto User

                // Ahora insertar los otros datos en datos_usuarios
                stmtInsertDatosUsuarios = conn.prepareStatement(sqlInsertDatosUsuarios);
                stmtInsertDatosUsuarios.setInt(1, resUserId);
                stmtInsertDatosUsuarios.setString(2, user.getNombre());
                stmtInsertDatosUsuarios.setString(3, user.getApellido());
                stmtInsertDatosUsuarios.setInt(4, user.getTelefono());
                stmtInsertDatosUsuarios.setString(5, user.getLocalidad());
                stmtInsertDatosUsuarios.setString(6, user.getProvincia());
                stmtInsertDatosUsuarios.setDate(7, new java.sql.Date(user.getFechaNacimiento().getTime()));

                stmtInsertDatosUsuarios.executeUpdate();

                // Confirmar la transacción
                conn.commit();
                System.out.println("Usuario registrado correctamente: " + user.getLogin());
                return new Message(MessageType.OK_RESPONSE, user);
            } else {
                conn.rollback();
                return new Message(MessageType.SIGNUP_ERROR, user);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (conn != null) {
                    conn.rollback();  // Revertir la transacción en caso de error
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return new Message(MessageType.SIGNUP_ERROR, user);
        } finally {
            // Asegurarse de liberar la conexión y cerrar recursos
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmtInsertUser != null) {
                    stmtInsertUser.close();
                }
                if (stmtInsertDatosUsuarios != null) {
                    stmtInsertDatosUsuarios.close();
                }
                if (conn != null) {
                    pool.releaseConnection(conn);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // Método para validar un usuario (inicio de sesión)
    public Message signIn(User user) {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Obtener una conexión del pool
            conn = pool.getConnection();

            // Preparar la consulta SQL
            stmt = conn.prepareStatement(sqlSignIn);
            stmt.setString(1, user.getLogin());
            stmt.setString(2, user.getPass());

            rs = stmt.executeQuery();

            // Si se encuentra un usuario, el inicio de sesión es válido
            if (rs.next()) {
                return new Message(MessageType.OK_RESPONSE, user);
            } else {
                return new Message(MessageType.SIGNIN_ERROR, user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Message(MessageType.SIGNIN_ERROR, user);
        } finally {
            // Asegurarse de liberar la conexión y cerrar el ResultSet
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (conn != null) {
                pool.releaseConnection(conn);
            }
        }
    }

    // Método para verificar si un usuario ya existe en la base de datos
    public Message loginExist(String login) {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            // Obtener una conexión del pool
            conn = pool.getConnection();

            // Preparar la consulta SQL
            stmt = conn.prepareStatement(sqlLoginExist);
            stmt.setString(1, login);

            rs = stmt.executeQuery();

            // Si se encuentra un usuario, el inicio de sesión es válido
            if (rs.next()) {
                return new Message(MessageType.LOGIN_OK, user);
            } else {
                return new Message(MessageType.LOGIN_EXIST_ERROR, user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Message(MessageType.SQL_ERROR, user);
        } finally {
            // Asegurarse de liberar la conexión y cerrar el ResultSet
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return new Message(MessageType.SQL_ERROR, user);
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    return new Message(MessageType.SQL_ERROR, user);
                }
            }
            if (conn != null) {
                pool.releaseConnection(conn);
            }
        }
    }

    // Método para obtener las provincias de España
    public List<String> getCountries() {

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<String> provincias = new ArrayList<>();

        try {
            // Obtener una conexión del pool
            conn = pool.getConnection();

            // Preparar la consulta SQL
            stmt = conn.prepareStatement(sqlCountries);
            rs = stmt.executeQuery();

            // Agregar los resultados a la lista
            while (rs.next()) {
                provincias.add(rs.getString("name"));
            }

            return provincias;

        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            // Asegurarse de liberar la conexión y cerrar ResultSet
            try {
                if (rs != null) {
                    rs.close();
                }
                if (stmt != null) {
                    stmt.close();
                }
                if (conn != null) {
                    pool.releaseConnection(conn);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
