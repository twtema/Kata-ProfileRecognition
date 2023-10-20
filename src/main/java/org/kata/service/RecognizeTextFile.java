package org.kata.service;


import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
public class RecognizeTextFile {

    public String recognizeName(String recognizedText) {
        //TODO реализовать распознование
        return "name";
    }

    public String recognizeSurname(String recognizedText) {
        //TODO реализовать распознование
        return "surname";
    }

    public String recognizePatronymic(String recognizedText) {
        //TODO реализовать распознование
        return "patronymic";
    }

    public String recognizeNumber(String recognizedText) {
        //TODO реализовать распознование
        return "Number";
    }

    public String recognizeSerial(String recognizedText) {
        //TODO реализовать распознование
        return "Serial";
    }

    public Date recognizeExpirationDate(String recognizedText) {
        //TODO реализовать распознование
        return new Date();
    }

    public Date recognizeIssueDate(String recognizedText) {
        //TODO реализовать распознование
        return new Date();
    }

}
