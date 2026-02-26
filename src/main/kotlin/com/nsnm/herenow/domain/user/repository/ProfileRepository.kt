package com.nsnm.herenow.domain.user.repository

import com.nsnm.herenow.domain.user.model.entity.ProfileEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ProfileRepository : JpaRepository<ProfileEntity, String> {
    @org.springframework.data.jpa.repository.Query("SELECT p.avatarUrl FROM ProfileEntity p WHERE p.avatarUrl IS NOT NULL")
    fun findAllAvatarUrls(): List<String>
}
