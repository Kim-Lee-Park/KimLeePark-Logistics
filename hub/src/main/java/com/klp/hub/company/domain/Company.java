package com.klp.hub.company.domain;

import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "p_companies")
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;
}
