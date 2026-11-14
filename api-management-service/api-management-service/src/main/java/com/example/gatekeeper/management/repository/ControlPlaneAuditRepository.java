package com.example.gatekeeper.management.repository;

import com.example.gatekeeper.management.entity.ControlPlaneAuditRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ControlPlaneAuditRepository extends JpaRepository<ControlPlaneAuditRecord, Long> {

    List<ControlPlaneAuditRecord> findAllByOrganizationIdOrderByCreatedAtDesc(Long id);

}
