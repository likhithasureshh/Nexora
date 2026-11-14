package com.projects.distributed_lovable.intelligence_service.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "chat_sessions")
public class ChatSession {

    @EmbeddedId
    ChatSessionId chatSessionId;
    @CreationTimestamp
    @Column(nullable = false,updatable = false)
    Instant createdAt;
    @UpdateTimestamp
    Instant updatedAt;


    Instant deletedAt;

}
