package com.pfem2.iso27004.Controller;

import java.util.List;

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

import com.pfem2.iso27004.Entity.ConstructTemplate;
import com.pfem2.iso27004.Entity.Indicator;
import com.pfem2.iso27004.Repository.ConstructTemplateRepository;
import com.pfem2.iso27004.Service.IndicatorService;

@RestController
@RequestMapping("api/v1/indicator")
@CrossOrigin(origins = "*")

public class IndicatorContoller {

    private final IndicatorService indicatorService;
    private final ConstructTemplateRepository constructTemplateRepository;

    @Autowired
    public IndicatorContoller(IndicatorService indicatorService, ConstructTemplateRepository constructTemplateRepository) {
        this.indicatorService = indicatorService;
        this.constructTemplateRepository = constructTemplateRepository;
    }

    @GetMapping(path = "/not-evaluated")
    public List<Indicator> getRIndicators() {
        return indicatorService.getRIndicators();
    }

    @GetMapping
    public List<Indicator> getIndicators() {
        return indicatorService.getIndicators();
    }

    @PutMapping
    public Indicator EditIndicator(@RequestBody Indicator indicator) {
        return indicatorService.editIndicator(indicator);
    }

    @GetMapping(path = "{indicatorID}")
    public Indicator getIndicatorByID(@PathVariable("indicatorID") Long id) {
        return indicatorService.getIndicatorByID(id);
    }

    @PostMapping
    public Indicator addIndicators(@RequestBody Indicator indicator) {
        return indicatorService.addIndicator(indicator);
    }

    @DeleteMapping(path = "{indicatorID}")
    public void deleteIndicators(@PathVariable("indicatorID") Long id) {
        indicatorService.deleteIndicator(id);
    }

    @GetMapping(path = "/library")
    public List<ConstructTemplate> getLibrary() {
        return constructTemplateRepository.findAll();
    }

    @GetMapping(path = "/library/{constructId}")
    public ConstructTemplate getLibraryItem(@PathVariable String constructId) {
        return constructTemplateRepository.findByConstructId(constructId).orElse(null);
    }

    @PostMapping(path = "/library/{constructId}/instantiate")
    public Indicator instantiateFromTemplate(@PathVariable String constructId, @RequestBody Indicator overrides) {
        ConstructTemplate template = constructTemplateRepository.findByConstructId(constructId).orElse(null);
        if (template == null) return null;

        Indicator indicator = Indicator.builder()
                .name(overrides.getName() != null ? overrides.getName() : template.getName())
                .category(template.getCategory() != null ? template.getCategory() : "")
                .type(overrides.getType() != null ? overrides.getType() : "")
                .description(template.getObjectOfMeasurement() != null ? template.getObjectOfMeasurement() : "")
                .howtomeasure(template.getMeasurementFunction() != null ? template.getMeasurementFunction() : "")
                .benefit(template.getIndicatorInterpretation() != null ? template.getIndicatorInterpretation() : "")
                .targetValue(template.getTargetValue() != null ? template.getTargetValue() : 0.0)
                .acceptableValue(template.getAcceptableValue() != null ? template.getAcceptableValue() : 0.0)
                .frequency(template.getFrequency() != null ? template.getFrequency() : "monthly")
                .valueUnit(template.getValueUnit() != null ? template.getValueUnit() : "")
                .performance(template.getPerformance() != null ? template.getPerformance() : "asc")
                .infoOwner(template.getInfoOwner() != null ? template.getInfoOwner() : "")
                .infoCollector(overrides.getInfoCollector() != null ? overrides.getInfoCollector() : "")
                .infoCustomer(overrides.getInfoCustomer() != null ? overrides.getInfoCustomer() : "")
                .constructId(template.getConstructId())
                .controlReference(template.getControlReference())
                .controlObjective(template.getControlObjective())
                .objectOfMeasurement(template.getObjectOfMeasurement())
                .attribute(template.getAttribute())
                .measurementMethodType(template.getMeasurementMethodType() != null
                        ? com.pfem2.iso27004.Entity.MeasurementMethodType.valueOf(template.getMeasurementMethodType())
                        : null)
                .scaleType(template.getScaleType() != null
                        ? com.pfem2.iso27004.Entity.ScaleType.valueOf(template.getScaleType())
                        : null)
                .baseMeasureDescription(template.getBaseMeasureDescription())
                .derivedMeasureDescription(template.getDerivedMeasureDescription())
                .measurementFunction(template.getMeasurementFunction())
                .analyticalModel(template.getAnalyticalModel())
                .indicatorInterpretation(template.getIndicatorInterpretation())
                .build();

        return indicatorService.addIndicator(indicator);
    }

}
