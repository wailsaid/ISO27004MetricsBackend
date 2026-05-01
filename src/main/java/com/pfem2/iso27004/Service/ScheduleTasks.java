package com.pfem2.iso27004.Service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import jakarta.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.pfem2.iso27004.Entity.Collector;
import com.pfem2.iso27004.Entity.Evaluation;
import com.pfem2.iso27004.Entity.Indicator;

@Configuration
@EnableScheduling
public class ScheduleTasks {

    private final JavaMailSender emailSender;
    private final UserService userService;
    private final EvaluationService evaluationService;
    private final TemplateEngine templateEngine;

    @Autowired
    public ScheduleTasks(JavaMailSender emailSender, UserService userService, EvaluationService evaluationService,
            TemplateEngine templateEngine) {
        this.emailSender = emailSender;
        this.userService = userService;
        this.evaluationService = evaluationService;
        this.templateEngine = templateEngine;
    }

    // Existing: notify collectors about indicators due next month (1st and 20th at 09:00)
    @Scheduled(cron = "0 0 9 1,20 * *")
    public void scheduleFixedDelayTask() {
        List<Collector> collectors = this.userService.getCollectors();
        for (Collector c : collectors) {
            List<Indicator> l = new ArrayList<>();
            for (Indicator i : c.getIndicator()) {
                Evaluation e = this.evaluationService.getLatestInicatorEvaluation(i.getId());
                if (e != null) {
                    Calendar nextMonth = Calendar.getInstance();
                    nextMonth.add(Calendar.MONTH, 1);
                    Calendar nextEvalDate = Calendar.getInstance();
                    nextEvalDate.setTime(e.getNextEvaluationDate());

                    if (nextEvalDate.get(Calendar.YEAR) == nextMonth.get(Calendar.YEAR)
                            && nextEvalDate.get(Calendar.MONTH) == nextMonth.get(Calendar.MONTH)) {
                        if (!l.contains(i)) {
                            l.add(i);
                        }
                    }
                }
            }
            if (!l.isEmpty()) {
                Context contxt = new Context();
                contxt.setVariable("deadline", "due for next Month");
                contxt.setVariable("indicators", l);
                String body = templateEngine.process("emailTemplate", contxt);
                sendMail(c.getCollector().getEmail(), "Indicators Due for Next Month", body);
            }
        }
    }

    // Phase 3: daily alert to admins listing any indicators currently RAG = RED
    @Scheduled(cron = "0 0 8 * * *")
    public void notifyRedRagIndicators() {
        List<Evaluation> redEvaluations = this.evaluationService.getRedRagEvaluations();
        if (redEvaluations.isEmpty()) return;

        List<String> adminEmails = this.userService.getAdminEmails();
        if (adminEmails.isEmpty()) return;

        Context ctx = new Context();
        ctx.setVariable("evaluations", redEvaluations);
        ctx.setVariable("deadline", "RED RAG status — immediate attention required");
        ctx.setVariable("indicators", redEvaluations.stream()
                .map(Evaluation::getIndicator).distinct().toList());
        String body = templateEngine.process("emailTemplate", ctx);

        for (String email : adminEmails) {
            sendMail(email, "[ALERT] Indicators with RED RAG Status", body);
        }
    }

    // Phase 2: daily digest to admins of evaluations awaiting verification
    @Scheduled(cron = "0 0 9 * * *")
    public void notifyPendingVerification() {
        List<Evaluation> pending = this.evaluationService.getPendingVerification();
        if (pending.isEmpty()) return;

        List<String> adminEmails = this.userService.getAdminEmails();
        if (adminEmails.isEmpty()) return;

        Context ctx = new Context();
        ctx.setVariable("evaluations", pending);
        ctx.setVariable("deadline", "awaiting your verification");
        ctx.setVariable("indicators", pending.stream()
                .map(Evaluation::getIndicator).distinct().toList());
        String body = templateEngine.process("emailTemplate", ctx);

        for (String email : adminEmails) {
            sendMail(email, "[ACTION REQUIRED] Evaluations Pending Verification (" + pending.size() + ")", body);
        }
    }

    public void sendMail(String to, String subject, String body) {
        try {
            MimeMessage mimeMessage = emailSender.createMimeMessage();
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true);
            messageHelper.setTo(to);
            messageHelper.setSubject(subject);

            ClassPathResource imageResource = new ClassPathResource("static/images/logo.png");
            messageHelper.addInline("logo", imageResource);

            messageHelper.setText(body, true);
            emailSender.send(mimeMessage);
        } catch (Exception e) {
            System.out.println(e);
        }
        System.out.println("email : '" + subject + "' to '" + to + "' send.");
    }
}
