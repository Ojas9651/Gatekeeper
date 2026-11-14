package com.example.gatekeeper.management.dto.requestDto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class TenantAdminRequest {

    @NotNull
    private Long organizationId;

    @NotBlank
    @Size(max = 50)
    private String username;

    @NotBlank
    @Size(min = 5, max = 30)
    private String password;

}
