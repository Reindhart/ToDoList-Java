import java.time.LocalDate;

public class Task {
    private int id;
    private int groupId;
    private String description;
    private boolean completed;
    private LocalDate  dueDate;

    public Task(){
        this.id = 0;
    }

    public void setTask(int id, int userId, int groupId, String description, boolean completed, LocalDate dueDate) {
        this.id = id;
        this.groupId = groupId;
        this.description = description;
        this.completed = completed;
        this.dueDate = dueDate;
    }

    public int getId() {
        return id;
    }
    public int getGroupId() {
        return groupId;
    }
    public String getDescription() {
        return description;
    }
    public boolean isCompleted() {
        return completed;
    }
    public LocalDate getDueDate() {
        return dueDate;
    }

    public void markAsCompleted() {
        this.completed = true;
    }
    
    public void markAsIncomplete() {
        this.completed = false;
    }

    public void changeDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }
    
    public void changeDescription(String description) {
        this.description = description;
    }
    
}
