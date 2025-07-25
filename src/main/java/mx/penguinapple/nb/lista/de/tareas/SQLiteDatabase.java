/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.penguinapple.nb.lista.de.tareas;

import java.sql.*;
import javax.swing.DefaultListModel;
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
                System.out.println("Ruta DB: " + System.getProperty("user.dir") + "/todolist.db");
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
        
        String sqlGrupos = """
            CREATE TABLE IF NOT EXISTS grupos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_id INTEGER NOT NULL,
                nombre VARCHAR(50) NOT NULL,
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id) 
                    ON DELETE CASCADE 
                    ON UPDATE CASCADE
            );
            """;
        
        String sqlTareas = """
            CREATE TABLE IF NOT EXISTS tareas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_id INTEGER NOT NULL,
                grupo_id INTEGER NOT NULL,
                tarea TEXT NOT NULL,
                completado BOOLEAN DEFAULT FALSE,
                fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP,
                fecha_completado DATETIME NULL,
                fecha_limite DATETIME NULL,
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id) 
                    ON DELETE CASCADE 
                    ON UPDATE CASCADE,
                FOREIGN KEY (grupo_id) REFERENCES grupos(id)
                                    ON DELETE CASCADE 
                                    ON UPDATE CASCADE
            );
            """;

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
            Statement stmt = conn.createStatement()) {
            
            stmt.execute("PRAGMA foreign_keys = ON");
            
            // Crear tablas
            stmt.execute(sqlUsuarios);
            stmt.execute(sqlGrupos);
            stmt.execute(sqlTareas);
            
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
    
    
    public static DefaultListModel<String> obtenerGrupos(String[] datosUsuario){
        String sql = "SELECT * FROM grupos WHERE usuario_id = ?";
        DefaultListModel<String> model = new DefaultListModel<>();
        
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, datosUsuario[0]);
  
            try (ResultSet rs = pstmt.executeQuery()){
                while (rs.next()) {                   
                    String nombreGrupo = rs.getString("nombre");
                    model.addElement(nombreGrupo);
                }
                
                return model;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new DefaultListModel<>();
        }
    }
    
    public static DefaultListModel<String> obtenerTareas(String[] datosUsuario){
        String sql = "SELECT * FROM grupos WHERE usuario_id = ?";
        DefaultListModel<String> model = new DefaultListModel<>();
        
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, datosUsuario[0]);
  
            try (ResultSet rs = pstmt.executeQuery()){
                while (rs.next()) {                   
                    String nombreGrupo = rs.getString("nombre");
                    model.addElement(nombreGrupo);
                }
                
                return model;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new DefaultListModel<>();
        }
    }
    
    public static boolean insertarGrupo(String usuario_id, String nombre){
        String sql = "INSERT INTO grupos(usuario_id, nombre) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, usuario_id);
            pstmt.setString(2, nombre);
            pstmt.executeUpdate();
            
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }        
    }
}
