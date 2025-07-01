package com.tezish.demo.services.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.tezish.demo.enums.EStatus;
import com.tezish.demo.model.DownloadToken;
import com.tezish.demo.model.User;
import com.tezish.demo.repository.DownloadTokenRepository;
import com.tezish.demo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import java.util.Random;

@Slf4j
@Service
public class PdfServiceImpl implements PdfService {

    @Value("${pdf.expiration}")
    private long pdfExpiration;

    private final UserRepository userRepository;
    private final TemplateEngine templateEngine;
    private final DownloadTokenRepository downloadTokenRepository;

    public PdfServiceImpl(
            UserRepository userRepository,
            TemplateEngine templateEngine,
            DownloadTokenRepository downloadTokenRepository
    ) {
        this.userRepository = userRepository;
        this.templateEngine = templateEngine;
        this.downloadTokenRepository = downloadTokenRepository;
    }


    @Override
    public String generatePdf(Authentication authentication) throws Exception {
        log.info("Generating PDF for user: {}", authentication.getName());
        if (authentication.getName() == null) {
            throw new RuntimeException("Authentication is null or user not authenticated");
        }
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Context context = prepareContext(user);

        String renderedHtml = templateEngine.process("pdf.html", context);

        byte[] pdfBytes = generatePdf(renderedHtml);

        Path uploadDir = Paths.get("uploads");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String pdfName = "user_" + user.getId() + "_" + System.currentTimeMillis() + ".pdf";
        Path pdfPath = Paths.get(uploadDir.toString(), pdfName);

        Files.write(pdfPath, pdfBytes);
        log.info("PDF saved successfully at: {}", pdfPath);

        DownloadToken downloadToken = DownloadToken.builder()
                .token(generateToken())
                .fileName(pdfPath.getFileName().toString())
                .expirationDate(new Date(System.currentTimeMillis() + Duration.ofMinutes(pdfExpiration).toMillis()))
                .userId(user.getId())
                .status(EStatus.ACTIVE)
                .build();

        downloadTokenRepository.save(downloadToken);
        return "/download/" + downloadToken.getToken();
    }

    private String generateToken() {
        byte[] randomBytes = new byte[64];
        new Random().nextBytes(randomBytes);
        StringBuilder token = new StringBuilder();
        for (byte b : randomBytes) {
            token.append(String.format("%02x", b));
        }
        return token.toString();
    }


    public Context prepareContext(User user) {
        try {
            Context context = new Context();

            context.setVariable("user", Map.of(
                    "fullName", user.getFullName() != null ? user.getFullName() : "",
                    "email", user.getEmail(),
                    "role", user.getRole() != null ? user.getRole().toString() : "",
                    "imageUrl", user.getImageUrl() != null ? user.getImageUrl()
                            : new File("src/main/resources/templates/img.png").toURI().toString()
            ));
            return context;
        } catch (Exception e) {
            throw new RuntimeException("Error preparing context: " + e.getMessage());
        }
    }

    public byte[] generatePdf(String htmlContent) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PdfRendererBuilder builder = new PdfRendererBuilder();
        builder.useFastMode();
        builder.withHtmlContent(htmlContent, null); // No base URI required
        builder.toStream(outputStream);
        builder.run();
        return outputStream.toByteArray();
    }
}
