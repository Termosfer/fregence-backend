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
                
                // --- OPTİMİZASİYA PARAMETRLƏRİ ---
                "quality", "auto",        // Cloudinary avtomatik ən yaxşı sıxılmanı seçir
                "fetch_format", "auto",  // Brauzerə uyğun format (məsələn, WebP) təyin edir
                "width", 800,            // Şəklin enini maksimum 800px edir
                "height", 800,           // Şəklin hündürlüyünü maksimum 800px edir
                "crop", "limit"          // Şəkli deformasiya etmədən böyükdürsə kiçildir
                // ----------------------------------
        ));

        return Map.of(
            "url", uploadResult.get("secure_url").toString(),
            "public_id", uploadResult.get("public_id").toString()
        );
    
    }

    // 2. Şəkli Cloudinary-dən silir
    public void deleteImage(String publicId) throws IOException {
        if (publicId != null && !publicId.isEmpty()) {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        }
    }
}