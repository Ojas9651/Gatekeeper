package com.example.gatekeeper.management.repository;

import com.example.gatekeeper.management.entity.Organization;
import com.example.gatekeeper.management.entity.OrganizationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    boolean existsByNameIgnoreCase(String name);
    boolean existsBySlugIgnoreCase(String name);
    Optional<Organization> findByIdAndStatusNot(Long id, OrganizationStatus status);
    List<Organization> findAllByStatusNot(OrganizationStatus status);
}
