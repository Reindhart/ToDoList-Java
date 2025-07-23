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
                contraseña TEXT NOT NULL,
                fecha_creacion DATETIME DEFAULT CURRENT_TIMESTAMP
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
            System.out.println("✓ Tabla 'usuarios' creada");
            
            stmt.execute(sqlTareas);
            System.out.println("✓ Tabla 'tareas' creada con foreign key");
            
        } catch (SQLException e) {
            System.err.println("Error al crear las tablas: " + e.getMessage());
        }
    }
    
    /**
     * Obtiene una conexión a la base de datos
     */
    public static Connection obtenerConexion() throws SQLException {
        return DriverManager.getConnection(DATABASE_URL);
    }
    
    /**
     * Ejemplo de insertar datos
     */
    public static int insertarUsuario(String usuario, String contraseña) {
        String sql = "INSERT INTO usuarios (usuario, contraseña) VALUES (?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            // Habilitar foreign keys
            conn.createStatement().execute("PRAGMA foreign_keys = ON");
            
            pstmt.setString(1, usuario);
            pstmt.setString(2, contraseña); // En producción: hashear la contraseña!
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    int userId = rs.getInt(1);
                    System.out.println("✓ Usuario '" + usuario + "' creado con ID: " + userId);
                    return userId;
                }
            }
            
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE constraint failed")) {
                System.err.println("❌ El usuario '" + usuario + "' ya existe");
            } else {
                System.err.println("Error al insertar usuario: " + e.getMessage());
            }
        }
        
        return -1; // Error
    }
    
    /**
     * Insertar una tarea
     */
    public static boolean insertarTarea(int usuarioId, String tarea) {
        String sql = "INSERT INTO tareas (usuario_id, tarea) VALUES (?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            conn.createStatement().execute("PRAGMA foreign_keys = ON");
            
            pstmt.setInt(1, usuarioId);
            pstmt.setString(2, tarea);
            
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                System.out.println("✓ Tarea agregada: " + tarea);
                return true;
            }
            
        } catch (SQLException e) {
            if (e.getMessage().contains("FOREIGN KEY constraint failed")) {
                System.err.println("❌ Error: El usuario con ID " + usuarioId + " no existe");
            } else {
                System.err.println("Error al insertar tarea: " + e.getMessage());
            }
        }
        
        return false;
    }
    
    /**
     * Marcar tarea como completada
     */
    public static boolean completarTarea(int tareaId) {
        String sql = "UPDATE tareas SET completado = TRUE, fecha_completado = CURRENT_TIMESTAMP WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, tareaId);
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                System.out.println("✓ Tarea marcada como completada");
                return true;
            } else {
                System.err.println("❌ No se encontró la tarea con ID: " + tareaId);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al completar tarea: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Obtener todas las tareas de un usuario
     */
    public static void mostrarTareasUsuario(int usuarioId) {
        String sql = """
            SELECT t.id, t.tarea, t.completado, t.fecha_creacion, u.usuario
            FROM tareas t
            JOIN usuarios u ON t.usuario_id = u.id
            WHERE t.usuario_id = ?
            ORDER BY t.completado ASC, t.fecha_creacion DESC
            """;
        
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();
            
            System.out.println("\n=== TAREAS DEL USUARIO ===");
            boolean hayTareas = false;
            
            while (rs.next()) {
                hayTareas = true;
                String estado = rs.getBoolean("completado") ? "✅ COMPLETADA" : "⏳ PENDIENTE";
                
                System.out.printf("ID: %d | %s | %s | Creada: %s%n",
                    rs.getInt("id"),
                    estado,
                    rs.getString("tarea"),
                    rs.getString("fecha_creacion"));
            }
            
            if (!hayTareas) {
                System.out.println("No hay tareas para este usuario.");
            }
            
        } catch (SQLException e) {
            System.err.println("Error al consultar tareas: " + e.getMessage());
        }
    }
    
    /**
     * Eliminar usuario (elimina automáticamente sus tareas por CASCADE)
     */
    public static boolean eliminarUsuario(int usuarioId) {
        String sql = "DELETE FROM usuarios WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            conn.createStatement().execute("PRAGMA foreign_keys = ON");
            
            pstmt.setInt(1, usuarioId);
            int filasAfectadas = pstmt.executeUpdate();
            
            if (filasAfectadas > 0) {
                System.out.println("✓ Usuario eliminado (y todas sus tareas por CASCADE)");
                return true;
            } else {
                System.err.println("❌ No se encontró el usuario con ID: " + usuarioId);
            }
            
        } catch (SQLException e) {
            System.err.println("Error al eliminar usuario: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Buscar usuario por nombre
     */
    public static int buscarUsuarioId(String nombreUsuario) {
        String sql = "SELECT id FROM usuarios WHERE usuario = ?";
        
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, nombreUsuario);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt("id");
            }
            
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario: " + e.getMessage());
        }
        
        return -1; // No encontrado
    }
    
    /**
     * Mostrar estadísticas de un usuario
     */
    public static void mostrarEstadisticas(int usuarioId) {
        String sql = """
            SELECT 
                u.usuario,
                COUNT(t.id) as total_tareas,
                SUM(CASE WHEN t.completado = 1 THEN 1 ELSE 0 END) as completadas,
                SUM(CASE WHEN t.completado = 0 THEN 1 ELSE 0 END) as pendientes
            FROM usuarios u
            LEFT JOIN tareas t ON u.id = t.usuario_id
            WHERE u.id = ?
            GROUP BY u.id, u.usuario
            """;
        
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, usuarioId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("\n=== ESTADÍSTICAS ===");
                System.out.println("Usuario: " + rs.getString("usuario"));
                System.out.println("Total tareas: " + rs.getInt("total_tareas"));
                System.out.println("Completadas: " + rs.getInt("completadas"));
                System.out.println("Pendientes: " + rs.getInt("pendientes"));
            }
            
        } catch (SQLException e) {
            System.err.println("Error al obtener estadísticas: " + e.getMessage());
        }
    }
}
