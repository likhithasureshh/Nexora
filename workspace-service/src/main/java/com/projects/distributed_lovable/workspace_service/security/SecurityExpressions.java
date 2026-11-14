package com.projects.distributed_lovable.workspace_service.security;

import com.projects.distributed_lovable.common_lib.enums.ProjectPermissions;
import com.projects.distributed_lovable.common_lib.security.AuthUtil;
import com.projects.distributed_lovable.workspace_service.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.projects.distributed_lovable.common_lib.enums.ProjectPermissions.*;


@Component("security")
@RequiredArgsConstructor
public class SecurityExpressions {

    private final ProjectMemberRepository projectMemberRepository;
    private final AuthUtil authUtil;

    public boolean handlePermissions(Long projectId, ProjectPermissions projectPermissions)
    {
        Long userId = authUtil.getCurrentUserId();
        return projectMemberRepository.findRoleByProjectIdAndUserId(projectId,userId)
                .map(role-> role.getPermissionsSet().contains(projectPermissions))
                .orElse(false);
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
