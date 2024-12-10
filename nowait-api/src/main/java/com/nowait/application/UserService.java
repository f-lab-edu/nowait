package com.nowait.application;

import com.nowait.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public void validateUserExist(Long userId, String errorMessage) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException(errorMessage);
        }
    }

}
