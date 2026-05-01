package com.pfem2.iso27004.Service;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.pfem2.iso27004.Entity.EvalStatus;
import com.pfem2.iso27004.Entity.Evaluation;
import com.pfem2.iso27004.Entity.Indicator;
import com.pfem2.iso27004.Entity.RagStatus;
import com.pfem2.iso27004.Entity.TrendDirection;
import com.pfem2.iso27004.Repository.EvaluationRepository;
import com.pfem2.iso27004.Repository.UserRepository;
import com.pfem2.iso27004.Security.Errors.BadrequestException;

import jakarta.transaction.Transactional;

@Service
public class EvaluationService {

    private final EvaluationRepository evaluationRepository;
    private final UserRepository userRepository;

    @Autowired
    public EvaluationService(EvaluationRepository evaluationRepository, UserRepository userRepository) {
        this.evaluationRepository = evaluationRepository;
        this.userRepository = userRepository;
    }

    public Evaluation SaveEvaluation(Evaluation evaluation) {
        return this.evaluationRepository.save(evaluation);
    }

    public Evaluation getById(Long id) {
        return this.evaluationRepository.findById(id)
                .orElseThrow(() -> new BadrequestException("Evaluation with id: " + id + " does not exist"));
    }

    public Evaluation addevalution(Evaluation evaluation) {
        Indicator indicator = evaluation.getIndicator();
        String frequency = indicator.getFrequency();
        String performance = indicator.getPerformance();

        evaluation.setNextEvaluationDate(computeNextDate(evaluation.getEvaluationDate(), frequency));

        double performanceValue = 0.0;
        Evaluation previousEvaluation = this.evaluationRepository
                .findLatestEvaluationByIndicatorId(indicator.getId());

        if (previousEvaluation != null) {
            double previousValue = previousEvaluation.getValue();
            double currentValue = evaluation.getValue();
            performanceValue = performance.equals("asc")
                    ? currentValue - previousValue
                    : previousValue - currentValue;
        }
        evaluation.setPerformance(performanceValue);

        double value = evaluation.getValue();
        evaluation.setStatus(computeStatus(performance, value, indicator.getAcceptableValue(), indicator.getTargetValue()));
        evaluation.setRagStatus(computeRagStatus(indicator, value));
        evaluation.setTrendDirection(computeTrend(indicator.getId(), value, performance));

        if (evaluation.getEvalStatus() == null) {
            evaluation.setEvalStatus(EvalStatus.DRAFT);
        }
        if (evaluation.getCollectedAt() == null) {
            evaluation.setCollectedAt(new Date());
        }

        return this.evaluationRepository.save(evaluation);
    }

    // Phase 2: transition DRAFT → SUBMITTED
    public Evaluation submitEvaluation(Long id) {
        Evaluation evaluation = getById(id);
        if (evaluation.getEvalStatus() != EvalStatus.DRAFT) {
            throw new BadrequestException("Only DRAFT evaluations can be submitted");
        }
        evaluation.setEvalStatus(EvalStatus.SUBMITTED);
        return this.evaluationRepository.save(evaluation);
    }

    // Phase 2: all SUBMITTED evaluations awaiting admin review
    public List<Evaluation> getPendingVerification() {
        return this.evaluationRepository.findByEvalStatus(EvalStatus.SUBMITTED);
    }

    // Phase 5: latest evaluations past their nextEvaluationDate
    public List<Evaluation> getOverdueEvaluations() {
        return this.evaluationRepository.findOverdueLatestEvaluations();
    }

    // Phase 3: count GREEN / AMBER / RED across all latest evaluations
    public Map<String, Object> getRagSummary() {
        long green = this.evaluationRepository.findLatestEvaluationsByRagStatus(RagStatus.GREEN).size();
        long amber = this.evaluationRepository.findLatestEvaluationsByRagStatus(RagStatus.AMBER).size();
        long red = this.evaluationRepository.findLatestEvaluationsByRagStatus(RagStatus.RED).size();
        long total = green + amber + red;

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("green", green);
        summary.put("amber", amber);
        summary.put("red", red);
        summary.put("total", total);
        summary.put("greenRate", total > 0 ? Math.round((double) green / total * 100.0) : 0);
        return summary;
    }

    // Phase 3: scorecard broken down by ISO 27001 control domain (e.g. "A.7")
    public Map<String, Object> getScorecardByDomain() {
        List<Evaluation> latest = getLatestEvaluations();
        Map<String, Map<String, Long>> domainMap = new HashMap<>();

        for (Evaluation e : latest) {
            String ref = e.getIndicator().getControlReference();
            String domain = extractDomain(ref);
            domainMap.computeIfAbsent(domain, k -> new HashMap<>());
            Map<String, Long> counts = domainMap.get(domain);
            counts.merge(e.getStatus(), 1L, Long::sum);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, Long>> entry : domainMap.entrySet()) {
            Map<String, Long> counts = entry.getValue();
            long target = counts.getOrDefault("Target-achieved", 0L);
            long acceptable = counts.getOrDefault("Acceptable", 0L);
            long bad = counts.getOrDefault("Bad", 0L);
            long total = target + acceptable + bad;
            Map<String, Object> domainData = new LinkedHashMap<>();
            domainData.put("targetAchieved", target);
            domainData.put("acceptable", acceptable);
            domainData.put("bad", bad);
            domainData.put("total", total);
            domainData.put("complianceRate", total > 0 ? Math.round((double) (target + acceptable) / total * 100.0) : 0);
            result.put(entry.getKey(), domainData);
        }
        return result;
    }

    // Phase 4: scorecard scoped to a set of indicators (used by programme scorecard)
    public Map<String, Object> getScorecardForIndicators(List<Long> indicatorIds) {
        long targetAchieved = 0, acceptable = 0, bad = 0;
        for (Long id : indicatorIds) {
            Evaluation e = this.evaluationRepository.findLatestEvaluationByIndicatorId(id);
            if (e == null) continue;
            switch (e.getStatus()) {
                case "Target-achieved": targetAchieved++; break;
                case "Acceptable": acceptable++; break;
                default: bad++;
            }
        }
        long total = targetAchieved + acceptable + bad;
        Map<String, Object> scorecard = new LinkedHashMap<>();
        scorecard.put("total", total);
        scorecard.put("targetAchieved", targetAchieved);
        scorecard.put("acceptable", acceptable);
        scorecard.put("bad", bad);
        scorecard.put("complianceRate", total > 0 ? Math.round((double) (targetAchieved + acceptable) / total * 100.0) : 0);
        return scorecard;
    }

    public List<Evaluation> getLatestEvaluations() {
        return this.evaluationRepository.findLatestEvaluations();
    }

    @Transactional
    public void deleteAllEvaluationsByIndicator(Long id) {
        this.evaluationRepository.deleteAllEvaluationsByIndicatorId(id);
    }

    public List<Evaluation> getAllInicatorEvaluations(Long id) {
        return this.evaluationRepository.findAllEvaluationsByIndicatorId(id);
    }

    public Evaluation getLatestInicatorEvaluation(Long id) {
        return this.evaluationRepository.findLatestEvaluationByIndicatorId(id);
    }

    public List<Evaluation> getDashboardIndicator() {
        return this.evaluationRepository.findDashboardIndicator();
    }

    public void updateEvaluations(Indicator indicator) {
        List<Evaluation> list = this.getAllInicatorEvaluations(indicator.getId());
        String performance = indicator.getPerformance();
        double acceptableValue = indicator.getAcceptableValue();
        double targetValue = indicator.getTargetValue();

        for (Evaluation e : list) {
            if (!performance.equals(e.getIndicator().getPerformance())) {
                e.setPerformance(-e.getPerformance());
            }
            double value = e.getValue();
            e.setStatus(computeStatus(performance, value, acceptableValue, targetValue));
            e.setRagStatus(computeRagStatus(indicator, value));
            e.setNextEvaluationDate(computeNextDate(e.getEvaluationDate(), indicator.getFrequency()));
            this.evaluationRepository.save(e);
        }
    }

    public List<Evaluation> getAll() {
        return this.evaluationRepository.findAll();
    }

    public void deleteEvaluation(Evaluation evaluation) {
        this.evaluationRepository.delete(evaluation);
    }

    public void getDeleteByResp(Long userId) {
        this.evaluationRepository.deletebyResp(userId);
    }

    public List<Evaluation> getTrend(Long indicatorId) {
        return this.evaluationRepository.findAllEvaluationsByIndicatorId(indicatorId);
    }

    public List<Evaluation> getRedRagEvaluations() {
        return this.evaluationRepository.findLatestEvaluationsByRagStatus(RagStatus.RED);
    }

    // --- private helpers ---

    private String computeStatus(String performance, double value, double acceptableValue, double targetValue) {
        if ((performance.equals("asc") && value >= targetValue) ||
                (performance.equals("desc") && value <= targetValue)) {
            return "Target-achieved";
        }
        if ((performance.equals("asc") && value >= acceptableValue && value < targetValue) ||
                (performance.equals("desc") && value <= acceptableValue && value > targetValue)) {
            return "Acceptable";
        }
        return "Bad";
    }

    private Date computeNextDate(Date evaluationDate, String frequency) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(evaluationDate);
        switch (frequency) {
            case "monthly":
                cal.add(Calendar.MONTH, 1);
                break;
            case "quarterly":
                cal.add(Calendar.MONTH, 3);
                break;
            case "annually":
                cal.add(Calendar.YEAR, 1);
                break;
            default:
                throw new IllegalArgumentException("Invalid frequency: " + frequency);
        }
        return cal.getTime();
    }

    private RagStatus computeRagStatus(Indicator indicator, double value) {
        Double greenThreshold = indicator.getDecisionCriteriaGreen();
        Double amberThreshold = indicator.getDecisionCriteriaAmber();
        if (greenThreshold == null || amberThreshold == null) {
            return null;
        }
        String performance = indicator.getPerformance();
        if (performance.equals("asc")) {
            if (value >= greenThreshold) return RagStatus.GREEN;
            if (value >= amberThreshold) return RagStatus.AMBER;
            return RagStatus.RED;
        } else {
            if (value <= greenThreshold) return RagStatus.GREEN;
            if (value <= amberThreshold) return RagStatus.AMBER;
            return RagStatus.RED;
        }
    }

    private TrendDirection computeTrend(Long indicatorId, double currentValue, String performance) {
        List<Evaluation> history = this.evaluationRepository.findAllEvaluationsByIndicatorId(indicatorId);
        if (history.isEmpty()) return TrendDirection.STABLE;

        int lookback = Math.min(3, history.size());
        double oldest = history.get(lookback - 1).getValue();
        double delta = currentValue - oldest;

        if (Math.abs(delta) < 0.001) return TrendDirection.STABLE;
        if (performance.equals("asc")) {
            return delta > 0 ? TrendDirection.UPWARD : TrendDirection.DOWNWARD;
        } else {
            return delta < 0 ? TrendDirection.UPWARD : TrendDirection.DOWNWARD;
        }
    }

    private String extractDomain(String controlReference) {
        if (controlReference == null || controlReference.isBlank()) return "Uncategorised";
        String[] parts = controlReference.split("\\.");
        return parts.length >= 2 ? parts[0] + "." + parts[1] : controlReference;
    }
}
