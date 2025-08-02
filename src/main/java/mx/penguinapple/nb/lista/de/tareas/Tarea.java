/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package mx.penguinapple.nb.lista.de.tareas;

/**
 *
 * @author Reindhart
 */
public class Tarea {

    private int id;
    private String descripcion;
    private boolean completado;
    private String fecha_limite;
    
    
    public void setTarea(int id, String descripcion, boolean completado, String fecha_limite){
        this.id = id;
        this.descripcion = descripcion;
        this.completado = completado;
        this.fecha_limite = fecha_limite;
    }
    

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isCompletado() {
        return completado;
    }

    public void setCompletado(boolean completado) {
        this.completado = completado;
    }

    public String getFecha_limite() {
        return fecha_limite;
    }

    public void setFecha_limite(String fecha_limite) {
        this.fecha_limite = fecha_limite;
    }

}
