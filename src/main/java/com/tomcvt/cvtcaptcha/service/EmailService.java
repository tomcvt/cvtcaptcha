package com.tomcvt.cvtcaptcha.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final String domainUrl;
    private final String recoveryUrl;

    public EmailService(JavaMailSender mailSender,
            @Value("${app.domain}") String domainUrl){
        this.mailSender = mailSender;
        this.domainUrl = domainUrl;
        this.recoveryUrl = domainUrl + "/public/reset-password?token=";
    }

    /*
    public boolean sendEmail(String to, String subject, String body) {
        
        return true;
    }
    */
    //TODO provide link to signal unintended password reset (track ip of triggering request)
    public boolean sendRecoveryEmail(String to, String token) throws MessagingException {
        String subject = "Email Verification";
        String recoveryWithTokenUrl = recoveryUrl + token;
        //TODO think how to handle unintended password reset
        String unintendedResetUrl = domainUrl + "/unintended-password-reset";
        String body = """
                <HTML>
                <body>
                <p>Dear User,</p>
                <p>Click the link to reset your password:</p>
                <a href="%s">Reset Password</a>
                <p>The link is active for 15 minutes since your request</p>
                <p>If you did not trigger password registration, Click the link below</p>
                <a href="%s">Unintended Password Reset</a>
                """;
        body = String.format(body, recoveryWithTokenUrl, unintendedResetUrl);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        mailSender.send(message);
        return true;
    }

    public boolean sendActivationEmail(String to, String token) throws MessagingException {
        String subject = "Email Verification";
        String activationUrl = domainUrl + "/public/verify-email?token=" + token;
        String body = """
                <HTML>
                <body>
                <p>Dear User,</p>
                <p>Click the link to verify your email address:</p>
                <a href="%s">Verify Email</a>
                <p>The link is active for 15 minutes since your registration</p>
                """;
        body = String.format(body, activationUrl);
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(body, true);
        mailSender.send(message);
        return true;
    }


}
