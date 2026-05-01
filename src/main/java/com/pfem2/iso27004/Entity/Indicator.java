package com.pfem2.iso27004.Entity;

import java.util.Date;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "indicator")
public class Indicator {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(nullable = false, length = 1000)
    private String howtomeasure;

    @Column(nullable = false)
    private String benefit;

    @Column(nullable = false)
    private Double targetValue;

    @Column(nullable = false)
    private Double acceptableValue;

    @Column(nullable = false)
    private String frequency;

    @Column(nullable = false)
    private String valueUnit;

    @Column(nullable = false)
    private String performance;

    @Column(nullable = false)
    private String infoOwner;

    @Column(nullable = false)
    private String infoCollector;

    @Column(nullable = false)
    private String infoCustomer;

    @Column(nullable = false, columnDefinition = "boolean default false")
    private boolean checked = false;

    @ManyToMany
    @JoinTable(name = "app-indicator", joinColumns = @JoinColumn(name = "indicator_id"), inverseJoinColumns = @JoinColumn(name = "app_id"))
    private Set<App> apps;

    // ISO 27004 Measurement Construct fields (§7.5 / Annex A) — nullable for backward compatibility

    private String constructId;

    private String controlReference;

    @Column(length = 1000)
    private String controlObjective;

    @Column(length = 1000)
    private String purposeOfMeasurement;

    @Column(length = 1000)
    private String objectOfMeasurement;

    private String attribute;

    @Enumerated(EnumType.STRING)
    private MeasurementMethodType measurementMethodType;

    @Enumerated(EnumType.STRING)
    private ScaleType scaleType;

    @Column(length = 1000)
    private String baseMeasureDescription;

    @Column(length = 1000)
    private String derivedMeasureDescription;

    @Column(length = 1000)
    private String measurementFunction;

    @Column(length = 1000)
    private String analyticalModel;

    // RAG decision criteria thresholds (§5.4.5)
    private Double decisionCriteriaGreen;

    private Double decisionCriteriaAmber;

    // Separate frequencies per ISO 27004 §7.5.6
    private String collectionFrequency;

    private String analysisFrequency;

    private String reportingFrequency;

    @Temporal(TemporalType.TIMESTAMP)
    private Date measurementRevisionDate;

    private String periodOfMeasurement;

    // Stakeholder roles per ISO 27004 §7.5.8
    private String clientForMeasurement;

    private String reviewerForMeasurement;

    private String informationCommunicator;

    private String reportingFormat;

    @Column(length = 2000)
    private String indicatorInterpretation;

    private String revisionStatus;
}
