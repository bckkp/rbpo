package ru.mtuci.rbpo.security;

import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import ru.mtuci.rbpo.dto.ObservationRecordRequest;
import ru.mtuci.rbpo.dto.ObservationSessionRequest;
import ru.mtuci.rbpo.repository.ObservationRecordRepository;
import ru.mtuci.rbpo.repository.ObservationSessionRepository;
import ru.mtuci.rbpo.repository.UserRepository;

@Component("authorizationService")
@Transactional(readOnly = true)
public class AuthorizationService {

    private final UserRepository userRepository;
    private final ObservationSessionRepository sessionRepository;
    private final ObservationRecordRepository recordRepository;

    public AuthorizationService(UserRepository userRepository,
                                ObservationSessionRepository sessionRepository,
                                ObservationRecordRepository recordRepository) {
        this.userRepository = userRepository;
        this.sessionRepository = sessionRepository;
        this.recordRepository = recordRepository;
    }

    public boolean isCurrentUser(Long userId, Authentication authentication) {
        return isAdmin(authentication) || currentUserId(authentication).filter(userId::equals).isPresent();
    }

    public boolean canCreateSession(ObservationSessionRequest request, Authentication authentication) {
        return request != null && isCurrentUser(request.observerId(), authentication);
    }

    public boolean canUpdateSession(Long sessionId,
                                    ObservationSessionRequest request,
                                    Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }
        if (!ownsSession(sessionId, authentication)) {
            return false;
        }
        return request == null || request.observerId() == null
                || currentUserId(authentication).filter(request.observerId()::equals).isPresent();
    }

    public boolean canModifySession(Long sessionId, Authentication authentication) {
        return isAdmin(authentication) || ownsSession(sessionId, authentication);
    }

    public boolean canCreateRecord(ObservationRecordRequest request, Authentication authentication) {
        return request != null && (isAdmin(authentication) || ownsSession(request.sessionId(), authentication));
    }

    public boolean canUpdateRecord(Long recordId,
                                   ObservationRecordRequest request,
                                   Authentication authentication) {
        if (isAdmin(authentication)) {
            return true;
        }
        if (!ownsRecord(recordId, authentication)) {
            return false;
        }
        return request == null || request.sessionId() == null || ownsSession(request.sessionId(), authentication);
    }

    public boolean canModifyRecord(Long recordId, Authentication authentication) {
        return isAdmin(authentication) || ownsRecord(recordId, authentication);
    }

    private boolean ownsSession(Long sessionId, Authentication authentication) {
        Optional<Long> userId = currentUserId(authentication);
        return sessionId != null && userId.isPresent()
                && sessionRepository.findById(sessionId)
                .map(session -> userId.get().equals(session.getObserverId()))
                .orElse(false);
    }

    private boolean ownsRecord(Long recordId, Authentication authentication) {
        return recordId != null && recordRepository.findById(recordId)
                .map(record -> ownsSession(record.getSessionId(), authentication))
                .orElse(false);
    }

    private Optional<Long> currentUserId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        return userRepository.findByUsernameIgnoreCase(authentication.getName()).map(user -> user.getId());
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
