package com.fregence.fregence.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileService {

    private final String uploadDir = "uploads/perfumes/";

    public String saveImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) return null;

        // Qovluğu yarat (əgər yoxdursa)
        Path path = Paths.get(uploadDir);
        if (!Files.exists(path)) {
            Files.createDirectories(path);
        }

        // Şəkil üçün unikal ad yarat: abc-123-jpg.jpg
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        Path filePath = path.resolve(fileName);

        // Faylı kopyala
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return fileName;
    }
}