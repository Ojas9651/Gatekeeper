package com.example.gatekeeper.management.dto.responseDto;

import com.example.gatekeeper.management.entity.RouteHeaderRule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class HeaderRuleResponse {

    private Long id;
    private String direction;
    private String action;
    private String headerName;
    private String headerValue;

    public static  HeaderRuleResponse from(RouteHeaderRule rule){
        return new HeaderRuleResponse(
                rule.getId(), rule.getDirection(), rule.getAction(),
                rule.getHeaderName(), rule.getHeaderValue()
        );
    }
}
