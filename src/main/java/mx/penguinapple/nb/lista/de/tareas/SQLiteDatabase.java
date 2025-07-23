/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.penguinapple.nb.lista.de.tareas;

import java.sql.*;

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
    
    /**
     * Crea la base de datos SQLite
     */
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
    
    /**
     * Crea las tablas en la base de datos
     */
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
    
    public static boolean insertarUsuario(String Usuario, char[] Contrasena){
        
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
            Statement stmt = conn.createStatement()) {
            String Pass = new String(Contrasena);
            
            
            
            String sql = "INSERT INTO usuarios VALUES (" + Usuario + ", " + Pass + ")";
            
        } catch (SQLException e) {
            System.out.println(e);
        }
        
        return true;
    }
    
    public static boolean buscarUsuario(String Usuario, char[] Contrasena){
        
        String sql = "SELECT * FROM usuarios WHERE username = ? AND password = ?";
        
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
            Statement stmt = conn.createStatement()) {
                
            
            
        } catch (SQLException e){
            
        }
        
        return true;
    }
}
