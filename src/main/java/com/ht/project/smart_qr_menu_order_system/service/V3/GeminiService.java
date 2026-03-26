package com.ht.project.smart_qr_menu_order_system.service.V3;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Part;
import com.ht.project.smart_qr_menu_order_system.service.AIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
public class GeminiService implements AIService {
    private final Client client;

    @Value("${gemini.model}")
    private String model;

    public GeminiService(@Value("${gemini.api-key:}") String apiKey) {
        Client.Builder builder = Client.builder();
        if (apiKey != null && !apiKey.isBlank()) {
            builder.apiKey(apiKey);
        }
        this.client = builder.build();
    }

    @Override
    public String generateText(MultipartFile image, String prompt) throws IOException {
        Content content = Content.fromParts(
                Part.fromText(prompt),
                Part.fromBytes(image.getBytes(), image.getContentType())
        );
        GenerateContentConfig config = GenerateContentConfig.builder()
                .temperature(0.2f)
                .responseMimeType("application/json")
                .build();
        var response = client.models.generateContent(
                model,
                content,
                config
        );
        log.info(response.text());
        return response.text();
    }
}
