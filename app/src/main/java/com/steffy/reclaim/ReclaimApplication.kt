package com.steffy.reclaim

import android.app.Application
import com.steffy.reclaim.data.profile.ProfileRepository
import com.steffy.reclaim.data.profile.RoomProfileRepository
import com.steffy.reclaim.database.ReclaimDatabase

class ReclaimApplication : Application() {
    lateinit var profileRepository: ProfileRepository
        private set

    override fun onCreate() {
        super.onCreate()
        profileRepository = RoomProfileRepository(ReclaimDatabase.getInstance(this))
    }
}