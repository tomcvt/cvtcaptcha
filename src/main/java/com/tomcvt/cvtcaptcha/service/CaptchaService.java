package com.tomcvt.cvtcaptcha.service;

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
        captchaData.setType(type);
        captchaData.setUserIp(userIp);
        captchaData.setCreatedAt(System.currentTimeMillis());
        captchaData.setExpiresAt(System.currentTimeMillis() + 5 * 60 * 1000); 
        //TODO make switch here
        if (type == CaptchaType.CLICK_IN_ORDER) {
            String solution = solutionGenerator.generateCIOSolution();
            captchaData.setSolution(solution);
            //String imageData = captchaImageGenerator.generateCIOImage(solution);
            //captchaData.setData(imageData);
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
        return captchaDataRepository.save(captchaData);
    }
}
