package com.projects.distributed_lovable.workspace_service.controller;

import com.projects.distributed_lovable.common_lib.dto.FileTreeDto;
import com.projects.distributed_lovable.common_lib.enums.ProjectPermissions;
import com.projects.distributed_lovable.workspace_service.service.ProjectFileService;
import com.projects.distributed_lovable.workspace_service.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/internal/v1")
public class InternalWorkspaceController {

    private final ProjectFileService projectFileService;
    private final ProjectService projectService;

    @GetMapping("/projects/{projectId}/files/tree")
    public FileTreeDto getFileTree(@PathVariable Long projectId)
    {
         return projectFileService.getFileTree(projectId);
    }
    @GetMapping("/projects/{projectId}/files/content")
    public String getFileContent(@PathVariable Long projectId, @RequestParam String path)
    {
        return projectFileService.getFileContent(projectId,path);
    }
    @GetMapping("/projects/{projectId}/permissions/check")
    public boolean checkProjectPermission(
            @PathVariable Long projectId,
            @RequestParam ProjectPermissions permission) {
        return projectService.hasPermission(projectId, permission);
    }
}
