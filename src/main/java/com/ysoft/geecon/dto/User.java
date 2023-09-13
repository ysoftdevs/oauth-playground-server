package com.ysoft.geecon.dto;

public record User(String login, String password) {
    public boolean validatePassword(String password) {
        return this.password != null && this.password.equals(password);
    }
}
