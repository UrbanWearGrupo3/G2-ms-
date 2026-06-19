package com.grupo3.tienda_ropa.notification.service;

import com.grupo3.tienda_ropa.notification.dto.NotificationRequest;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;

@Service
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    public EmailNotificationService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Async
    public void sendEmail(NotificationRequest request) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
            );

            // Generar contenido HTML dinámico con Thymeleaf
            Context context = new Context();
            if (request.getTemplateData() != null) {
                context.setVariables(request.getTemplateData());
            }
            String htmlContent = templateEngine.process(request.getType().getTemplatePath(), context);

            helper.setTo(request.getRecipient());
            helper.setSubject(request.getSubject());
            helper.setText(htmlContent, true);
            helper.setFrom("no-reply@urbanwear.com");

            mailSender.send(message);
            System.out.println("Email enviado exitosamente a: " + request.getRecipient());
        } catch (Exception e) {
            System.err.println("Error enviando email a " + request.getRecipient() + ": " + e.getMessage());
        }
    }
}
