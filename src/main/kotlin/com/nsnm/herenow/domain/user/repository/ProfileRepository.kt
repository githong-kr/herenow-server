package com.nsnm.herenow.domain.user.repository

import com.nsnm.herenow.domain.user.entity.ProfileEntity
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface ProfileRepository : JpaRepository<ProfileEntity, UUID>
