package com.routeoptimizer.service;
 
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
 
import java.io.IOException;
import java.util.UUID;
 
/**
 * Service to manage Firebase Storage operations.
 */
@Service
public class FirebaseStorageService {
 
    @Value("${firebase.storage.bucket}")
    private String bucketName;
 
    /**
     * Uploads a file to Firebase Storage and returns the public URL.
     * @param file The PDF manifest file.
     * @param folder The folder path (e.g., "manifests/").
     * @return The URL of the uploaded file.
     * @throws IOException If upload fails.
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        Bucket bucket = StorageClient.getInstance().bucket(bucketName);
        
        String fileName = folder + UUID.randomUUID() + "-" + file.getOriginalFilename();
        
        // Upload the file
        Blob blob = bucket.create(fileName, file.getBytes(), file.getContentType());
        
        // Return the media link (or a custom public URL)
        // For production, you might want to use signed URLs or a storage proxy
        return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", 
            bucketName, fileName.replace("/", "%2F"));
    }
}
