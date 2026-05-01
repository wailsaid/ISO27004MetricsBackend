package com.pfem2.iso27004.Entity;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "measurement_programme")
public class MeasurementProgramme {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String scope;

    @Column(length = 2000)
    private String objectives;

    @Column(length = 2000)
    private String policy;

    @Column(length = 2000)
    private String evaluationCriteria;

    private String reviewFrequency;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastReviewDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date nextReviewDate;

    @Column(columnDefinition = "varchar(50) default 'ACTIVE'")
    private String status;

    private String responsiblePerson;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date createdAt;

    // Phase 4: link programme to its indicators (§5.2 — programme scope)
    @ManyToMany
    @JoinTable(
        name = "programme_indicator",
        joinColumns = @JoinColumn(name = "programme_id"),
        inverseJoinColumns = @JoinColumn(name = "indicator_id")
    )
    @Builder.Default
    private Set<Indicator> indicators = new HashSet<>();
}
