package com.projects.distributed_lovable.workspace_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.checkerframework.checker.units.qual.N;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "processed_events")
@Data
@Builder
public class ProcessedEvents {
    @Id
    private String sagaId;
    private LocalDateTime localDateTime;
}
