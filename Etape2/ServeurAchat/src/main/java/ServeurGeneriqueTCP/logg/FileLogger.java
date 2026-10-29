package ServeurGeneriqueTCP.logg;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileLogger implements Logger {

    private static final String LOG_FILE = "server.log";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void Trace(String message) {
        if (message == null || message.isBlank()) {
            message = "[WARN] Message de log vide ou null.";
        } else {
            message = message.trim();
        }

        String timestamp = LocalDateTime.now().format(formatter);
        String logLine = "[" + timestamp + "] " + message;

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            bw.write(logLine);
            bw.newLine();
        } catch (IOException e) {
            System.err.println("Erreur lors de l'écriture dans le fichier de log : " + e.getMessage());
        }
    }
}