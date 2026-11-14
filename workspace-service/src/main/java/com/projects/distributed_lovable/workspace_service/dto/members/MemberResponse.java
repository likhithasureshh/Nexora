package com.projects.distributed_lovable.workspace_service.dto.members;


import com.projects.distributed_lovable.common_lib.enums.ProjectRole;

public record MemberResponse(
        Long userId,
        String name,
        String username,
        String avatarUrl,
        String invitedAt,
        ProjectRole projectRole
) {
}
