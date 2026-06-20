package com.grupo3.tienda_ropa.notification.service;

import com.grupo3.tienda_ropa.notification.dto.NotificationRequest;
import com.grupo3.tienda_ropa.notification.model.NotificationType;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailNotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private SpringTemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    private EmailNotificationService emailNotificationService;

    @BeforeEach
    void setUp() {
        emailNotificationService = new EmailNotificationService(mailSender, templateEngine);
    }

    @Test
    void testSendEmail_Success() {
        // Arrange
        NotificationRequest request = NotificationRequest.builder()
                .recipient("test@ejemplo.com")
                .subject("Asunto Test")
                .type(NotificationType.ORDER_CONFIRMATION)
                .templateData(Map.of("clientName", "Juan Pérez"))
                .build();

        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq(NotificationType.ORDER_CONFIRMATION.getTemplatePath()), any(Context.class)))
                .thenReturn("<html>HTML Content</html>");

        // Act
        emailNotificationService.sendEmail(request);

        // Assert
        verify(mailSender, times(1)).createMimeMessage();
        verify(templateEngine, times(1)).process(eq(NotificationType.ORDER_CONFIRMATION.getTemplatePath()), any(Context.class));
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    void testSendEmail_Exception_ShouldHandleAndNotThrow() {
        // Arrange
        NotificationRequest request = NotificationRequest.builder()
                .recipient("test@ejemplo.com")
                .subject("Asunto Test")
                .type(NotificationType.ORDER_CONFIRMATION)
                .build();

        when(mailSender.createMimeMessage()).thenThrow(new RuntimeException("Mail server down"));

        // Act & Assert
        // No debería lanzar excepción porque está envuelto en un try-catch dentro de sendEmail
        emailNotificationService.sendEmail(request);

        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, never()).send(any(MimeMessage.class));
    }
}
