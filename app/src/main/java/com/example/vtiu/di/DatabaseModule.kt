package com.example.vtiu.di

import android.content.Context
import androidx.room.Room
import com.example.vtiu.data.local.room.AppDatabase
import com.example.vtiu.data.local.room.dao.ProfileDao
import com.example.vtiu.data.local.room.dao.TimetableDao
import com.example.vtiu.data.local.room.dao.CourseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "vtiu_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideProfileDao(database: AppDatabase): ProfileDao {
        return database.profileDao()
    }

    @Provides
    fun provideTimetableDao(database: AppDatabase): TimetableDao {
        return database.timetableDao()
    }

    @Provides
    fun provideCourseDao(database: AppDatabase): CourseDao {
        return database.courseDao()
    }
}
