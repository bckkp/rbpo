package ru.mtuci.rbpo.service;

import static ru.mtuci.rbpo.service.Checks.notBlank;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ru.mtuci.rbpo.exception.BadRequestException;
import ru.mtuci.rbpo.exception.ConflictException;
import ru.mtuci.rbpo.exception.NotFoundException;
import ru.mtuci.rbpo.model.User;
import ru.mtuci.rbpo.repository.UserRepository;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final ObservationSessionService sessionService;

    public UserService(UserRepository userRepository, ObservationSessionService sessionService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("Пользователь", id));
    }

    public User create(User user) {
        user.setId(null);
        validate(user);
        return userRepository.save(user);
    }

    public User update(Long id, User patch) {
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
}
