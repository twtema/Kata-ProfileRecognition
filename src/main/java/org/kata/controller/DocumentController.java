package org.kata.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.kata.dto.RecognizeDocumentDto;
import org.kata.dto.enums.DocumentType;
import org.kata.exception.DocumentsRecognitionException;
import org.kata.service.DocumentService;
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

    private final DocumentService documentService;

    @Operation(summary = "Get info about document")
    @ApiResponse(responseCode = "200", description = "Found the document",
            content = { @Content(mediaType = "application/json",
                    schema = @Schema(implementation = RecognizeDocumentDto.class))})
    @GetMapping()
    public ResponseEntity<RecognizeDocumentDto> getContactMedium(
            @RequestParam("icp") String icp,
            @RequestParam("type") DocumentType type,
            @RequestParam("file") MultipartFile file) {
        return new ResponseEntity<>(documentService.recognizeDocument(icp, type, file), HttpStatus.CREATED);
    }

    @Operation(summary = "Error")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400", description = "Invalid info",
                    content = @Content),
            @ApiResponse(responseCode = "404", description = "Document not found",
                    content = @Content) })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(DocumentsRecognitionException.class)
    public ErrorMessage getContactMediumHandler(DocumentsRecognitionException e) {
        return new ErrorMessage(e.getMessage());
    }
}
