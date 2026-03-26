package com.ht.project.smart_qr_menu_order_system.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

public interface AIService {
    String generateText(MultipartFile image, String prompt) throws IOException;
}
