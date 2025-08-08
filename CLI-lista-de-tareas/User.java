import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class User {
    
    private int id;
    private String name;
    private String password;
    private Group group = new Group();
    public List<Group> groups = new ArrayList<>();

    public void setUser(int id, String name, String password){
        this.id = id;
        this.name = name;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public boolean hasGroups(){        
        return groups.isEmpty() ? false : true;
    }

    public static void register(String username, String password){

        List<String> lines = new ArrayList<>();

        try{
            File file = new File("users.txt");
            if (!file.exists()){
                file.createNewFile();
                FileWriter writer = new FileWriter("users.txt");
                writer.write("1\n");
                writer.close();
            }

            BufferedReader reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();

            int lastId = Integer.parseInt(line.trim());

            String rest;
            while((rest = reader.readLine()) != null){
                lines.add(rest);
            }
            reader.close();
            
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));

            writer.write((lastId + 1) + "\n");

            for (String row : lines) {
                writer.write(row + "\n");
            }

            writer.write(lastId + "," + username + "," + password + "\n");
            writer.close();

            main.clearScreen();
            System.out.println("Se ha registrado correctamente. \n");

        } catch (IOException e){
            System.out.println("An error ocurred: " + e.getMessage());
        }
    }

    public void loadGroups(){
        File file = new File("groups.txt");
        if (file.exists() && file.length() != 0){
            try (Scanner scanner = new Scanner(file)) {

                scanner.useDelimiter(",|\n");
                while (scanner.hasNext()) {
                    String fileGroupId = scanner.next().trim();
                    int id = Integer.parseInt(fileGroupId);
                    String fileUserId = scanner.next().trim();
                    int userId = Integer.parseInt(fileUserId);

                    if (userId == id){
                        group.setGroup(id, userId, name);
                    }
                    
                }
            } catch (IOException e) {
                System.out.println("An error occurred while reading the file: " + e.getMessage());
            }
        }
    }


    public void getGroups (){
        int contador = 0;
        int total = groups.size();
        
        for (Group group : groups){
            
            if (group.hasTasks()){

            } else {
                System.out.printf("| %i. %-13s | %-60s | %-10s | %-20s |\n", contador + 1, group.getName(), "Sin Tareas", "---", "---");
            }
            if (++contador < total) {
            System.out.println("|-------------------------------------------------------------------------------------------------------------------|");
            }    
        }
        System.out.println("+===================================================================================================================+");
    }
}
