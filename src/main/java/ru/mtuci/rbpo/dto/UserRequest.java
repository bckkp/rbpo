package ru.mtuci.rbpo.dto;

import ru.mtuci.rbpo.model.UserRole;
import ru.mtuci.rbpo.model.UserStatus;

/** Тело POST/PATCH для пользователя. В PATCH передаются только изменяемые поля. */
public record UserRequest(String username,
                          String fullName,
                          String email,
                          String password,
                          UserRole role,
                          UserStatus status) {
}
