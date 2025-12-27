package com.tomcvt.cvtcaptcha.controller.web;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
public class ErrorPageController implements ErrorController {
    @RequestMapping(value ="/error", produces = "text/html")
    @ResponseBody
    public ResponseEntity<String> handleError() {
        /*
        if (code != null) {
            switch (code) {
                case "404":
                    return "errors/404";
                case "403":
                    return "errors/403";
                case "500":
                    return "errors/500";
                default:
                    return "errors/generic";
            }
        }
        return "errors/generic";
        */
       return ResponseEntity.status(404).body("The requested page was not found.");
    }
}
