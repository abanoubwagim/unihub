package com.unihub.shared.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "countries")
@Getter
@NoArgsConstructor
public class Country {
    
    @Id
    private Integer id;
    private String name;
    private String code;  // "EG", etc.
    
}
