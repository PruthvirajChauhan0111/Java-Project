package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReportGenerator {

    private ReportGenerator() {
    }

    public static String generate(Path logsFolder, Path reportFile) throws IOException {
        long totalDeposits = 0;
        long totalWithdrawals = 0;
        List<String> fileDetails = new ArrayList<>();
        List<Path> logFiles = new ArrayList<>();

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(logsFolder)) {
            for (Path path : stream) {
                if (Files.isRegularFile(path)) {
                    logFiles.add(path);
                }
            }
        }
        logFiles.sort(Comparator.naturalOrder());

        for (Path log : logFiles) {
            BasicFileAttributes attributes = Files.readAttributes(log, BasicFileAttributes.class);
            try (BufferedReader reader = Files.newBufferedReader(log)) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] parts = line.trim().split("\\s+");
                    if (parts.length < 3) {
                        continue;
                    }
                    long amount;
                    try {
                        amount = Long.parseLong(parts[parts.length - 1]);
                    } catch (NumberFormatException e) {
                        continue;
                    }
                    if ("DEPOSIT".equalsIgnoreCase(parts[0])) {
                        totalDeposits += amount;
                    } else if ("WITHDRAW".equalsIgnoreCase(parts[0])) {
                        totalWithdrawals += amount;
                    }
                }
            }
            fileDetails.add(String.format("%s | size=%d bytes | last modified=%s",
                    log.getFileName(), attributes.size(), attributes.lastModifiedTime()));
        }

        String newLine = System.lineSeparator();
        StringBuilder report = new StringBuilder();
        report.append("MiniBank End-of-Day Report").append(newLine);
        report.append("==========================").append(newLine);
        report.append("Total deposits   : Rs. ").append(totalDeposits).append(newLine);
        report.append("Total withdrawals: Rs. ").append(totalWithdrawals).append(newLine);
        report.append("Net change       : Rs. ")
                .append(totalDeposits - totalWithdrawals).append(newLine);
        report.append("Log files:").append(newLine);
        for (String detail : fileDetails) {
            report.append("  ").append(detail).append(newLine);
        }

        Path parent = reportFile.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        Files.writeString(reportFile, report.toString());
        return report.toString();
    }
}
