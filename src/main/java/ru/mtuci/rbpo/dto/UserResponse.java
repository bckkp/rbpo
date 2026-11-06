package ru.mtuci.rbpo.dto;

import ru.mtuci.rbpo.model.UserRole;
import ru.mtuci.rbpo.model.UserStatus;

public record UserResponse(Long id,
                           String username,
                           String fullName,
                           String email,
                           UserRole role,
                           UserStatus status) {
}
