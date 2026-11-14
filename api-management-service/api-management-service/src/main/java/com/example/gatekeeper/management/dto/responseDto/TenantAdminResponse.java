package com.example.gatekeeper.management.dto.responseDto;

import com.example.gatekeeper.management.entity.ManagementUser;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor

public class TenantAdminResponse {

    private Long tenantId;

    private Long organizationId;

    private String role;

    private String username;

    private boolean enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static TenantAdminResponse from(ManagementUser user){
        TenantAdminResponse tenantAdminResponse = new TenantAdminResponse();

        tenantAdminResponse.setTenantId(user.getId());
        tenantAdminResponse.setOrganizationId(user.getOrganization().getId());
        tenantAdminResponse.setUsername(user.getUsername());
        tenantAdminResponse.setRole(user.getRole().getName());
        tenantAdminResponse.setEnabled(user.isEnabled());
        tenantAdminResponse.setCreatedAt(user.getCreatedAt());
        tenantAdminResponse.setUpdatedAt(user.getUpdatedAt());

        return  tenantAdminResponse;
    }

}
