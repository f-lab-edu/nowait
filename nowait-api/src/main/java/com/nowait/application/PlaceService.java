package com.nowait.application;

import com.nowait.domain.repository.PlaceRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PlaceService {

    private final PlaceRepository placeRepository;

    public void validatePlaceExist(Long placeId, String errorMessage) {
        if (!placeRepository.existsById(placeId)) {
            throw new EntityNotFoundException(errorMessage);
        }
    }
}
