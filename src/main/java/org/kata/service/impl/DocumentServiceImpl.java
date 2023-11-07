package org.kata.service.impl;

import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.kata.config.Tess4JConfig;
import org.kata.config.UrlProperties;
import org.kata.dto.RecognizeDocumentDto;
import org.kata.dto.enums.DocumentType;
import org.kata.exception.DocumentsRecognitionException;
import org.kata.service.DocumentService;
import org.kata.service.RecognizeTextFile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class DocumentServiceImpl implements DocumentService {
    private final UrlProperties urlProperties;
    private final WebClient updateWebClient;
    private final Tess4JConfig tess4JConfig;
    private final RecognizeTextFile recognizeTextFile;
    private static final String TESSDATA_SRC = "src/main/resources/tessdata";
    private static final String TRAINED_DATA = "rus-best";
    private static final int PASSPORT_WIDTH = 900;
    private static final int PASSPORT_HEIGHT = 1200;
    private static final Rectangle PASSPORT_FULL_NAME_BOX = new Rectangle(370, 630, 405, 225);
    private static final Rectangle PASSPORT_SERIAL_NUMS_BOX = new Rectangle(775, 125, 127, 451);
    private static final Rectangle PASSPORT_ISSUE_DATA_BOX = new Rectangle(71, 236, 300, 60);
    private static final int DRIVING_LICENSE_WIDTH = 1200;
    private static final int DRIVING_LICENSE_HEIGHT = 800;
    private static final Rectangle DRIVING_LICENSE_FULL_NAME_BOX = new Rectangle(480, 120, 680, 175);
    private static final Rectangle DRIVING_LICENSE_SERIAL_NUMS_BOX = new Rectangle(480, 500, 250, 90);
    private static final Rectangle DRIVING_LICENSE_ISSUE_DATA_BOX = new Rectangle(480, 410, 586, 60);


    public DocumentServiceImpl(UrlProperties urlProperties, RecognizeTextFile recognizeTextFile, Tess4JConfig tess4JConfig) {
        this.urlProperties = urlProperties;
        this.updateWebClient = WebClient.create(urlProperties.getProfileUpdateBaseUrl());
        this.recognizeTextFile = recognizeTextFile;
        this.tess4JConfig = tess4JConfig;
    }

    public RecognizeDocumentDto recognizeDocument(String icp, DocumentType type, MultipartFile file) {
        List<String> documentInfo;
        List<String> itemsFullName;
        List<String> itemsSerialNums;
        List<String> itemsIssueData = new ArrayList<>();
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath(tess4JConfig.getTessdataSrc());
        tesseract.setLanguage(tess4JConfig.getTrainedData());
        tesseract.setPageSegMode(ITessAPI.TessPageSegMode.PSM_AUTO_ONLY);

        switch (type) {
            case RF_PASSPORT -> {
                documentInfo = getDocumentText(tesseract, file, tess4JConfig.getPassportWidth(), tess4JConfig.getPassportHeight(),
                        tess4JConfig.getPassportFullNameBox(), tess4JConfig.getPassportSerialNumsBox(), tess4JConfig.getPassportIssueDataBox());
                itemsFullName = new ArrayList<>(List.of(documentInfo.get(0).replaceAll("\\n", " ")
                        .replaceAll("\\s+", " ")
                        .split("\\s")));
                itemsSerialNums = new ArrayList<>(List.of(documentInfo.get(1).replaceAll("[^0-9\\s]", "")
                        .replaceAll("\\n", " ")
                        .replaceAll("\\s+", " ")
                        .split("\\s")));
                Pattern pattern = Pattern.compile("\\d\\d.\\d\\d.\\d\\d\\d\\d");
                Matcher matcher = pattern.matcher(documentInfo.get(2));
                while (matcher.find()) {
                    itemsIssueData.add(documentInfo.get(2).substring(matcher.start(), matcher.end()));
                }
            }
            case RF_DRIVING_LICENSE -> {
                documentInfo = getDocumentText(tesseract, file, tess4JConfig.getDrivingLicenseWidth(), tess4JConfig.getDrivingLicenseHeight(),
                        tess4JConfig.getDrivingLicenseNameBox(), tess4JConfig.getDrivingLicenseSerialNumsBox(), tess4JConfig.getDrivingLicenseIssueDataBoxX());
                itemsFullName = new ArrayList<>(List.of(documentInfo.get(0).replaceAll("\\n", " ")
                        .replaceAll("\\s\\s", " ")
                        .split("\\s")));
                itemsFullName.remove(1);
                itemsSerialNums = new ArrayList<>(List.of(documentInfo.get(1).replaceAll("\\n", " ")
                        .replaceAll("\\s\\s", " ")
                        .split("\\s")));
                itemsSerialNums.remove(0);
                itemsSerialNums.remove(0);
                Pattern pattern = Pattern.compile("\\d\\d.\\d\\d.\\d\\d\\d\\d");
                Matcher matcher = pattern.matcher(documentInfo.get(2));
                while (matcher.find()) {
                    itemsIssueData.add(documentInfo.get(2).substring(matcher.start(), matcher.end()));
                }
            }
            default -> throw new DocumentsRecognitionException("Неверный тип документа");
        }

        RecognizeDocumentDto recognizeDocumentDto = RecognizeDocumentDto.builder()
                .icp(icp)
                .name(recognizeTextFile.recognizeName(itemsFullName))
                .surname(recognizeTextFile.recognizeSurname(itemsFullName))
                .patronymic(recognizeTextFile.recognizePatronymic(itemsFullName))
                .documentType(type)
                .documentNumber(recognizeTextFile.recognizeNumber(itemsSerialNums))
                .documentSerial(recognizeTextFile.recognizeSerial(itemsSerialNums))
                .expirationDate(recognizeTextFile.recognizeExpirationDate(itemsIssueData))
                .issueDate(recognizeTextFile.recognizeIssueDate(itemsIssueData))
                .build();

        postDocument(recognizeDocumentDto);

        return recognizeDocumentDto;
    }

    private void postDocument(RecognizeDocumentDto dto) {
        updateWebClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path(urlProperties.getProfileUpdatePostDocuments())
                        .queryParam("icp", dto.getIcp())
                        .build())
                .body(Mono.just(dto), RecognizeDocumentDto.class)
                .retrieve()
                .bodyToMono(Void.class)
                .block();

        log.info("Распознанный документ успешно отправлен на обновление");
    }

    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }

    private List<String> getDocumentText (Tesseract tesseract, MultipartFile file, int targetWidth,
                                          int targetHeight, Rectangle fullNameBox, Rectangle serialNumsBox,
                                          Rectangle issueDateBox) {
        BufferedImage image;
        String recognizedTextFullName;
        String recognizedTextSerialNums;
        String recognizedTextIssueDate;
        List<String> result = new ArrayList<>();

        try {
            image = resizeImage(ImageIO.read(file.getInputStream()), targetWidth, targetHeight);
            recognizedTextFullName = tesseract.doOCR(image, fullNameBox);
            recognizedTextSerialNums = tesseract.doOCR(image, serialNumsBox);
            recognizedTextIssueDate = tesseract.doOCR(image, issueDateBox);
        } catch (IOException | TesseractException e) {
            throw new DocumentsRecognitionException("Ошибка при чтении документа");
        }
        if (StringUtil.isNullOrEmpty(recognizedTextFullName) || StringUtil.isNullOrEmpty(recognizedTextSerialNums)) {
            throw new DocumentsRecognitionException("Не удалось распознать документ");
        }

        result.add(recognizedTextFullName);
        result.add(recognizedTextSerialNums);
        result.add(recognizedTextIssueDate);

        return result;
    }
}
