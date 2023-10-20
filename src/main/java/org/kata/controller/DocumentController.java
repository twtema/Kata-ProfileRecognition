package org.kata.controller;

import lombok.RequiredArgsConstructor;
import org.kata.dto.RecognizeDocumentDto;
import org.kata.dto.enums.DocumentType;
import org.kata.exception.DocumentsRecognitionException;
import org.kata.service.DocumentService;
import org.springdoc.api.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/recognition")
public class DocumentController {

    private final DocumentService documentService;

    @GetMapping()
    public ResponseEntity<RecognizeDocumentDto> getContactMedium(
            @RequestParam("icp") String icp,
            @RequestParam("type") DocumentType type,
            @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(documentService.recognizeDocument(icp, type, file), HttpStatus.CREATED);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DocumentsRecognitionException.class)
    public ErrorMessage getContactMediumHandler(DocumentsRecognitionException e) {
        return new ErrorMessage(e.getMessage());
    }
}
