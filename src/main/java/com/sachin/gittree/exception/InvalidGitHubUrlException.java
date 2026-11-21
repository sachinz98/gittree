package com.sachin.gittree.exception;

public class InvalidGitHubUrlException extends RuntimeException {
    public InvalidGitHubUrlException(String message) {
        super(message);
    }
}