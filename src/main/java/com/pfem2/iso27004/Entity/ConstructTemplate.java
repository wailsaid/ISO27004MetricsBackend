package com.pfem2.iso27004.Entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "construct_template")
public class ConstructTemplate {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String constructId;

    @Column(nullable = false)
    private String name;

    private String controlReference;

    @Column(length = 1000)
    private String controlObjective;

    @Column(length = 1000)
    private String objectOfMeasurement;

    private String attribute;

    private String measurementMethodType;

    private String scaleType;

    @Column(length = 1000)
    private String baseMeasureDescription;

    @Column(length = 1000)
    private String derivedMeasureDescription;

    @Column(length = 1000)
    private String measurementFunction;

    @Column(length = 1000)
    private String analyticalModel;

    private Double targetValue;

    private Double acceptableValue;

    private String valueUnit;

    private String performance;

    private String frequency;

    @Column(length = 1000)
    private String indicatorInterpretation;

    private String infoOwner;

    private String category;
}
