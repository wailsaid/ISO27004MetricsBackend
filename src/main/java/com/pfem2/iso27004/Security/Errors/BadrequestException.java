package com.pfem2.iso27004.Security.Errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadrequestException extends RuntimeException {
    public BadrequestException(String message) {
        super(message);
    }
}
