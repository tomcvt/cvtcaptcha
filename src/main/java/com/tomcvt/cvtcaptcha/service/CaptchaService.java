package com.tomcvt.cvtcaptcha.service;

import java.io.File;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tomcvt.cvtcaptcha.dtos.CIOParameters;
import com.tomcvt.cvtcaptcha.enums.CaptchaType;
import com.tomcvt.cvtcaptcha.model.CaptchaData;
import com.tomcvt.cvtcaptcha.repository.CaptchaDataRepository;

@Service
public class CaptchaService {
    private final SolutionGenerator solutionGenerator;
    private final CaptchaDataRepository captchaDataRepository;
    private final CaptchaImageGenerator captchaImageGenerator;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public CaptchaService(SolutionGenerator solutionGenerator, 
                          CaptchaDataRepository captchaDataRepository,
                          CaptchaImageGenerator captchaImageGenerator) {
        this.solutionGenerator = solutionGenerator;
        this.captchaDataRepository = captchaDataRepository;
        this.captchaImageGenerator = captchaImageGenerator;
    }

    public CaptchaData createCaptcha(UUID requestId, CaptchaType type, String userIp) {
        CaptchaData captchaData = new CaptchaData();
        requestId = captchaData.getRequestId();
        captchaData.setType(type);
        captchaData.setUserIp(userIp);
        captchaData.setCreatedAt(System.currentTimeMillis());
        captchaData.setExpiresAt(System.currentTimeMillis() + 5 * 60 * 1000); 
        //TODO make switch here
        if (type == CaptchaType.CLICK_IN_ORDER) {
            String solution = solutionGenerator.generateCIOSolution();
            captchaData.setSolution(solution);
            File imageFile = captchaImageGenerator.generateEmojiCaptchaImage(requestId, solution);
            String imageData = "/captcha-images/" + imageFile.getName();
            captchaData.setData(imageData);
            float clickRadius = 0.1f;
            String parameters = null;
            try {
                parameters = objectMapper.writeValueAsString(new CIOParameters(clickRadius));
            } catch (Exception e) {
                //TODO logginh and proper handling
                e.printStackTrace();
            }
            captchaData.setParameters(parameters); //parameters to solve resolver
        }
        System.out.println("Solution for captcha " + requestId + ": " + captchaData.getSolution());
        return captchaDataRepository.save(captchaData);
    }

    public boolean verifyCaptchaSolution(UUID requestId, CaptchaType type, Object solution) {
        //TODO implement

        System.out.println("Verifying captcha solution for requestId: " + requestId + ", type: " + type + ", solution: " + solution);

        return true;
    }
}
