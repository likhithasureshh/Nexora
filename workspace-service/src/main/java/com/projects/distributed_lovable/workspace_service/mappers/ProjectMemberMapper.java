package com.projects.distributed_lovable.workspace_service.mappers;


import com.projects.distributed_lovable.workspace_service.dto.members.MemberResponse;
import com.projects.distributed_lovable.workspace_service.entity.ProjectMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {
//    @Mapping(target = "userId",source = "id")
//    @Mapping(target = "projectRole",constant = "OWNER")
//    //MemberResponse toMemberResponseFromUser(User user);

    @Mapping(target = "userId",source = "id.userId")
    MemberResponse toMemberResponseFromProjectMember(ProjectMember projectMember);
}
