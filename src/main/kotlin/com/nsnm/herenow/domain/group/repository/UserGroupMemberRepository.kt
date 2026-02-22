package com.nsnm.herenow.domain.group.repository

import com.nsnm.herenow.domain.group.model.entity.UserGroupMemberEntity
import com.nsnm.herenow.domain.group.model.enums.GroupRole
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserGroupMemberRepository : JpaRepository<UserGroupMemberEntity, String> {
    fun findByProfileIdAndGroupId(profileId: String, groupId: String): UserGroupMemberEntity?
    fun findByGroupId(groupId: String): List<UserGroupMemberEntity>
    fun findByProfileId(profileId: String): List<UserGroupMemberEntity>
    fun findByProfileIdAndRole(profileId: String, role: GroupRole): List<UserGroupMemberEntity>
}
