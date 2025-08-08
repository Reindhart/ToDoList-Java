import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

class main {

  public static void clearScreen() {
    System.out.print("\033[H\033[2J");
    System.out.flush();
  }

  public static void listTareas(User user){

    Group group = new Group();
    final int NUEVO_GRUPO = 1, NUEVA_TAREA = 2, REGRESAR = 3, SALIR = 4;
    int option = 0;

    clearScreen();
    while (option != 3) {
      group.clearGroup();
      System.out.println("Tareas del usuario: " + user.getName());
      System.out.println("+===================================================================================================================+");
      System.out.println("|      Grupo      |                           Tareas                            | Completada | Fecha de vencimiento |");
      System.out.println("+===================================================================================================================+");
      if (user.hasGroups()){
        user.getGroups();
      } else {
        System.out.println("|                                                                                                                   |");
        System.out.println("| No tiene grupos asignados.                                                                                        |");
        System.out.println("|                                                                                                                   |");
        System.out.println("+===================================================================================================================+\n");
      }
      System.out.println("Seleccione una opción:");
      System.out.println("1. Crear nuevo grupo");
      if (user.hasGroups()) {
        System.out.println("2. Crear nueva tarea");        
      }
      System.out.println("3. Salir");
      System.out.print("Seleccione una opción: ");
      Scanner scanner = new Scanner(System.in);
      option = scanner.nextInt();

      switch (option) {
        case NUEVO_GRUPO:
          System.out.print("\nIngrese el nombre del grupo: ");
          String groupName = scanner.next();
          group.setGroup(0, user.getId(), groupName);
          group.createGroup();
          user.groups.add(group);
          clearScreen();

          break;
        case NUEVA_TAREA:
          System.out.println("Seleccione un grupo para agregar la tarea (número)");
          String groupNameOrId = scanner.next();
          group = user.groups.get(Integer.parseInt(groupNameOrId) - 1);

          break;

        case REGRESAR:
          clearScreen();
          break;

        case SALIR:
          System.exit(0);
          break;

        default:
          System.out.println("Opción no válida. Por favor, intente de nuevo.\n");
      }
      scanner.close();
    }
  }

  

  public static void login(String username, String password) {
    try (Scanner scanner = new Scanner(new File("users.txt"))) {
        scanner.useDelimiter(",|\n");

        scanner.next(); // Skip lastId

        while (scanner.hasNext()) {

          String fileId = scanner.next().trim();
          int id = Integer.parseInt(fileId);
          String fileUsername = scanner.next().trim();

            if (scanner.hasNext()) {
                String filePassword = scanner.next().trim();

                if (fileUsername.equals(username)) {
                    if (filePassword.equals(password)) {
                        User user = new User();
                        user.setUser(id, fileUsername, filePassword);
                        listTareas(user);
                    } else {
                        System.out.println("Usuario o contraseña incorrectos.");
                    }
                    return;
                }
            }
        }
        clearScreen();
        System.out.println("Usuario o contraseña incorrectos\n");
    } catch (FileNotFoundException e) {
        System.out.println("Usuario o contraseña incorrectos\n");
    }
  }

  public static void main(String[] args) {

    clearScreen();

    final int REGISTRARSE = 1, INICIAR_SESION = 2, SALIR = 3;

    Scanner scanner = new Scanner(System.in);
    int opcion = 0;

    System.out.println("Bienvenido al gestor de tareas\n"); 
    System.out.println("¿Qué desea hacer?\n");
    while ( opcion != 3){
      System.out.println("1. Registrarse");
      System.out.println("2. Iniciar sesión");
      System.out.println("3. Salir\n");
      System.out.print("Escoja una opción: ");
      opcion = scanner.nextInt();
      
      switch (opcion) {
        case REGISTRARSE:
            clearScreen();
            System.out.print("Ingrese su usuario: ");
            String username = scanner.next();
            System.out.print("Ingrese una contraseña: ");
            String password = scanner.next();
            User.register(username, password);
          break;

        case INICIAR_SESION:
          clearScreen();
            System.out.print("Ingrese su usuario: ");
            String user = scanner.next();
            System.out.print("Ingrese una contraseña: ");
            String pwd = scanner.next();
            login(user, pwd);
          break;
        
        case SALIR:
          System.out.println("¡Hasta luego!");
          break;
      
        default:
          System.out.println("Opción no válida. Por favor, intente de nuevo.");
          break;
      }
    }
    scanner.close();
  }
}