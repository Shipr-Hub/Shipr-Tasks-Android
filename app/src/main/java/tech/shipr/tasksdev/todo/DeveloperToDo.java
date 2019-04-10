package tech.shipr.tasksdev.todo;

class DeveloperToDo {

    private String text;

    private String key;

    public DeveloperToDo() {
    }

    public DeveloperToDo(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
