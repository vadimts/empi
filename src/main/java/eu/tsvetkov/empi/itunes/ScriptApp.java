package eu.tsvetkov.empi.itunes;

public enum ScriptApp {
    MUSIC("Music"), SYSTEM_EVENTS("System Events");

    private String name;

    ScriptApp(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
