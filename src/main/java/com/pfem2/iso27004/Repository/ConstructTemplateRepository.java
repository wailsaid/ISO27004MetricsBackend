package com.pfem2.iso27004.Repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pfem2.iso27004.Entity.ConstructTemplate;

@Repository
public interface ConstructTemplateRepository extends JpaRepository<ConstructTemplate, Long> {

    Optional<ConstructTemplate> findByConstructId(String constructId);

    List<ConstructTemplate> findByCategory(String category);

    boolean existsByConstructId(String constructId);
}
