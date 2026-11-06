package ru.mtuci.rbpo.dto;

public record CsrfTokenResponse(String token, String headerName, String parameterName) {
}
