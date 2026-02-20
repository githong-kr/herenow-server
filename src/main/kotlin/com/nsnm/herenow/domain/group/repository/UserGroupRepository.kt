package com.nsnm.herenow.domain.group.repository

import com.nsnm.herenow.domain.group.model.entity.UserGroupEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserGroupRepository : JpaRepository<UserGroupEntity, String>
