/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.penguinapple.nb.lista.de.tareas;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.table.DefaultTableModel;
import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author Reindhart
 */
public class SQLiteDatabase {
    
    // Ruta donde se creará la base de datos
    private static final String DATABASE_URL = "jdbc:sqlite:todolist.db";
    
    public static void main(String[] args) {
        createDB();
        createTables();
    }
    
    public static void createDB() {
        try (Connection conn = DriverManager.getConnection(DATABASE_URL)) {
            if (conn != null) {
                System.out.println("Ruta DB: " + System.getProperty("user.dir") + "/todolist.db");
            }
        } catch (SQLException e) {
            System.err.println("Error al crear la base de datos: " + e.getMessage());
        }
    }
    
    public static void createTables() {
        // SQL para crear una tabla de ejemplo
        String sqlUsers = """
            CREATE TABLE IF NOT EXISTS usuarios (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario VARCHAR(50) NOT NULL UNIQUE,
                contrasena TEXT NOT NULL
            );
            """;
        
        String sqlGroups = """
            CREATE TABLE IF NOT EXISTS grupos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                usuario_id INTEGER NOT NULL,
                nombre VARCHAR(50) NOT NULL,
                FOREIGN KEY (usuario_id) REFERENCES usuarios(id) 
                    ON DELETE CASCADE 
                    ON UPDATE CASCADE
            );
            """;
        
        String sqlTasks = """
            CREATE TABLE IF NOT EXISTS tareas (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                grupo_id INTEGER NOT NULL,
                descripcion TEXT NOT NULL,
                completado BOOLEAN DEFAULT FALSE,
                fecha_limite DATETIME NULL,
                FOREIGN KEY (grupo_id) REFERENCES grupos(id)
                                    ON DELETE CASCADE 
                                    ON UPDATE CASCADE
            );
            """;

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
            Statement stmt = conn.createStatement()) {
            
            stmt.execute("PRAGMA foreign_keys = ON");
            
            // Crear tablas
            stmt.execute(sqlUsers);
            stmt.execute(sqlGroups);
            stmt.execute(sqlTasks);
            
        } catch (SQLException e) {
            System.err.println("Error al crear las tablas: " + e.getMessage());
        }
    }
    
    public static String[] searchUser(String user, char[] password){
        
        String sql = "SELECT * FROM usuarios WHERE usuario = ?";

        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, user);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String storedHash = rs.getString("contrasena");

                    if (BCrypt.checkpw(new String(password), storedHash)) {
                        String[] userData = {
                            rs.getString("id"),
                            rs.getString("usuario")
                        };
                        return userData;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static boolean insertUser(String user, char[] password){
        String sql = "INSERT INTO usuarios(usuario, contrasena) VALUES(?, ?)";
        String passwordString = null;
        
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            passwordString = new String(password);
            String hashedPassword = BCrypt.hashpw(passwordString, BCrypt.gensalt());

            pstmt.setString(1, user);
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
    
    public static DefaultListModel<Grupo> getGroups(String[] userData){
        String sql = "SELECT * FROM grupos WHERE usuario_id = ?";
        DefaultListModel<Grupo> model = new DefaultListModel<>();
        LinkedList<Grupo> groupList = new LinkedList<>();
        
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, userData[0]);
  
            try (ResultSet rs = pstmt.executeQuery()){
                while (rs.next()) {                   
                    String idGroup = rs.getString("id");
                    String nameGroup = rs.getString("nombre");
                    int id = Integer.parseInt(idGroup);                    
                    Grupo group = new Grupo(id, nameGroup);
                    model.addElement(group);
                    groupList.addLast(group);
                }
                
                return model;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new DefaultListModel<>();
        }
    }
    
    public static DefaultTableModel getTasks(int idGroup){
        List<Tarea> tareas = new ArrayList<>();
        
        DefaultTableModel model = new DefaultTableModel(new Object[]{"ID", "Completado", "Descripción", "Fecha Límite"}, 0){
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 1) return Boolean.class; // Checkbox
                return String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Solo checkbox editable
            }
        };
        
        String sql = "SELECT * FROM tareas WHERE grupo_id = ?";       
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idGroup);
  
            try (ResultSet rs = pstmt.executeQuery()){
                while (rs.next()) {                   
                    int id = rs.getInt("id");
                    String descripcion = rs.getString("descripcion");
                    boolean completado = rs.getBoolean("completado");
                    String fecha_limite = rs.getString("fecha_limite") == null ? "---" : rs.getString("fecha_limite");
                    
                    Tarea tarea = new Tarea();
                    
                    tarea.setTarea(id, descripcion, completado, fecha_limite);
                    tareas.addLast(tarea);
                }
            }
            
            for(Tarea t : tareas){
                Object[] fila = new Object[] {
                    t.getId(),
                    t.isCompletado(),
                    t.getDescripcion(),
                    t.getFecha_limite()
                };

                model.addRow(fila);
            }            
            
            return model;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static Integer addGroup(String idUser, String name){
        
        String sql = "INSERT INTO grupos(usuario_id, nombre) VALUES(?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, idUser);
            pstmt.setString(2, name);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1); // Devuelve el ID generado
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;        
    }
    
    public static Integer addTask(int idGroup, String task, String dueDate){
        
        String sql = "INSERT INTO tareas(grupo_id, descripcion, fecha_limite) VALUES(?, ?, ?)";
        
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
            PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, idGroup);
            pstmt.setString(2, task);
            pstmt.setString(3, dueDate);
            
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static boolean deleteGroup(int idGroup){
        String sql = "DELETE FROM grupos WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
            Statement stmt = conn.createStatement();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, idGroup);
            
            stmt.execute("PRAGMA foreign_keys = ON"); 
            pstmt.executeUpdate();
            return true;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    
    public static boolean deletTasks(List<Integer> completedTasks){
        String sql = "DELETE FROM tareas WHERE id = ?";
        
        try (Connection conn = DriverManager.getConnection(DATABASE_URL);
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            for (int id : completedTasks){
                pstmt.setInt(1, id);
                pstmt.executeUpdate();
            }            
            
            return true;

        }catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
