package com.workshop.ticketsystem.entity;

import com.workshop.ticketsystem.enums.DeviceType;
import com.workshop.ticketsystem.enums.TicketSource;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketMetadata {

    @Enumerated(EnumType.STRING)
    private TicketSource source;

    private String browser;

    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;
}
