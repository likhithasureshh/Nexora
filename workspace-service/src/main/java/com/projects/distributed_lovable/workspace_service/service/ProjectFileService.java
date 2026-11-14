package com.projects.distributed_lovable.workspace_service.service;


import com.projects.distributed_lovable.common_lib.dto.FileTreeDto;
import com.projects.distributed_lovable.workspace_service.dto.project.FileContentResponse;

public interface ProjectFileService {

    String getFileContent(Long projectId, String path);

    FileTreeDto getFileTree(Long projectId);

    void saveFiles(Long projectId, String filePath, String fileContent);
}
