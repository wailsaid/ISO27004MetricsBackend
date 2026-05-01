package com.pfem2.iso27004.Controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pfem2.iso27004.Entity.EvalStatus;
import com.pfem2.iso27004.Entity.Evaluation;
import com.pfem2.iso27004.Service.EvaluationService;

@RestController
@RequestMapping("api/v1/evaluation")
@CrossOrigin(origins = "*")
public class EvaluationController {

    private final EvaluationService evaluationService;

    @Autowired
    public EvaluationController(EvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @GetMapping(path = "/dashboard")
    public List<Evaluation> getDashboard() {
        return evaluationService.getDashboardIndicator();
    }

    @GetMapping(path = "/all/{indicatorID}")
    public List<Evaluation> getAllInicatorEvaluation(@PathVariable("indicatorID") Long id) {
        return this.evaluationService.getAllInicatorEvaluations(id);
    }

    @GetMapping
    public List<Evaluation> getLatestEvaluation() {
        return this.evaluationService.getLatestEvaluations();
    }

    @GetMapping(path = "/{indicatorID}")
    public Evaluation getLatestInicatorEvaluation(@PathVariable("indicatorID") Long id) {
        return this.evaluationService.getLatestInicatorEvaluation(id);
    }

    @PostMapping
    public Evaluation evaluate(@RequestBody Evaluation evaluation) {
        return evaluationService.addevalution(evaluation);
    }

    // Phase 1: trend history for charting
    @GetMapping(path = "/trend/{indicatorID}")
    public List<Evaluation> getTrend(@PathVariable("indicatorID") Long id) {
        return this.evaluationService.getTrend(id);
    }

    // Phase 1 + 3: overall compliance scorecard with domain breakdown
    @GetMapping(path = "/scorecard")
    public Map<String, Object> getScorecard() {
        List<Evaluation> latest = this.evaluationService.getLatestEvaluations();
        long total = latest.size();
        long target = latest.stream().filter(e -> "Target-achieved".equals(e.getStatus())).count();
        long acceptable = latest.stream().filter(e -> "Acceptable".equals(e.getStatus())).count();
        long bad = latest.stream().filter(e -> "Bad".equals(e.getStatus())).count();

        Map<String, Object> scorecard = new HashMap<>();
        scorecard.put("total", total);
        scorecard.put("targetAchieved", target);
        scorecard.put("acceptable", acceptable);
        scorecard.put("bad", bad);
        scorecard.put("complianceRate", total > 0 ? Math.round((double) (target + acceptable) / total * 100.0) : 0);
        scorecard.put("byDomain", this.evaluationService.getScorecardByDomain());
        return scorecard;
    }

    // Phase 3: RAG status summary (GREEN/AMBER/RED counts)
    @GetMapping(path = "/rag-summary")
    public Map<String, Object> getRagSummary() {
        return this.evaluationService.getRagSummary();
    }

    // Phase 5: indicators past their nextEvaluationDate
    @GetMapping(path = "/overdue")
    public List<Evaluation> getOverdue() {
        return this.evaluationService.getOverdueEvaluations();
    }

    // Phase 2: SUBMITTED evaluations awaiting admin verification
    @GetMapping(path = "/pending-verification")
    public List<Evaluation> getPendingVerification() {
        return this.evaluationService.getPendingVerification();
    }

    // Phase 2: collector submits evaluation (DRAFT → SUBMITTED)
    @PutMapping(path = "/{id}/submit")
    public Evaluation submitEvaluation(@PathVariable Long id) {
        return this.evaluationService.submitEvaluation(id);
    }

    // Phase 1: admin verifies or rejects a submitted evaluation
    @PutMapping(path = "/verify/{id}")
    public Evaluation verifyEvaluation(
            @PathVariable Long id,
            @RequestParam String action,
            @RequestParam String verifiedBy) {
        Evaluation evaluation = this.evaluationService.getById(id);
        if ("approve".equalsIgnoreCase(action)) {
            evaluation.setEvalStatus(EvalStatus.VERIFIED);
        } else {
            evaluation.setEvalStatus(EvalStatus.REJECTED);
        }
        evaluation.setVerifiedBy(verifiedBy);
        evaluation.setVerifiedAt(new Date());
        return this.evaluationService.SaveEvaluation(evaluation);
    }
}
