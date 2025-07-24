/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.penguinapple.nb.lista.de.tareas;

import java.sql.*;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author Reindhart
 */
public class SQLiteDatabase {
    
    // Ruta donde se creará la base de datos
    private static final String DATABASE_URL = "jdbc:sqlite:todolist.db";
    
    public static void main(String[] args) {
        crearBaseDatos();
        crearTablas();
    }
    
    public static void crearBaseDatos() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                System.out.println("Base de datos creada exitosamente.");
                System.out.println("Ruta: " + System.getProperty("user.dir") + "/todolist.db");
            }
        } catch (SQLException e) {
            System.err.println("Error al crear la base de datos: " + e.getMessage());
        }
    }
    
    public static void crearTablas() {
        // SQL para crear una tabla de ejemplo
        String sqlUsuarios = """
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario VARCHAR(50) NOT NULL UNIQUE,
                contrasena TEXT NOT NULL
            );
            """;

        String sqlTareas = """
            CREATE TABLE IF NOT EXISTS tareas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_id INTEGER NOT NULL,
                tarea TEXT NOT NULL,
                completado BOOLEAN DEFAULT FALSE,
                fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
                fecha_completado DATETIME NULL,
                fecha_limite DATETIME NULL,
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id) 
                    ON DELETE CASCADE 
                    ON UPDATE CASCADE
            );
            """;

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
            Statement stmt = conn.createStatement()) {
            
            stmt.execute("PRAGMA foreign_keys = ON");
            
            // Crear tablas
            stmt.execute(sqlUsuarios);
            System.out.println("Tabla 'usuarios' creada");
            stmt.execute(sqlTareas);
            System.out.println("Tabla 'tareas' creada con foreign key");
            
        } catch (SQLException e) {
            System.err.println("Error al crear las tablas: " + e.getMessage());
        }
    }
    
    
    
    public static boolean buscarUsuario(String usuario){
        
        String sql = "SELECT 1 FROM usuarios WHERE usuario = ? LIMIT 1";
                
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
            Statement stmt = conn.createStatement()) {
            if (stmt.execute(sql)){
                return true;
            }
        } catch (SQLException e) {
            return false;
        }        
        return false;
    }
    
    public static boolean insertarUsuario(String usuario, char[] contrasena){
        String sql = "INSERT INTO usuarios(usuario, contrasena) VALUES(?, ?)";
        String passwordString = null;
        
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            passwordString = new String(contrasena);
            String hashedPassword = BCrypt.hashpw(passwordString, BCrypt.gensalt());

            pstmt.setString(1, usuario);
            pstmt.setString(2, hashedPassword);
            
            return pstmt.executeUpdate() > 0;

        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.out.println("El nombre de usuario ya existe");
                return false;
            } 
        } finally {
            if (passwordString != null) passwordString = null;
        }
        return true;
    }
    
    public static String[] rsUsuario(String usuario, char[] contrasena) {
        String sql = "SELECT * FROM usuarios WHERE usuario = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("contrasena");

                    if (BCrypt.checkpw(new String(contrasena), storedHash)) {
                        String[] datosUsuario = {
                            rs.getString("id"),
                            rs.getString("usuario")
                        };
                        return datosUsuario;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
