package com.projects.distributed_lovable.workspace_service.service;


import com.projects.distributed_lovable.workspace_service.dto.project.DeployResponse;

public interface DeploymentService {
    public DeployResponse deploy(Long projectId);
}
