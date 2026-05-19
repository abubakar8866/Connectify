package com.abubakar.connectify.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.abubakar.connectify.dto.response.FileUploadResponse;

public interface FileService {

    // Single file upload
    String uploadFile(
            MultipartFile file,
            Long entityId,
            String oldFileName,
            String folderName
    );

    // Multiple file upload
    FileUploadResponse uploadMultipleFiles(
            List<MultipartFile> files,
            Long entityId,
            List<String> oldFiles,
            String folderName
    );

    // Delete file
    boolean deleteFile(
            String fileName,
            String folderName
    );

    // Upload image directly from URL (OAuth2 profile images)
    String uploadFromUrl(
            String imageUrl,
            Long entityId,
            String oldFileName,
            String folderName
    );

}
