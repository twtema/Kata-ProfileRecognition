package org.kata.service.impl;

import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.kata.config.UrlProperties;
import org.kata.dto.RecognizeDocumentNewDto;
import org.kata.dto.enums.DocumentType;
import org.kata.exception.DocumentsRecognitionException;
import org.kata.service.DocumentServiceNew;
import org.kata.service.RecognizeTextFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Date;

@Service
@Slf4j
public class DocumentServiceNewImpl implements DocumentServiceNew {
    private final UrlProperties urlProperties;
    private final WebClient updateWebClient;
    private final RecognizeTextFile recognizeTextFile;


    public DocumentServiceNewImpl(UrlProperties urlProperties, RecognizeTextFile recognizeTextFile) {
        this.urlProperties = urlProperties;
        this.updateWebClient = WebClient.create(urlProperties.getProfileUpdateBaseUrl());
        this.recognizeTextFile = recognizeTextFile;
    }

    public RecognizeDocumentNewDto recognizeDocumentNew(String icp, DocumentType type, MultipartFile file) {
        Tesseract tesseract = new Tesseract();
        //tesseract.setDatapath("/path_to_tessdata"); // Укажите путь к tessdata

        String recognizedText;
        try {
            BufferedImage image = null;

            InputStream in = new ByteArrayInputStream(file.getBytes());

            image = ImageIO.read(in);

            recognizedText = tesseract.doOCR(image);
        } catch (TesseractException e) {
            throw new DocumentsRecognitionException("Ошибка при распознавании документа");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (StringUtil.isNullOrEmpty(recognizedText)) {
            throw new DocumentsRecognitionException("Не удалось распознать документ");
        }
        RecognizeDocumentNewDto recognizeDocumentNewDto = RecognizeDocumentNewDto.builder()
                .icp(icp)
                .name(recognizeTextFile.recognizeName(recognizedText))
                .surname(recognizeTextFile.recognizeSurname(recognizedText))
                .patronymic(recognizeTextFile.recognizePatronymic(recognizedText))
                .documentType(type)
                .documentNumber(recognizeTextFile.recognizeNumber(recognizedText))
                .documentSerial(recognizeTextFile.recognizeSerial(recognizedText))
                .expirationDate(recognizeTextFile.recognizeExpirationDate(recognizedText))
                .issueDate(recognizeTextFile.recognizeIssueDate(recognizedText))

                .externalDate(Date.from(Instant.now()))

                .build();

        postDocumentNew(recognizeDocumentNewDto);

        return recognizeDocumentNewDto;
    }

    private void postDocumentNew(RecognizeDocumentNewDto dto) {

        updateWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(urlProperties.getProfileUpdatePostDocuments())
                        .queryParam("icp", dto.getIcp())
                        .build())
                .body(Mono.just(dto), RecognizeDocumentNewDto.class)
                .retrieve()
                .bodyToMono(Void.class)
                .block();

        log.info("Распознанный документ успешно отправлен на обновление");
    }
}
