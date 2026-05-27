package com.abubakar.connectify.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.abubakar.connectify.dto.response.FileUploadResponse;
import com.abubakar.connectify.exception.UnableToUploadFileException;
import com.abubakar.connectify.exception.EmptyException;
import com.abubakar.connectify.exception.OperationFailException;
import com.abubakar.connectify.service.FileService;

@Service
public class FileServiceImpl implements FileService {

	@Value("${file.upload}")
	private String uploadDir;

	private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

	private static final long MAX_IMAGE_SIZE = 2 * 1024 * 1024; // 2MB

	private static final long MAX_VIDEO_SIZE = 50 * 1024 * 1024; // 50MB

	private static final int MAX_FILE_COUNT = 5;

	private static final long MAX_TOTAL_SIZE = 60 * 1024 * 1024; // 60MB

	private static final List<String> ALLOWED_TYPES = List.of(
			"image/png", "image/jpeg", "image/jpg", "image/webp",
			"video/mp4", "video/webm", "video/quicktime"
	);

	// COMMON METHOD (REUSABLE)
	private Path createFolder(String folderName) {

		logger.info("Creating/Checking folder: {}", folderName);

		Path path = Paths.get(uploadDir, folderName);

		try {
			if (!Files.exists(path)) {
				Files.createDirectories(path);
				logger.info("Folder created successfully at: {}", path.toAbsolutePath());
			} else {
				logger.debug("Folder already exists at: {}", path.toAbsolutePath());
			}
		} catch (IOException e) {
			logger.error("Failed to create directory: {} | Error: {}", folderName, e.getMessage(), e);
			throw new OperationFailException("Failed to create directory");
		}

		return path;

	}

	// Single file upload
	@Override
	public String uploadFile(MultipartFile file, Long entityId, String oldFileName, String folderName) {

		logger.info("File upload started | EntityId: {} | Folder: {}", entityId, folderName);

		// Null / Empty check
		if (file == null || file.isEmpty()) {
			logger.warn("File is empty or null");
			throw new EmptyException("File is required.");
		}

		// Content type validation
		String contentType = file.getContentType();
		if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
			logger.warn("Invalid file type: {}", contentType);
			throw new UnableToUploadFileException("Invalid file type.");
		}

		// File size validation
		long maxAllowedSize =
				contentType.startsWith("video")
						? MAX_VIDEO_SIZE
						: MAX_IMAGE_SIZE;

		if (file.getSize() > maxAllowedSize) {

			logger.warn(
					"File size exceeded | type: {} | size: {}",
					contentType,
					file.getSize()
			);

			throw new UnableToUploadFileException(
					contentType.startsWith("video")
							? "Video size exceeds 50MB."
							: "Image size exceeds 2MB."
			);
		}

		// File name validation
		String originalFileName =
				file.getOriginalFilename();

		if (
				originalFileName == null
						||
						originalFileName.isBlank()
		) {

			logger.warn(
					"Invalid file name: {}",
					originalFileName
			);

			throw new UnableToUploadFileException(
					"Invalid file name."
			);
		}

		// SAFE CLEAN NAME
		String cleanFileName =
				Paths.get(originalFileName)
						.getFileName()
						.toString();

		Path folderPath = createFolder(folderName);

		int index = cleanFileName.lastIndexOf(".");
		if (index <= 0) {
			logger.warn("File Extension not found: {}", cleanFileName);
			throw new UnableToUploadFileException("File Extension not found.");
		}

		String extension = cleanFileName.substring(index).toLowerCase();
		String fileName = entityId + "_" + UUID.randomUUID() + extension;

		try {
			// Save file
			Files.copy(file.getInputStream(), folderPath.resolve(fileName));
			logger.info("File uploaded successfully: {}", fileName);

			// Delete old file
			if (oldFileName != null && !oldFileName.isBlank()) {
				boolean deleted = deleteFile(oldFileName, folderName);
				logger.info("Old file deletion | File: {} | Deleted: {}", oldFileName, deleted);
			}

		} catch (IOException e) {
			logger.error("File upload failed | File: {} | Error: {}", fileName, e.getMessage(), e);
			throw new UnableToUploadFileException("Upload failed.");
		}

		return fileName;

	}

	// Multiple file upload
	@Override
	public FileUploadResponse uploadMultipleFiles(List<MultipartFile> files, Long entityId, List<String> oldFiles,
												  String folderName) {

		logger.info("Multiple file upload started | EntityId: {} | Folder: {}", entityId, folderName);

		if (files == null || files.isEmpty()) {
			logger.warn("File list is empty or null");
			throw new EmptyException("Files required.");
		}

		if (files.size() > MAX_FILE_COUNT) {
			logger.warn("File count exceeded | Count: {}", files.size());
			throw new UnableToUploadFileException("Maximum 5 files allowed.");
		}

		//totalSize = total size of all uploaded files (in bytes)
		long totalSize = files.stream()
				.filter(Objects::nonNull)
				.mapToLong(MultipartFile::getSize)
				.sum();

		if (totalSize > MAX_TOTAL_SIZE) {
			logger.warn("Total file size exceeded | Size: {}", totalSize);
			throw new UnableToUploadFileException("Total file size must not exceed 60MB.");
		}

		createFolder(folderName);

		List<String> success = new ArrayList<>();
		List<String> failed = new ArrayList<>();

		for (MultipartFile file : files) {
			try {
				String originalName = (file != null) ? file.getOriginalFilename() : "unknown";
				logger.info("Uploading file: {}", originalName);

				String fileName = uploadFile(file, entityId, null, folderName);

				success.add(fileName);
				logger.info("File uploaded successfully: {}", fileName);

			} catch (Exception e) {
				String failedName = (file != null) ? file.getOriginalFilename() : "unknown";

				logger.error("File upload failed: {} | Error: {}", failedName, e.getMessage(), e);

				failed.add(failedName + " -> " + e.getMessage());
			}
		}

		// Delete old files if at least one upload success
		if (!success.isEmpty() && oldFiles != null && !oldFiles.isEmpty()) {

			logger.info("Deleting old files | Count: {}", oldFiles.size());

			for (String oldFile : oldFiles) {
				try {
					boolean deleted = deleteFile(oldFile, folderName);
					logger.info("Old file deletion | File: {} | Deleted: {}", oldFile, deleted);
				} catch (Exception e) {
					logger.warn("Failed to delete old file: {} | Error: {}", oldFile, e.getMessage());
				}
			}
		}

		logger.info("Upload summary | Success: {} | Failed: {}", success.size(), failed.size());

		return new FileUploadResponse(success, failed);

	}

	// Delete file
	@Override
	public boolean deleteFile(String fileName, String folderName) {

		logger.info("Delete file request | File: {} | Folder: {}", fileName, folderName);

		if (fileName == null || fileName.isBlank()) {
			logger.warn("Invalid file name for deletion: {}", fileName);
			return false;
		}

		try {
			boolean deleted = Files.deleteIfExists(Paths.get(uploadDir, folderName, fileName));

			if (deleted) {
				logger.info("File deleted successfully: {}", fileName);
			} else {
				logger.warn("File not found for deletion: {}", fileName);
			}

			return deleted;

		} catch (IOException e) {
			logger.error("Error deleting file: {} | Error: {}", fileName, e.getMessage(), e);
			return false;
		}
	}

	// URL Image Upload
	@Override
	public String uploadFromUrl(String imageUrl, Long entityId, String oldFileName, String folderName) {

		logger.info("Upload from URL started | EntityId: {} | Folder: {}", entityId, folderName);

		if (imageUrl == null || imageUrl.isBlank()) {
			logger.warn("Image URL is null or blank");
			return null;
		}

		Path folderPath = createFolder(folderName);

		try {
			URL url = URI.create(imageUrl).toURL();

			URLConnection connection = url.openConnection();
			connection.setConnectTimeout(5000);
			connection.setReadTimeout(5000);

			//Content type
			String contentType = connection.getContentType();
			if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
				logger.warn("Invalid content type from URL: {}", contentType);
				return null;
			}

			long contentLength = url.openConnection().getContentLengthLong();

			// File size validation
			long maxAllowedSize =
					contentType.startsWith("video")
							? MAX_VIDEO_SIZE
							: MAX_IMAGE_SIZE;

			if (contentLength > maxAllowedSize) {

				logger.warn(
						"File size exceeded from URL | type: {} | size: {}",
						contentType,
						contentLength
				);

				return null;
			}

			// Extract extension safely
			// Detect extension from content type
			String extension;

			switch (contentType) {

				case "image/png" -> extension = ".png";

				case "image/jpeg",
					 "image/jpg" -> extension = ".jpg";

				case "image/webp" -> extension = ".webp";

				case "video/mp4" -> extension = ".mp4";

				case "video/webm" -> extension = ".webm";

				case "video/quicktime" -> extension = ".mov";

				default -> {

					logger.warn(
							"Unsupported file type: {}",
							contentType
					);

					return null;
				}
			}

			String fileName = entityId + "_" + UUID.randomUUID() + extension;

			try (InputStream in = connection.getInputStream()) {
				Files.copy(in, folderPath.resolve(fileName));
			}

			logger.info("Image downloaded and saved successfully: {}", fileName);

			// Delete old file
			if (oldFileName != null && !oldFileName.isBlank()) {
				boolean deleted = deleteFile(oldFileName, folderName);
				logger.info("Old file deletion | File: {} | Deleted: {}", oldFileName, deleted);
			}

			return fileName;

		} catch (IOException e) {
			logger.error("Failed to upload image from URL: {} | Error: {}", imageUrl, e.getMessage(), e);
			return null;
		}
	}

}

