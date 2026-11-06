package ru.mtuci.rbpo.service;

import static ru.mtuci.rbpo.service.Checks.notBlank;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.mtuci.rbpo.exception.BadRequestException;
import ru.mtuci.rbpo.exception.ConflictException;
import ru.mtuci.rbpo.exception.NotFoundException;
import ru.mtuci.rbpo.model.User;
import ru.mtuci.rbpo.model.UserRole;
import ru.mtuci.rbpo.model.UserStatus;
import ru.mtuci.rbpo.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ObservationSessionService sessionService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       ObservationSessionService sessionService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Пользователь", id));
    }

    public User create(User user, String password, UserRole role, UserStatus status) {
        user.setId(null);
        user.setPasswordHash(encodePassword(password));
        user.setRole(role == null ? UserRole.OBSERVER : role);
        user.setStatus(status == null ? UserStatus.ACTIVE : status);
        validate(user);
        return userRepository.save(user);
    }

    /** Публичная регистрация всегда создаёт обычного активного наблюдателя. */
    public User register(User user, String password) {
        return create(user, password, UserRole.OBSERVER, UserStatus.ACTIVE);
    }

    public User update(Long id,
                       User patch,
                       String password,
                       UserRole role,
                       UserStatus status) {
        User user = findById(id);
        if (patch.getUsername() != null) {
            user.setUsername(patch.getUsername());
        }
        if (patch.getFullName() != null) {
            user.setFullName(patch.getFullName());
        }
        if (patch.getEmail() != null) {
            user.setEmail(patch.getEmail());
        }
        if (password != null) {
            user.setPasswordHash(encodePassword(password));
        }
        if (role != null) {
            user.setRole(role);
        }
        if (status != null) {
            user.setStatus(status);
        }
        validate(user);
        return userRepository.save(user);
    }

    /** Удаляет пользователя вместе с его сеансами наблюдений и их записями. */
    public void delete(Long id) {
        findById(id);
        sessionService.deleteAllByObserverId(id);
        userRepository.deleteById(id);
    }

    private void validate(User user) {
        user.setUsername(notBlank(user.getUsername(), "username"));
        user.setFullName(notBlank(user.getFullName(), "fullName"));
        user.setEmail(notBlank(user.getEmail(), "email"));
        if (!user.getEmail().matches("[^@\\s]+@[^@\\s]+\\.[^@\\s]+")) {
            throw new BadRequestException("Некорректный email: " + user.getEmail());
        }
        boolean usernameTaken = user.getId() == null
                ? userRepository.existsByUsernameIgnoreCase(user.getUsername())
                : userRepository.existsByUsernameIgnoreCaseAndIdNot(user.getUsername(), user.getId());
        if (usernameTaken) {
            throw new ConflictException("Имя пользователя '" + user.getUsername() + "' уже занято");
        }
        boolean emailTaken = user.getId() == null
                ? userRepository.existsByEmailIgnoreCase(user.getEmail())
                : userRepository.existsByEmailIgnoreCaseAndIdNot(user.getEmail(), user.getId());
        if (emailTaken) {
            throw new ConflictException("Email '" + user.getEmail() + "' уже используется");
        }
    }

    private String encodePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new BadRequestException("Поле 'password' обязательно");
        }
        if (password.length() < 8 || password.length() > 72
                || !password.matches(".*[A-ZА-Я].*")
                || !password.matches(".*[a-zа-я].*")
                || !password.matches(".*\\d.*")
                || !password.matches(".*[^A-Za-zА-Яа-я0-9].*")) {
            throw new BadRequestException(
                    "Пароль должен содержать 8–72 символа, прописную и строчную буквы, цифру и специальный символ");
        }
        return passwordEncoder.encode(password);
    }
}
