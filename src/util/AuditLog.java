package util;

public class AuditLog implements AutoCloseable {

    private final String name;

    public AuditLog(String name) {
        this.name = name;
        System.out.println("Audit log opened: " + name);
    }

    public void record(String message) {
        System.out.println("[AUDIT] " + message);
    }

    @Override
    public void close() {
        System.out.println("Audit log closed: " + name);
    }
}
