package com.showtime.demo;

import jakarta.persistence.*;

/**
 * INHERITANCE MAPPING DEMO (separate from the booking flow).
 * Strategy JOINED: one table for the parent (staff) and one per child (managers, ushers).
 * Other strategies: SINGLE_TABLE (one table, discriminator column) and TABLE_PER_CLASS.
 */
@Entity
@Table(name = "staff")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Staff {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
