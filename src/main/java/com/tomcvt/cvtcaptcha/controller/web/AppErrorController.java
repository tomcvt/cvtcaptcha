package com.tomcvt.cvtcaptcha.controller.web;

import java.util.Map;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

@Profile({"prod","demo"})
@RestController
public class AppErrorController implements ErrorController {
    @RequestMapping("/error")
    public Map<String, Object> handleError(HttpServletRequest request) {
        Integer status = (Integer) request.getAttribute("jakarta.servlet.error.status_code");
        String message = (String) request.getAttribute("jakarta.servlet.error.message");
        return Map.of(
            "status", status,
            "error", message != null ? message : "Unexpected error"
        );
    }
}
