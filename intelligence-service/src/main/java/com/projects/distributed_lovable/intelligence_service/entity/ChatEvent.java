package com.projects.distributed_lovable.intelligence_service.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;

import com.projects.distributed_lovable.common_lib.enums.ChatEventStatus;
import com.projects.distributed_lovable.common_lib.enums.ChatEventType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "chat_events")
@Getter
@Setter
@Builder
public class ChatEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    @JsonIgnore
    ChatMessage chatMessage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    ChatEventType type;

    @Column(nullable = false)
    Integer sequenceOrder;

    @Column(columnDefinition = "text")
    String content;

    String filePath;

    @Column(columnDefinition = "text")
    String metadata;

    String sagaId;

    @Enumerated(EnumType.STRING)
    ChatEventStatus status;

}
