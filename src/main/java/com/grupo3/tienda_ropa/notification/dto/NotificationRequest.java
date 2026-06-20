package com.grupo3.tienda_ropa.notification.dto;

import com.grupo3.tienda_ropa.notification.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
    private String recipient;
    private String subject;
    private NotificationType type;
    private Map<String, Object> templateData;
}
