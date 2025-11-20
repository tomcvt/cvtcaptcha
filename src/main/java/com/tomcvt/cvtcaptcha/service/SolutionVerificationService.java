package com.tomcvt.cvtcaptcha.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.tomcvt.cvtcaptcha.dtos.Point;
import com.tomcvt.cvtcaptcha.utility.SolutionParser;

@Service
public class SolutionVerificationService {
    private final int imageWidth; // = 400;
    private final int imageHeight; // = 300;
    private final int radius; // = 36;

    public SolutionVerificationService(@Value("${app.image.width}") int imageWidth,
                                       @Value("${app.image.height}") int imageHeight,
                                       @Value("${app.image.emote-r}") int radius) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.radius = radius;
    }

    public boolean verifyCIOSolution(String correctSolution, String userSolution) {
        List<Point> correctPoints = SolutionParser.parseCIOSolution(correctSolution);
        List<Point> userPoints = SolutionParser.parseCIOSolution(userSolution);

        if (correctPoints.size() != userPoints.size()) {
            return false;
        }

        for (int i = 0; i < correctPoints.size(); i++) {
            Point correctPoint = correctPoints.get(i);
            Point userPoint = userPoints.get(i);

            float correctX = correctPoint.x() * imageWidth;
            float correctY = correctPoint.y() * imageHeight;
            float userX = userPoint.x() * imageWidth;
            float userY = userPoint.y() * imageHeight;

            float dx2 = (correctX - userX) * (correctX - userX);
            float dy2 = (correctY - userY) * (correctY - userY);
            if (dx2 + dy2 < radius * radius) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }
}
