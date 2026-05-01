package com.pfem2.iso27004.Service;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.pfem2.iso27004.Entity.Evaluation;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class PDFService {
    private final EvaluationService evaluationservice;
    private final TemplateEngine templateEngine;

    @Autowired
    public PDFService(EvaluationService evaluationservice, TemplateEngine templateEngine) {
        this.evaluationservice = evaluationservice;
        this.templateEngine = templateEngine;
    }

    public void generateRepport(HttpServletResponse response) {
        try {
            response.setContentType("application/pdf");

            OutputStream outputStream = response.getOutputStream();
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDocument = new PdfDocument(writer);

            String templateName = "indicatorTemplate";
            Context ct = new Context();

            List<Evaluation> evaluations = this.evaluationservice.getDashboardIndicator();
            Map<Evaluation, List<Evaluation>> data = new HashMap<>();

            for (Evaluation evaluation : evaluations) {
                pdfDocument.addNewPage();
                data.put(evaluation,
                        this.evaluationservice.getAllInicatorEvaluations(evaluation.getIndicator().getId()));
            }

            // Phase 3: pass RAG summary and scorecard to the PDF template
            ct.setVariable("evaluations", data);
            ct.setVariable("ragSummary", this.evaluationservice.getRagSummary());
            ct.setVariable("scorecard", buildScorecard(evaluations));
            ct.setVariable("generatedAt", new java.util.Date());

            String renderedHtml = templateEngine.process(templateName, ct);

            ConverterProperties converterProperties = new ConverterProperties();
            HtmlConverter.convertToPdf(renderedHtml, pdfDocument, converterProperties);

            pdfDocument.close();
            outputStream.flush();
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> buildScorecard(List<Evaluation> evaluations) {
        long total = evaluations.size();
        long target = evaluations.stream().filter(e -> "Target-achieved".equals(e.getStatus())).count();
        long acceptable = evaluations.stream().filter(e -> "Acceptable".equals(e.getStatus())).count();
        long bad = evaluations.stream().filter(e -> "Bad".equals(e.getStatus())).count();

        Map<String, Object> scorecard = new HashMap<>();
        scorecard.put("total", total);
        scorecard.put("targetAchieved", target);
        scorecard.put("acceptable", acceptable);
        scorecard.put("bad", bad);
        scorecard.put("complianceRate", total > 0 ? Math.round((double) (target + acceptable) / total * 100.0) : 0);
        return scorecard;
    }
}
