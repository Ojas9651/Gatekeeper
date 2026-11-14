package com.example.gatekeeper.management.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Table(name = "route_header_rules")

@Entity
public class RouteHeaderRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_pk", nullable = false)
    private APIRoute route;
    
    private String direction;
    private String action;
    private String headerName;
    private String headerValue;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
