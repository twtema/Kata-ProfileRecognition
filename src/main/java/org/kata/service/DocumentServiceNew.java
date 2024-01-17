package org.kata.service;

import org.kata.dto.RecognizeDocumentNewDto;
import org.kata.dto.enums.DocumentType;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentServiceNew {
    RecognizeDocumentNewDto recognizeDocumentNew(String icp, DocumentType type, MultipartFile file);
}
