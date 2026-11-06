package ru.mtuci.rbpo.dto;

public record RegistrationRequest(String username, String fullName, String email, String password) {
}
