package org.kata.controller;

import lombok.RequiredArgsConstructor;
import org.kata.dto.RecognizeDocumentNewDto;
import org.kata.dto.enums.DocumentType;
import org.kata.exception.DocumentsRecognitionException;
import org.kata.service.DocumentServiceNew;
import org.springdoc.api.ErrorMessage;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("v1/recognition")
public class DocumentController {

    private final DocumentServiceNew documentService;

    @GetMapping()
    public ResponseEntity<RecognizeDocumentNewDto> getContactMedium(
            @RequestParam("icp") String icp,
            @RequestParam("type") DocumentType type,
            @RequestParam(value = "file") MultipartFile file) {

        return new ResponseEntity<>(documentService.recognizeDocumentNew(icp, type, file), HttpStatus.CREATED);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DocumentsRecognitionException.class)
    public ErrorMessage getContactMediumHandler(DocumentsRecognitionException e) {
        return new ErrorMessage(e.getMessage());
    }
}
