package com.superapp.boundaries.user;

import com.superapp.util.EmailChecker;

public class UserIdBoundary {

    private String superapp ;
    private String email ;

    public UserIdBoundary() {
        this.superapp = "2023a.noam.levy"; // TODO: change to super app name from application.properties
    }

    public UserIdBoundary(String email) {
        this();
        if (!EmailChecker.isValidEmail(email))
            throw new RuntimeException("invalid email");
        this.email = email;
    }

    public UserIdBoundary(String superApp, String email) {
        if (!EmailChecker.isValidEmail(email))
            throw new RuntimeException("invalid email");
        if (superApp.isBlank())
            throw  new RuntimeException("super-app name cannot be empty");
        this.superapp = superApp;
        this.email = email;
    }

    public String getSuperapp() {
        return superapp;
    }

    public void setSuperapp(String superApp) {
        this.superapp = superApp;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        if (EmailChecker.isValidEmail(email))
            this.email = email;
        else
            throw new RuntimeException("Invalid email");
    }

    @Override
    public String toString() {
        return "UserIdBoundary{" +
                "superApp='" + superapp + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
