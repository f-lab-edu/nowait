package com.nowait.place.domain.repository;

import com.nowait.place.domain.model.Place;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceRepository extends JpaRepository<Place, Long> {

}
