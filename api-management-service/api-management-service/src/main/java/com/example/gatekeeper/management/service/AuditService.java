package com.example.gatekeeper.management.service;

import com.example.gatekeeper.management.entity.ControlPlaneAuditRecord;
import com.example.gatekeeper.management.entity.Organization;
import com.example.gatekeeper.management.repository.ControlPlaneAuditRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Service
public class AuditService {

    private final ControlPlaneAuditRepository auditRepository;

    public AuditService(ControlPlaneAuditRepository auditRepository) {
        this.auditRepository = auditRepository;
    }

    public void record(Organization organization, String action, String resourceType,
                       String resourceId, long startedAt, String details){

            Authentication authentication =
                    SecurityContextHolder.getContext().getAuthentication();

            String actor = authentication == null ? "system" : authentication.getName();

        ControlPlaneAuditRecord record = new ControlPlaneAuditRecord();

        record.setAction(action);
        record.setActor(actor);
        record.setDetails(details);
        record.setOrganization(organization);
        record.setResourceId(resourceId);
        record.setResourceType(resourceType);
        record.setCorrelationId(getCorrelationId());
        record.setDurationMs((System.nanoTime() -  startedAt) / 1000000);
        record.setResult("SUCCESS");
        record.setCreatedAt(LocalDateTime.now());
        auditRepository.save(record);
    }

    public String getCorrelationId(){
        if(!(RequestContextHolder.getRequestAttributes()
                        instanceof ServletRequestAttributes)) return null;

        HttpServletRequest request = (
                (ServletRequestAttributes)
                        RequestContextHolder.getRequestAttributes()).getRequest();
        return request.getHeader("X-Correlation-ID");
    }
}
