package uk.gov.companieshouse.monitornotification.matcher.enumerationshelper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.logging.LoggerFactory;

import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import java.nio.file.Files;
import java.nio.file.Path;

@ExtendWith(MockitoExtension.class)
public class FileHelperTest {
    @TempDir
    private Path tempDir;

    private FileHelper fileHelper;

    @Mock
    private FileHelper fileHelperMock;

    @Test
    void loadFile_existingFile_returnsInputStreamWithContents() throws Exception {
        final Path tempFile = Files.createFile(tempDir.resolve("file-service-test.txt"));
        String testContent = "Hello World!";
        Files.writeString(tempFile, testContent);
        try {
            this.fileHelper = new FileHelper(LoggerFactory.getLogger("test-logger"));
            InputStream is = this.fileHelper.loadFile(tempFile.toString());
            assertNotNull(is, "Expected non-null InputStream for existing file");
            String actual;
            try (BufferedReader r = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                actual = r.readLine(); // our file has single-line content
            }
            assertEquals(testContent, actual);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    void loadFile_nonexistentPath_throwFileNotFoundError() throws Exception {
        final Path tempFile = Files.createFile(tempDir.resolve("file-service-test.txt"));
        when(this.fileHelperMock.loadFile(tempFile.toString())).thenThrow(FileNotFoundException.class);
        assertThrows(FileNotFoundException.class, () ->
                this.fileHelperMock.loadFile(tempFile.toString()));
    }
}
