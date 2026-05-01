package com.pfem2.iso27004.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pfem2.iso27004.Entity.Indicator;
import com.pfem2.iso27004.Entity.MeasurementProgramme;
import com.pfem2.iso27004.Repository.MeasurementProgrammeRepository;

@Service
public class MeasurementProgrammeService {

    private final MeasurementProgrammeRepository programmeRepository;
    private final EvaluationService evaluationService;

    @Autowired
    public MeasurementProgrammeService(MeasurementProgrammeRepository programmeRepository,
            EvaluationService evaluationService) {
        this.programmeRepository = programmeRepository;
        this.evaluationService = evaluationService;
    }

    public MeasurementProgramme create(MeasurementProgramme programme) {
        programme.setCreatedAt(new Date());
        if (programme.getStatus() == null) {
            programme.setStatus("ACTIVE");
        }
        return programmeRepository.save(programme);
    }

    public MeasurementProgramme update(MeasurementProgramme programme) {
        return programmeRepository.save(programme);
    }

    public List<MeasurementProgramme> getAll() {
        return programmeRepository.findAllOrderedByDate();
    }

    public MeasurementProgramme getById(Long id) {
        return programmeRepository.findById(id).orElse(null);
    }

    public void delete(Long id) {
        programmeRepository.deleteById(id);
    }

    public List<MeasurementProgramme> getActive() {
        return programmeRepository.findByStatus("ACTIVE");
    }

    // Phase 4: scorecard scoped to indicators linked to this programme
    public Map<String, Object> getProgrammeScorecard(Long programmeId) {
        MeasurementProgramme programme = programmeRepository.findById(programmeId).orElse(null);
        if (programme == null) return Map.of("error", "Programme not found");

        List<Long> indicatorIds = programme.getIndicators().stream()
                .map(Indicator::getId)
                .collect(Collectors.toList());

        return evaluationService.getScorecardForIndicators(indicatorIds);
    }

    // Phase 4: list programmes whose nextReviewDate is within the next 30 days
    public List<MeasurementProgramme> getProgrammesDueForReview() {
        Date now = new Date();
        Date thirtyDaysAhead = new Date(now.getTime() + 30L * 24 * 60 * 60 * 1000);
        return programmeRepository.findAll().stream()
                .filter(p -> p.getNextReviewDate() != null
                        && p.getNextReviewDate().after(now)
                        && p.getNextReviewDate().before(thirtyDaysAhead))
                .collect(Collectors.toList());
    }
}
