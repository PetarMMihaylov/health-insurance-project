package app.utility;

import app.exception.DomainException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Component
public class DocumentUtil {

    public List<String> getAvailableDocuments() {

        try {
            Path folder = Paths.get("src/main/resources/static/documents");
            return Files.list(folder)
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .toList();
        } catch (IOException e) {
            throw new DomainException("Unable to load documents");
        }
    }
}