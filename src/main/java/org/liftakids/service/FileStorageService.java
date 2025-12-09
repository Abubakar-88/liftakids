package org.liftakids.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageService {

    @Autowired
    private S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.r2.public-url}")
    private String baseUrl;

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate file type
        String originalFileName = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFileName);
        String[] allowedExtensions = {"jpg", "jpeg", "png", "pdf", "doc", "docx"};

        if (!isValidFileExtension(fileExtension, allowedExtensions)) {
            throw new IllegalArgumentException("Invalid file type. Allowed types: " + String.join(", ", allowedExtensions));
        }

        // Validate file size (5MB limit)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }

        // Generate unique file name
        String fileName = generateFileName(folder, fileExtension);

        // Upload to S3/R2
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            // Return file URL
            return baseUrl + "/" + fileName;
        } catch (Exception e) {
            throw new IOException("Failed to upload file to storage: " + e.getMessage(), e);
        }
    }

    public void deleteFile(String fileUrl) {
        try {
            // Extract file key from URL
            String fileKey = extractFileKeyFromUrl(fileUrl);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileKey)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    private String generateFileName(String folder, String fileExtension) {
        String uuid = UUID.randomUUID().toString();
        if (folder != null && !folder.trim().isEmpty()) {
            return folder + "/" + uuid + "." + fileExtension;
        }
        return uuid + "." + fileExtension;
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            throw new IllegalArgumentException("Invalid file name");
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    private boolean isValidFileExtension(String extension, String[] allowedExtensions) {
        for (String allowedExt : allowedExtensions) {
            if (allowedExt.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }

    private String extractFileKeyFromUrl(String fileUrl) {
        // Remove base URL to get the file key
        if (fileUrl.startsWith(baseUrl)) {
            return fileUrl.substring(baseUrl.length() + 1);
        }
        // If it's already a key, return as is
        return fileUrl;
    }

    // Optional: Method to check if file exists
    public boolean fileExists(String fileUrl) {
        try {
            String fileKey = extractFileKeyFromUrl(fileUrl);
            s3Client.headObject(builder -> builder.bucket(bucketName).key(fileKey));
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}