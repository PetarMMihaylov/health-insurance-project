package app.utility;

import app.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentUtilUTest {

    @InjectMocks
    private DocumentUtil documentUtil;

    @Test
    void getAvailableDocuments_returnsFileNames() throws IOException {

        Path file1 = mock(Path.class);
        Path file2 = mock(Path.class);

        when(file1.getFileName()).thenReturn(Path.of("file1.txt"));
        when(file2.getFileName()).thenReturn(Path.of("file2.pdf"));

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            Stream<Path> mockStream = Stream.of(file1, file2);

            filesMock.when(() -> Files.list(any(Path.class))).thenReturn(mockStream);
            filesMock.when(() -> Files.isRegularFile(file1)).thenReturn(true);
            filesMock.when(() -> Files.isRegularFile(file2)).thenReturn(true);

            List<String> documents = documentUtil.getAvailableDocuments();

            assertEquals(2, documents.size());
            assertTrue(documents.contains("file1.txt"));
            assertTrue(documents.contains("file2.pdf"));

            filesMock.verify(() -> Files.list(any(Path.class)), times(1));
            filesMock.verify(() -> Files.isRegularFile(file1), times(1));
            filesMock.verify(() -> Files.isRegularFile(file2), times(1));
        }
    }

    @Test
    void getAvailableDocuments_throwsDomainException_onIOException() {

        try (MockedStatic<Files> filesMock = mockStatic(Files.class)) {
            filesMock.when(() -> Files.list(any(Path.class))).thenThrow(new IOException());

            DomainException exception = assertThrows(DomainException.class, documentUtil::getAvailableDocuments);
            assertEquals("Unable to load documents", exception.getMessage());

            filesMock.verify(() -> Files.list(any(Path.class)), times(1));
        }
    }
}
