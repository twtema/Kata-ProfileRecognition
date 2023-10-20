package org.kata.service;

import org.kata.dto.RecognizeDocumentDto;
import org.kata.dto.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentService {
    RecognizeDocumentDto recognizeDocument(String icp, DocumentType type, MultipartFile file);
}
