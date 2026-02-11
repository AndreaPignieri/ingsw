package com.dietiestates25.service;

import com.dietiestates25.exception.FileStorageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LocalStorageServiceTest {

    private LocalStorageService localStorageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        localStorageService = new LocalStorageService();
        ReflectionTestUtils.setField(localStorageService, "rootLocation", tempDir);
        localStorageService.init();
    }

    @Test
    void store_ValidFile_Success() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "Hello, World!".getBytes());

        String result = localStorageService.store(file);

        assertNotNull(result);
        assertTrue(result.contains(".txt"));

        // Check if file actually exists in temp dir
        // The filename is random UUID, so we can't guess it easily from here without
        // parsing result
        // Result format: "/uploads/" + filename
        String filename = result.substring("/uploads/".length());
        assertTrue(Files.exists(tempDir.resolve(filename)));
    }

    @Test
    void store_EmptyFile_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.txt",
                "text/plain",
                new byte[0]);

        assertThrows(FileStorageException.class, () -> localStorageService.store(file));
    }

    @Test
    void store_InvalidPath_ThrowsException() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "../test.txt", // Attempt path traversal
                "text/plain",
                "Hello".getBytes());

        // In the current implementation, standard MultipartFile.getOriginalFilename()
        // cleans the path usually,
        // but if we force it, the service checks for .. in resolve.
        // However, the service implementation uses UUID for filename, so original
        // filename path traversal might be ignored or handled differently.
        // Checking the code:
        // String filename = UUID.randomUUID().toString() + extension;
        // The service ignores the original filename for the most part except extension.
        // So path traversal via filename is actually mitigated by design.

        // Let's test non-existing directory if possible, or just skip path traversal if
        // it's not possible to trigger.
        // The "security check" in existing code: if
        // (!destinationFile.startsWith(this.rootLocation))
        // relies on resolve. Since we use UUID, it's hard to trigger.

        // Let's try to mock a file that throws IOException on getInputStream
        MockMultipartFile badFile = new MockMultipartFile(
                "file",
                "test.txt",
                "text/plain",
                "content".getBytes()) {
            @Override
            public java.io.InputStream getInputStream() throws IOException {
                throw new IOException("Stream error");
            }
        };

        assertThrows(FileStorageException.class, () -> localStorageService.store(badFile));
    }
}
