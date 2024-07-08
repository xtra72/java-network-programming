package com.nhnacademy.aiot;

public class InvalidStatusException extends RuntimeException {
    int code;

    public InvalidStatusException(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
