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

    public String saveImage(MultipartFile file) throws IOException {
        if (file.isEmpty()) return null;

        // Şəkli buluda yükləyirik
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", "fregence/perfumes" // Cloudinary daxilində qovluq adı
        ));

        // Cloudinary-nin bizə verdiyi daimi linki qaytarırıq (https://res.cloudinary.com/...)
        return uploadResult.get("secure_url").toString();
    }
}