package com.pfem2.iso27004.Controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pfem2.iso27004.Entity.MeasurementProgramme;
import com.pfem2.iso27004.Service.MeasurementProgrammeService;

@RestController
@RequestMapping("api/v1/programme")
@CrossOrigin(origins = "*")
public class MeasurementProgrammeController {

    private final MeasurementProgrammeService programmeService;

    @Autowired
    public MeasurementProgrammeController(MeasurementProgrammeService programmeService) {
        this.programmeService = programmeService;
    }

    @GetMapping
    public List<MeasurementProgramme> getAll() {
        return programmeService.getAll();
    }

    @GetMapping("/{id}")
    public MeasurementProgramme getById(@PathVariable Long id) {
        return programmeService.getById(id);
    }

    @GetMapping("/active")
    public List<MeasurementProgramme> getActive() {
        return programmeService.getActive();
    }

    @GetMapping("/due-for-review")
    public List<MeasurementProgramme> getDueForReview() {
        return programmeService.getProgrammesDueForReview();
    }

    // Phase 4: compliance scorecard scoped to this programme's indicators
    @GetMapping("/{id}/scorecard")
    public Map<String, Object> getScorecard(@PathVariable Long id) {
        return programmeService.getProgrammeScorecard(id);
    }

    @PostMapping
    public MeasurementProgramme create(@RequestBody MeasurementProgramme programme) {
        return programmeService.create(programme);
    }

    @PutMapping
    public MeasurementProgramme update(@RequestBody MeasurementProgramme programme) {
        return programmeService.update(programme);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        programmeService.delete(id);
    }
}
