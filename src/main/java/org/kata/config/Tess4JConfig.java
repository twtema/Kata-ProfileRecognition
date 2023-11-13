package org.kata.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.awt.*;

@Component
@Getter
@Setter
public class Tess4JConfig {

    private String tessdataSrc = "src\\main\\resources\\tessdata";

    private String trainedData = "rus-best";

    private int passportWidth = 900;

    private int passportHeight = 1200;

    private Rectangle passportFullNameBox = new Rectangle(370, 630, 405, 225);

    private Rectangle passportSerialNumsBox = new Rectangle(775, 125, 127, 451);

    private Rectangle passportIssueDataBox = new Rectangle(71, 236, 300, 60);

    private int drivingLicenseWidth = 1200;

    private int drivingLicenseHeight = 800;

    private Rectangle drivingLicenseNameBox = new Rectangle(480, 120, 680, 175);

    private Rectangle drivingLicenseSerialNumsBox = new Rectangle(480, 500, 250, 90);

    private Rectangle drivingLicenseIssueDataBoxX = new Rectangle(480, 410, 586, 60);
}
