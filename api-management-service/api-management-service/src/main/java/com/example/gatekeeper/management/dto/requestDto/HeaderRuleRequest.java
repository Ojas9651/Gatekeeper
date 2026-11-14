package com.example.gatekeeper.management.dto.requestDto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor

public class HeaderRuleRequest {

    @NotBlank
    @Size(max = 20)
    private String direction;

    @NotBlank
    @Size(max = 20)
    private String action;

    @NotBlank
    @Size(max = 200)
    private String headerName;

    @Size(max = 2000)
    private String headerValue;
}
