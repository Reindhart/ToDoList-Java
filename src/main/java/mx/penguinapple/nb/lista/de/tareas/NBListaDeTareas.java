/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package mx.penguinapple.nb.lista.de.tareas;
import static mx.penguinapple.nb.lista.de.tareas.SQLiteDatabase.crearTablas;

/**
 *
 * @author Reindhart
 */
public class NBListaDeTareas {

    public static void main(String[] args) {
        crearTablas();
        Login.main(args);
    }
}
