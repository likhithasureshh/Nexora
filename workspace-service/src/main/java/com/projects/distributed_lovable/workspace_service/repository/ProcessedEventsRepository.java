package com.projects.distributed_lovable.workspace_service.repository;

import com.projects.distributed_lovable.workspace_service.entity.ProcessedEvents;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProcessedEventsRepository extends JpaRepository<ProcessedEvents,String> {
}
