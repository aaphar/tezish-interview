package com.tezish.demo.services.pdf;

import org.springframework.security.core.Authentication;

public interface PdfService {

    String generatePdf(Authentication authentication) throws Exception;

}
