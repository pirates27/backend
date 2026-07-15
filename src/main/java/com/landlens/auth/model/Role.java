package com.landlens.auth.model;

import com.landlens.common.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role extends BaseAuditEntity {

    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;

    @Column(name = "description")
    private String description;

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
    }
}
