# ISO/IEC 27004:2009 Backend Compliance Plan
## Spring Boot 3 / Java — Gap Analysis & Implementation Roadmap

---

## 1. Executive Summary

This document details the backend (Spring Boot 3, Java, MySQL) changes required to make the ISO27004MetricsBackend application fully compliant with ISO/IEC 27004:2009 *Information security management — Measurement*. It mirrors the structure of the frontend Angular compliance plan and covers gap analysis, data model extensions, new service/controller modules, API endpoint changes, security configuration updates, and a phased implementation priority.

---

## 2. ISO 27004 Standard — Key Requirements vs Current Backend State

### 2.1 Gap Analysis Table

| ISO 27004 Clause | Requirement | Current State | Gap |
|---|---|---|---|
| §5.2 — Measurement Programme | Defined programme with scope, objectives, policy, review schedule | Not implemented | **Missing** — No `MeasurementProgramme` entity or CRUD |
| §5.4.5 — Decision Criteria | GREEN/AMBER/RED thresholds per indicator | Binary "Target-achieved"/"Acceptable"/"Bad" as String | **Partial** — No RAG enum; no `decisionCriteriaGreen`/`decisionCriteriaAmber` fields |
| §7.5 — Measurement Construct | 25+ standardised construct fields per indicator | ~15 basic fields; no constructId, controlReference, analyticalModel, etc. | **Major gap** — ~15 fields missing from `Indicator` entity |
| §7.5.6 — Separate Frequencies | Collection, analysis, reporting frequencies are distinct | Single `frequency` field | **Missing** — `collectionFrequency`, `analysisFrequency`, `reportingFrequency` |
| §7.5.8 — Stakeholder Roles | 5 roles: info owner, collector, customer, reviewer, communicator | 3 fields: `infoOwner`, `infoCollector`, `infoCustomer` | **Partial** — `reviewerForMeasurement`, `informationCommunicator` missing |
| §8.3 — Data Collection & Verification | Audit trail: collectedBy, collectedAt, verifiedBy, verifiedAt; verification workflow | No audit trail fields; no verification workflow | **Missing** — All audit trail fields missing from `Evaluation` entity |
| §8.3b — Verification Workflow | DRAFT → SUBMITTED → VERIFIED / REJECTED lifecycle | No status lifecycle; evaluation immediately final | **Missing** — No `EvalStatus` enum or workflow |
| §9.2 — Trend Analysis | Trend direction computed from historical values | `performance` delta stored; no formal trend direction | **Partial** — No `TrendDirection` enum or trend endpoint |
| §10 — Programme Evaluation | Evaluate and improve the measurement programme itself | No programme evaluation concept | **Missing** |
| Annex A — Construct Template | Standardised 25-field measurement construct template | No template library | **Missing** — No `ConstructTemplate` entity or seeding |
| Annex B — 14 Example Constructs | Pre-built reference constructs (B.1.1 – B.10) | No seeded reference data | **Missing** — No `DataInitializer` |
| §5.4.5 / §7.5.7 — RAG Status | GREEN/AMBER/RED per evaluation result | No RAG status on evaluations | **Missing** — No `RagStatus` field on `Evaluation` |

---

## 3. Data Model Extensions

### 3.1 `Indicator` Entity — New Fields (ISO 27004 §7.5 / Annex A)

All new fields are **nullable** to maintain backward compatibility with existing data.

```java
// Measurement Construct identity
String constructId;           // e.g. "B.1.1" — links to ConstructTemplate
String controlReference;      // ISO 27001 control, e.g. "A.7.2.2"
String controlObjective;      // ISO 27001 control objective

// Measurement scope
String purposeOfMeasurement;
String objectOfMeasurement;
String attribute;             // specific property being measured

// Measurement method (§7.5.4)
MeasurementMethodType measurementMethodType;  // OBJECTIVE | SUBJECTIVE
ScaleType scaleType;                          // NOMINAL | ORDINAL | INTERVAL | RATIO

// Measurement descriptions (§7.5.5)
String baseMeasureDescription;
String derivedMeasureDescription;
String measurementFunction;   // formula converting base → derived
String analyticalModel;       // model interpreting derived measure

// RAG decision criteria (§5.4.5)
Double decisionCriteriaGreen;  // >= GREEN threshold
Double decisionCriteriaAmber;  // >= AMBER threshold (< GREEN)

// Separate frequencies (§7.5.6)
String collectionFrequency;    // how often data is collected
String analysisFrequency;      // how often results are analysed
String reportingFrequency;     // how often results are reported

// Temporal (§7.5.6)
Date measurementRevisionDate;
String periodOfMeasurement;

// Stakeholder roles (§7.5.8)
String clientForMeasurement;       // who commissions the measurement
String reviewerForMeasurement;     // who reviews results
String informationCommunicator;    // who disseminates results
String reportingFormat;

// Interpretation
String indicatorInterpretation;    // how to interpret the indicator result
String revisionStatus;
```

### 3.2 `Evaluation` Entity — New Fields

```java
// RAG status (§5.4.5)
RagStatus ragStatus;          // GREEN | AMBER | RED (null if no RAG thresholds set)
Double indicatorRatio;        // computed ratio used in RAG decision

// Trend analysis (§9.2)
TrendDirection trendDirection;  // UPWARD | STABLE | DOWNWARD

// Verification workflow (§8.3)
EvalStatus evalStatus;        // DRAFT | SUBMITTED | VERIFIED | REJECTED

// Audit trail (§8.3b)
String collectedBy;
Date collectedAt;
String collectionIssues;
String verifiedBy;
Date verifiedAt;
```

### 3.3 New Entity — `MeasurementProgramme` (§5.2)

```java
@Entity @Table(name = "measurement_programme")
public class MeasurementProgramme {
    Long id;
    String name;              // nullable = false
    String scope;             // organisational scope
    String objectives;        // what the programme aims to achieve
    String policy;            // IS measurement policy reference
    String evaluationCriteria;
    String reviewFrequency;   // monthly | quarterly | annually
    Date lastReviewDate;
    Date nextReviewDate;
    String status;            // ACTIVE | UNDER_REVIEW | INACTIVE
    String responsiblePerson;
    Date createdAt;           // nullable = false, set on create
}
```

### 3.4 New Entity — `ConstructTemplate` (Annex A/B)

```java
@Entity @Table(name = "construct_template")
public class ConstructTemplate {
    Long id;
    String constructId;           // unique, e.g. "B.1.1"
    String name;
    String controlReference;
    String controlObjective;
    String objectOfMeasurement;
    String attribute;
    String measurementMethodType; // stored as String for flexibility
    String scaleType;
    String baseMeasureDescription;
    String derivedMeasureDescription;
    String measurementFunction;
    String analyticalModel;
    Double targetValue;
    Double acceptableValue;
    String valueUnit;
    String performance;           // asc | desc
    String frequency;
    String indicatorInterpretation;
    String infoOwner;
    String category;
}
```

### 3.5 New Enums

| Enum | Values | ISO 27004 Reference |
|---|---|---|
| `RagStatus` | GREEN, AMBER, RED | §5.4.5 — Decision criteria |
| `EvalStatus` | DRAFT, SUBMITTED, VERIFIED, REJECTED | §8.3 — Verification workflow |
| `TrendDirection` | UPWARD, STABLE, DOWNWARD | §9.2 — Trend analysis |
| `MeasurementMethodType` | OBJECTIVE, SUBJECTIVE | §7.5.4 — Measurement method |
| `ScaleType` | NOMINAL, ORDINAL, INTERVAL, RATIO | §7.5.4 — Measurement scale |

---

## 4. Service Layer Changes

### 4.1 `EvaluationService` Refactoring

**Problem**: Status computation logic and frequency switch were duplicated in `addevalution()` and `updateEvaluations()`.

**Fix**: Extract to private helpers:

```java
// Eliminates duplication — used by both addevalution() and updateEvaluations()
private String computeStatus(String performance, double value, double acceptable, double target)
private Date computeNextDate(Date evaluationDate, String frequency)

// New ISO 27004 logic
private RagStatus computeRagStatus(Indicator indicator, double value)
    // Returns null if decisionCriteriaGreen / decisionCriteriaAmber not set
    // Uses performance direction (asc/desc) for threshold comparison

private TrendDirection computeTrend(Long indicatorId, double currentValue, String performance)
    // Looks back up to 3 prior evaluations
    // UPWARD = improving in performance direction, DOWNWARD = degrading
```

**New public method**:
```java
public List<Evaluation> getTrend(Long indicatorId)
    // Returns all evaluations ordered DESC for charting
```

### 4.2 `MeasurementProgrammeService` (new)

```java
MeasurementProgramme create(MeasurementProgramme)   // sets createdAt, defaults status to ACTIVE
MeasurementProgramme update(MeasurementProgramme)
List<MeasurementProgramme> getAll()
MeasurementProgramme getById(Long id)
void delete(Long id)
List<MeasurementProgramme> getActive()
```

### 4.3 `DataInitializer` (new — CommandLineRunner)

Seeds the 14 Annex B construct templates on application startup. Uses `existsByConstructId()` to avoid re-seeding on restart.

**Templates seeded**: B.1.1, B.1.2, B.1.3, B.2.1, B.2.2, B.3, B.4.1, B.4.2, B.5, B.6, B.7, B.8, B.9, B.10

---

## 5. API Endpoint Changes

### 5.1 New Endpoints — Evaluation

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/evaluation/trend/{indicatorId}` | ADMIN, USER, COLLECTOR | Returns full evaluation history for trend charting |
| GET | `/api/v1/evaluation/scorecard` | ADMIN, USER, COLLECTOR | Returns aggregate counts: total, targetAchieved, acceptable, bad, complianceRate % |
| PUT | `/api/v1/evaluation/verify/{id}?action=approve&verifiedBy=name` | ADMIN | Sets evalStatus to VERIFIED or REJECTED, records verifiedBy + verifiedAt |

### 5.2 New Endpoints — Indicator Library (Annex B)

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/indicator/library` | ADMIN, USER, COLLECTOR | Returns all 14 construct templates |
| GET | `/api/v1/indicator/library/{constructId}` | ADMIN, USER, COLLECTOR | Returns single template by ID (e.g. "B.1.1") |
| POST | `/api/v1/indicator/library/{constructId}/instantiate` | ADMIN | Creates a new Indicator pre-populated from the template; body provides overrides (name, infoCollector, infoCustomer, type) |

### 5.3 New Endpoints — Measurement Programme

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/v1/programme` | ADMIN, USER, COLLECTOR | List all programmes |
| GET | `/api/v1/programme/{id}` | ADMIN, USER, COLLECTOR | Get programme by ID |
| GET | `/api/v1/programme/active` | ADMIN, USER, COLLECTOR | List ACTIVE programmes |
| POST | `/api/v1/programme` | ADMIN | Create programme |
| PUT | `/api/v1/programme` | ADMIN | Update programme |
| DELETE | `/api/v1/programme/{id}` | ADMIN | Delete programme |

### 5.4 Existing Endpoints — Behavioural Changes

| Endpoint | Change |
|---|---|
| POST `/api/v1/evaluation` | Now also computes `ragStatus`, `trendDirection`, sets `evalStatus=DRAFT`, sets `collectedAt` |
| PUT `/api/v1/indicator` | `updateEvaluations()` now also recomputes `ragStatus` for all evaluations |

---

## 6. Security Configuration Changes

New rules added to `SecurityConfig.java`:

```java
// Measurement Programme — read: all roles, write: ADMIN only
auth.requestMatchers(HttpMethod.GET, "/api/v1/programme/**").hasAnyAuthority("ADMIN","USER","COLLECTOR");
auth.requestMatchers(HttpMethod.POST, "/api/v1/programme/**").hasAuthority("ADMIN");
auth.requestMatchers(HttpMethod.PUT, "/api/v1/programme/**").hasAuthority("ADMIN");
auth.requestMatchers(HttpMethod.DELETE, "/api/v1/programme/**").hasAuthority("ADMIN");

// Indicator library — read: all roles, instantiate: ADMIN only
auth.requestMatchers(HttpMethod.GET, "/api/v1/indicator/library/**").hasAnyAuthority("ADMIN","USER","COLLECTOR");
auth.requestMatchers(HttpMethod.POST, "/api/v1/indicator/library/**").hasAuthority("ADMIN");

// New evaluation endpoints
auth.requestMatchers(HttpMethod.GET, "/api/v1/evaluation/trend/**").hasAnyAuthority("ADMIN","USER","COLLECTOR");
auth.requestMatchers(HttpMethod.GET, "/api/v1/evaluation/scorecard").hasAnyAuthority("ADMIN","USER","COLLECTOR");
auth.requestMatchers(HttpMethod.PUT, "/api/v1/evaluation/verify/**").hasAuthority("ADMIN");
```

---

## 7. New Repository Interfaces

| Repository | Entity | Custom Methods |
|---|---|---|
| `MeasurementProgrammeRepository` | `MeasurementProgramme` | `findByStatus(String)`, `findAllOrderedByDate()` |
| `ConstructTemplateRepository` | `ConstructTemplate` | `findByConstructId(String)`, `findByCategory(String)`, `existsByConstructId(String)` |

---

## 8. ISO 27004 Terminology Mapping

| ISO 27004 Term | Backend Implementation |
|---|---|
| Measurement construct | `Indicator` entity (extended) + `ConstructTemplate` entity |
| Base measure | `Indicator.baseMeasureDescription` + raw `Evaluation.value` |
| Derived measure | `Indicator.derivedMeasureDescription` + `Evaluation.indicatorRatio` |
| Measurement function | `Indicator.measurementFunction` |
| Analytical model | `Indicator.analyticalModel` |
| Decision criteria | `Indicator.decisionCriteriaGreen`, `decisionCriteriaAmber` → `Evaluation.ragStatus` |
| Indicator | `Evaluation.status` + `Evaluation.ragStatus` |
| Trend | `Evaluation.trendDirection` (UPWARD/STABLE/DOWNWARD) |
| Information need | `Indicator.purposeOfMeasurement` |
| Object of measurement | `Indicator.objectOfMeasurement` |
| Attribute | `Indicator.attribute` |
| Measurement programme | `MeasurementProgramme` entity |
| Data collection | `Evaluation.collectedBy`, `collectedAt`, `collectionIssues` |
| Verification | `Evaluation.verifiedBy`, `verifiedAt`, `evalStatus` |
| Communicating results | `Evaluation.evalStatus = VERIFIED` → available to consumers |
| Performance direction | `Indicator.performance` ("asc" = higher is better, "desc" = lower is better) |

---

## 9. Implementation Priority

### Phase 1 — Foundation (DONE in this session)
- [x] 5 enum classes: `RagStatus`, `EvalStatus`, `TrendDirection`, `MeasurementMethodType`, `ScaleType`
- [x] Extend `Indicator.java` with ~20 nullable ISO 27004 construct fields
- [x] Extend `Evaluation.java` with RAG status, trend, audit trail, verification workflow
- [x] Refactor `EvaluationService.java`: extract `computeStatus()`, `computeNextDate()`, add `computeRagStatus()`, `computeTrend()`
- [x] `MeasurementProgramme` entity + `MeasurementProgrammeRepository` + `MeasurementProgrammeService` + `MeasurementProgrammeController`
- [x] `ConstructTemplate` entity + `ConstructTemplateRepository`
- [x] `DataInitializer` seeding all 14 Annex B construct templates
- [x] New `EvaluationController` endpoints: trend, scorecard, verify
- [x] New `IndicatorController` endpoints: library, instantiate
- [x] Updated `SecurityConfig` with all new route permissions

### Phase 2 — Data Quality (Next)
- [ ] Add `@Valid` + `@NotBlank` / `@NotNull` validation on new required fields in request bodies
- [ ] Add `GlobalExceptionHandler` for `MethodArgumentNotValidException` returning structured error responses
- [ ] Add `PUT /api/v1/evaluation/{id}/submit` endpoint (transitions `evalStatus` DRAFT → SUBMITTED)
- [ ] Add `GET /api/v1/evaluation/pending-verification` (returns SUBMITTED evaluations, ADMIN only)
- [ ] Extend `EvaluationRepository` with queries filtering by `evalStatus` and `ragStatus`
- [ ] Add `@PreAuthorize` annotations for finer method-level security

### Phase 3 — Reporting & Analytics
- [ ] Extend scorecard endpoint to break down compliance % by ISO 27001 control domain (group by `controlReference` prefix)
- [ ] Add RAG summary endpoint: count GREEN/AMBER/RED across all latest evaluations
- [ ] Integrate RAG status into existing PDF report (`PDFService.java` / `generateReport.java`)
- [ ] Add trend chart data endpoint: last N evaluations per indicator with value + ragStatus
- [ ] Email notifications when evaluation transitions to VERIFIED or when RAG drops to RED (`ScheduleTasks.java`)

### Phase 4 — Programme Management
- [ ] Link `MeasurementProgramme` to `Indicator` via a join table (`programme_indicator`)
- [ ] Add programme review workflow: trigger review notification when `nextReviewDate` approaches
- [ ] Add `GET /api/v1/programme/{id}/scorecard` — scorecard scoped to indicators in a programme
- [ ] Add programme export endpoint (PDF or CSV summary)

### Phase 5 — Advanced Compliance
- [ ] Implement `indicatorRatio` computation (derived measure formula evaluation — requires expression engine or pre-defined formulas)
- [ ] Add measurement result archival (retain history beyond current evaluation set)
- [ ] Implement `collectionIssues` reporting workflow: flagged issues require ADMIN acknowledgment
- [ ] Add `GET /api/v1/evaluation/overdue` — indicators past their `nextEvaluationDate`
- [ ] Add compliance dashboard data endpoint aggregated by ISO 27001 Annex A domain

---

## 10. Database Migration Notes

Hibernate `spring.jpa.hibernate.ddl-auto` handles schema updates automatically. However, for **production** deployments use a migration tool:

```
-- New columns on indicator table (all nullable — no data migration needed)
ALTER TABLE indicator ADD COLUMN construct_id VARCHAR(255);
ALTER TABLE indicator ADD COLUMN control_reference VARCHAR(255);
ALTER TABLE indicator ADD COLUMN decision_criteria_green DOUBLE;
ALTER TABLE indicator ADD COLUMN decision_criteria_amber DOUBLE;
ALTER TABLE indicator ADD COLUMN measurement_method_type VARCHAR(50);
ALTER TABLE indicator ADD COLUMN scale_type VARCHAR(50);
-- ... (remaining fields from §3.1 above)

-- New columns on evaluation table (all nullable — no data migration needed)
ALTER TABLE evaluation ADD COLUMN rag_status VARCHAR(20);
ALTER TABLE evaluation ADD COLUMN eval_status VARCHAR(20) DEFAULT 'DRAFT';
ALTER TABLE evaluation ADD COLUMN trend_direction VARCHAR(20);
ALTER TABLE evaluation ADD COLUMN collected_by VARCHAR(255);
ALTER TABLE evaluation ADD COLUMN collected_at DATETIME;
ALTER TABLE evaluation ADD COLUMN collection_issues VARCHAR(1000);
ALTER TABLE evaluation ADD COLUMN verified_by VARCHAR(255);
ALTER TABLE evaluation ADD COLUMN verified_at DATETIME;

-- New tables
CREATE TABLE measurement_programme (...);
CREATE TABLE construct_template (...);
```

---

## 11. File Summary — Changes Made

| File | Action | Description |
|---|---|---|
| `Entity/RagStatus.java` | Created | GREEN/AMBER/RED enum |
| `Entity/EvalStatus.java` | Created | DRAFT/SUBMITTED/VERIFIED/REJECTED enum |
| `Entity/TrendDirection.java` | Created | UPWARD/STABLE/DOWNWARD enum |
| `Entity/MeasurementMethodType.java` | Created | OBJECTIVE/SUBJECTIVE enum |
| `Entity/ScaleType.java` | Created | NOMINAL/ORDINAL/INTERVAL/RATIO enum |
| `Entity/Indicator.java` | Extended | +20 ISO 27004 construct fields, all nullable |
| `Entity/Evaluation.java` | Extended | +ragStatus, trendDirection, evalStatus, audit trail |
| `Entity/MeasurementProgramme.java` | Created | ISO 27004 §5.2 programme entity |
| `Entity/ConstructTemplate.java` | Created | Annex A/B template entity |
| `Repository/MeasurementProgrammeRepository.java` | Created | JPA repo with status/date queries |
| `Repository/ConstructTemplateRepository.java` | Created | JPA repo with constructId lookup |
| `Service/EvaluationService.java` | Refactored | Extracted helpers; added RAG + trend computation |
| `Service/MeasurementProgrammeService.java` | Created | CRUD + active filter |
| `Service/DataInitializer.java` | Created | Seeds all 14 Annex B construct templates |
| `Controller/EvaluationController.java` | Extended | +trend, scorecard, verify endpoints |
| `Controller/IndicatorContoller.java` | Extended | +library, instantiate endpoints |
| `Controller/MeasurementProgrammeController.java` | Created | Full CRUD REST controller |
| `Config/SecurityConfig.java` | Extended | +rules for programme, library, trend, scorecard, verify |
