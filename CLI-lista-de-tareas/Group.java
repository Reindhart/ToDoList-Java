import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Group {
    
    private int id;
    private int userId;
    private String name;
    public List<Task> tasks = new ArrayList<>();

    public void setGroup(int id, int userId, String name){
        this.id = id;
        this.userId = userId;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public boolean hasTasks() {
        return tasks.isEmpty() ? false : true;
    }

    public void clearGroup(){
        this.id = 0;
        this.userId = 0;
        this.name = "";
    }

    public void createGroup(){
        List<String> lines = new ArrayList<>();

        try{
            File file = new File("groups.txt");
            if (!file.exists()){
                file.createNewFile();
                FileWriter writer = new FileWriter("groups.txt");
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

            writer.write(lastId + "," + userId + "," + name + "\n");
            writer.close();

            main.clearScreen();
            System.out.println("Se ha guardado correctamente. \n");

        } catch (IOException e){
            System.out.println("An error ocurred: " + e.getMessage());
        }
    }

    public void loadTasks() {
        File file = new File("tasks.txt");
        if (file.exists() && file.length() != 0){
            try (Scanner scanner = new Scanner(file)) {
                scanner.useDelimiter(",|\n");
                while (scanner.hasNext()) {
                    String fileTaskId = scanner.next().trim();
                    int id = Integer.parseInt(fileTaskId);
                    String fileUserId = scanner.next().trim();
                    int userId = Integer.parseInt(fileUserId);
                    String fileGroupId = scanner.next().trim();
                    int groupId = Integer.parseInt(fileGroupId);
                    String description = scanner.next().trim();
                    boolean completed = Boolean.parseBoolean(scanner.next().trim());
                    LocalDate dueDate = LocalDate.parse(scanner.next().trim());

                    if(userId == this.userId && groupId == this.id) {
                        Task task = new Task();
                        task.setTask(id, userId, groupId, description, completed, dueDate);
                        tasks.add(task);
                    }

                }
            } catch (IOException e) {
                System.out.println("An error occurred while reading the file: " + e.getMessage());
            }
        }   
    }

    public List<Task> getTasks(){
        return tasks;
    }
}
