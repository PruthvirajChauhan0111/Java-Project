package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public class TransactionLog {

    private TransactionLog() {
    }

    public static void append(Path file, String line) throws IOException {
        Path parent = file.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.write(file, List.of(line), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    }
}
