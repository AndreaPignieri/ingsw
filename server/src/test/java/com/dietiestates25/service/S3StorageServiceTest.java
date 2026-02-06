package com.dietiestates25.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3StorageServiceTest {

    @Mock
    private S3Client s3Client;

    @Mock
    private MultipartFile file;

    private S3StorageService s3StorageService;

    private final String BUCKET_NAME = "test-bucket";
    private final String REGION = "us-east-1";

    @BeforeEach
    void setUp() {
        s3StorageService = new S3StorageService(s3Client);
        ReflectionTestUtils.setField(s3StorageService, "bucketName", BUCKET_NAME);
        ReflectionTestUtils.setField(s3StorageService, "region", REGION);
    }

    @Test
    void store_shouldUploadFileAndReturnUrl() throws IOException {
        // Arrange
        String originalFilename = "test.jpg";
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn(originalFilename);
        when(file.getInputStream()).thenReturn(new ByteArrayInputStream("test content".getBytes()));
        when(file.getSize()).thenReturn(12L);

        // Act
        String resultUrl = s3StorageService.store(file);

        // Assert
        ArgumentCaptor<PutObjectRequest> putObjectRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(putObjectRequestCaptor.capture(), any(RequestBody.class));

        PutObjectRequest capturedRequest = putObjectRequestCaptor.getValue();
        assertEquals(BUCKET_NAME, capturedRequest.bucket());
        assertTrue(capturedRequest.key().endsWith(".jpg"));

        String expectedUrlStart = "https://" + BUCKET_NAME + ".s3." + REGION + ".amazonaws.com/";
        assertTrue(resultUrl.startsWith(expectedUrlStart));
        assertTrue(resultUrl.endsWith(".jpg"));
    }

    @Test
    void store_shouldThrowException_whenFileIsEmpty() {
        // Arrange
        when(file.isEmpty()).thenReturn(true);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> s3StorageService.store(file));
    }

    @Test
    void load_shouldThrowUnsupportedOperationException() {
        // Act & Assert
        assertThrows(UnsupportedOperationException.class, () -> s3StorageService.load("any-file"));
    }
}
