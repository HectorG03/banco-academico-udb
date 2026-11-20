package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBD {
    
   private static final String URL =
        "jdbc:mysql://localhost:3306/banco_academico_udb"
        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USUARIO = "banco_user";
    private static final String CLAVE = "Banco2026!";

    public static Connection getConexion() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, CLAVE);
    }    
}