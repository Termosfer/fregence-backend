package com.fregence.fregence.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class FileService {

    @Autowired
    private Cloudinary cloudinary;

    // 1. Şəkli yükləyir və məlumatları Map kimi qaytarır
    public Map<String, String> uploadImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) return null;

        Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "fregence/perfumes",
                "quality", "auto",
                "fetch_format", "auto",
                "width", 800,
                "height", 800,
                "crop", "limit"
        ));

        return Map.of(
            "url", uploadResult.get("secure_url").toString(),
            "public_id", uploadResult.get("public_id").toString()
        );
    }

    // 2. Şəkli Cloudinary-den silir (XƏTANI DAXİLDƏ HƏLL EDİRİK)
    public void deleteImage(String publicId) {
        try {
            if (publicId != null && !publicId.isEmpty()) {
                cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            }
        } catch (IOException e) {
            // Şəkil silinməsə belə proqram dayanmasın, sadəcə xətanı konsola yazırıq
            System.err.println("Cloudinary-den shekil silinerken xeta bash verdi: " + e.getMessage());
        }
    }
}