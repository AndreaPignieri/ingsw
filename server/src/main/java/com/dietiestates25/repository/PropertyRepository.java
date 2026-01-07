package com.dietiestates25.repository;

import com.dietiestates25.model.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PropertyRepository extends JpaRepository<Property, Long>, JpaSpecificationExecutor<Property> {

        @Query(value = "SELECT p FROM Property p WHERE ST_Distance(p.location, :point) < :radiusInMeters")
        Page<Property> findByLocationNear(@Param("point") org.locationtech.jts.geom.Point point,
                        @Param("radiusInMeters") double radiusInMeters, Pageable pageable);
}
