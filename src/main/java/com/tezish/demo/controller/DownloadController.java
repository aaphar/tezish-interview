package com.tezish.demo.controller;

import com.tezish.demo.enums.EStatus;
import com.tezish.demo.model.DownloadToken;
import com.tezish.demo.repository.DownloadTokenRepository;
import com.tezish.demo.services.pdf.PdfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

@Slf4j
@CrossOrigin(origins = {"*"}, maxAge = 3600)
@RestController
@RequestMapping("/api/pdf")
public class DownloadController {

    private final PdfService pdfService;
    private final DownloadTokenRepository downloadTokenRepository;


    public DownloadController(PdfService pdfService, DownloadTokenRepository downloadTokenRepository) {
        this.pdfService = pdfService;
        this.downloadTokenRepository = downloadTokenRepository;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> createDownloadLink(Authentication authentication) throws Exception {
        return ResponseEntity.ok(pdfService.generatePdf(authentication));
    }

    @GetMapping("/download/{token}")
    public ResponseEntity<Resource> download(@PathVariable String token) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.info("Authorities: {}", auth.getAuthorities());
        DownloadToken stored = downloadTokenRepository.findByToken(token);

        if (stored == null || stored.getExpirationDate().before(new Date()) || stored.getStatus().name().equals("EXPIRED")) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Path filePath = Paths.get("uploads", stored.getFileName()).normalize();
        Resource file = new FileSystemResource(filePath);

        if (!file.exists()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        stored.setStatus(EStatus.EXPIRED); // update the status to EXPIRED for one time access
        downloadTokenRepository.save(stored);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(file);
    }

}
