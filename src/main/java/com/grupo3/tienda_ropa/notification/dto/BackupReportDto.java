package com.grupo3.tienda_ropa.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BackupReportDto {
    private String status; // SUCCESS or FAILED
    private String filename;
    private String error;
}
