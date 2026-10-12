package com.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.app.model.Personal;

public interface PersonalRepository extends JpaRepository<Personal, Long> {

    List<Personal> findByActivoTrue();

    Optional<Personal> findByIdAndActivoTrue(Long id);
}
