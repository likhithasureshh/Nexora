package com.projects.distributed_lovable.intelligence_service.security;

import com.projects.distributed_lovable.common_lib.enums.ProjectPermissions;
import com.projects.distributed_lovable.common_lib.security.AuthUtil;
import com.projects.distributed_lovable.intelligence_service.client.WorkspaceClient;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.stereotype.Component;

import static com.projects.distributed_lovable.common_lib.enums.ProjectPermissions.*;


@Component("security")
@RequiredArgsConstructor
@Slf4j
public class SecurityExpressions {

    private final AuthUtil authUtil;
    private final WorkspaceClient workspaceClient;

    public boolean handlePermissions(Long projectId, ProjectPermissions projectPermissions)
    {
        try {
            return workspaceClient.checkPermission(projectId, projectPermissions);
        } catch (FeignException.Unauthorized e) {
            log.warn("Token expired or invalid during permission check for project: {}", projectId);
            throw new CredentialsExpiredException("JWT token is expired or invalid");
        } catch (FeignException e) {
            log.error("Workspace-service failed during permission check: {}", e.getMessage());
            return false;
        }
    }

    public boolean canViewProject(Long projectId)
    {
        return handlePermissions(projectId,VIEW);
    }
    public boolean canEditProject(Long projectId)
    {
        return handlePermissions(projectId,EDIT);
    }
    public boolean canDeleteProject(Long projectId)
    {
        return handlePermissions(projectId,DELETE);
    }

    public boolean canViewMembers(Long projectId)
    {
        return handlePermissions(projectId,VIEW);
    }

    public boolean canManageMembers(Long projectId)
    {
        return handlePermissions(projectId,MANAGE_MEMBERS);
    }

}
