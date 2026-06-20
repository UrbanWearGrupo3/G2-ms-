package com.grupo3.tienda_ropa.notification.model;

public enum NotificationType {
    BACKUP_ALERT("email/backup-alert"),
    ORDER_CONFIRMATION("email/order-confirmation"),
    PROMOTIONAL("email/promotional"),
    PASSWORD_RESET("email/password-reset"),
    SHIPPING_UPDATE("email/shipping-update");

    private final String templatePath;

    NotificationType(String templatePath) {
        this.templatePath = templatePath;
    }

    public String getTemplatePath() {
        return templatePath;
    }
}
