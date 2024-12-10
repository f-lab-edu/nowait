package com.nowait.domain.repository;

import com.nowait.domain.model.place.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {

}
