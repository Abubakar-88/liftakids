package org.liftakids.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;
    @Value("${cloud.r2.public-url}")
    private String publicBucketUrl;

    public String uploadFile(MultipartFile file, String studentName) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Generate a unique file name
        String fileName = studentName + "_" + UUID.randomUUID() + ".jpg";

        // Upload the file to R2
        s3Client.putObject(PutObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .build(),
                software.amazon.awssdk.core.sync.RequestBody.fromInputStream(
                        file.getInputStream(), file.getSize())
        );

        // Return the public R2.dev file URL
        return publicBucketUrl + "/" + fileName;
    }


    public boolean deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return false;
        }

        try {
            String fileName = extractFileNameFromUrl(fileUrl);

            if (fileName == null || fileName.isEmpty()) {
                System.err.println("Invalid file URL: " + fileUrl);
                return false;
            }

            // Check if file exists before deleting (optional)
            try {
                s3Client.headObject(HeadObjectRequest.builder()
                        .bucket(bucketName)
                        .key(fileName)
                        .build());
            } catch (NoSuchKeyException e) {
                System.err.println("File not found in S3: " + fileName);
                return false;
            }

            // Delete the file
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build());

            System.out.println("Successfully deleted file: " + fileName);
            return true;

        } catch (S3Exception e) {
            System.err.println("S3 error deleting file '" + fileUrl + "': " + e.awsErrorDetails().errorMessage());
            return false;
        } catch (Exception e) {
            System.err.println("Unexpected error deleting file '" + fileUrl + "': " + e.getMessage());
            return false;
        }
    }

    private String extractFileNameFromUrl(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return null;
        }

        try {
            // Handle different URL formats
            if (fileUrl.startsWith(publicBucketUrl)) {
                return fileUrl.substring(publicBucketUrl.length() + 1);
            }

            // If it contains the bucket name in a different format
            if (fileUrl.contains(bucketName)) {
                return fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            }

            // Assume it's already a file name
            return fileUrl;

        } catch (Exception e) {
            System.err.println("Error extracting file name from URL: " + fileUrl);
            return null;
        }
    }



}
