package ru.mtuci.rbpo.dto;

/** Тело POST/PATCH для пользователя. В PATCH передаются только изменяемые поля. */
public record UserRequest(String username, String fullName, String email) {
}
