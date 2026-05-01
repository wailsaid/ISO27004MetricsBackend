package com.pfem2.iso27004.Entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
@Entity
@Table
public class Evaluation {

    @Id
    @GeneratedValue
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false)
    private Double value;

    @Column(nullable = false)
    private Double performance;

    @Column(nullable = false)
    private String status;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false)
    private Date evaluationDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date nextEvaluationDate;

    @ManyToOne
    @JoinColumn(name = "indicator_id")
    private Indicator indicator;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User resp;

    // ISO 27004 RAG status (§5.4.5)
    @Enumerated(EnumType.STRING)
    private RagStatus ragStatus;

    // Ratio value used in RAG decision
    private Double indicatorRatio;

    // Trend analysis (§9.2)
    @Enumerated(EnumType.STRING)
    private TrendDirection trendDirection;

    // Verification workflow (§8.3): DRAFT → SUBMITTED → VERIFIED / REJECTED
    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20) default 'DRAFT'")
    private EvalStatus evalStatus;

    // Audit trail (§8.3b)
    private String collectedBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date collectedAt;

    @Column(length = 1000)
    private String collectionIssues;

    private String verifiedBy;

    @Temporal(TemporalType.TIMESTAMP)
    private Date verifiedAt;
}
