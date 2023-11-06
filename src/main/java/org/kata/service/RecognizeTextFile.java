package org.kata.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class RecognizeTextFile {

    public String recognizeName(List<String> recognizedText) {
        return recognizedText.get(1);
    }

    public String recognizeSurname(List<String> recognizedText) {
        return recognizedText.get(0);
    }

    public String recognizePatronymic(List<String> recognizedText) {
        return recognizedText.get(2);
    }

    public String recognizeNumber(List<String> recognizedText) {
        return recognizedText.get(2);
    }

    public String recognizeSerial(List<String> recognizedText) {
        return recognizedText.get(0) + recognizedText.get(1);
    }

    public Date recognizeExpirationDate(List<String> recognizedText) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate date = LocalDate.parse(recognizedText.get(1), formatter);
            return Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
        } catch (Exception e) {
            return null;
        }
    }

    public Date recognizeIssueDate(List<String> recognizedText) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDate date = LocalDate.parse(recognizedText.get(0), formatter);
        return Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }


}
