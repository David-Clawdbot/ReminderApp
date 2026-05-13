package com.reminder.app.di

import android.content.Context
import androidx.room.Room
import com.reminder.app.data.local.ReminderDao
import com.reminder.app.data.local.ReminderDatabase
import com.reminder.app.data.repository.ReminderRepository
import com.reminder.app.data.repository.ReminderRepositoryImpl
import dagger.Binds
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
    fun provideReminderDatabase(
        @ApplicationContext context: Context
    ): ReminderDatabase {
        return Room.databaseBuilder(
            context,
            ReminderDatabase::class.java,
            "reminder_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideReminderDao(database: ReminderDatabase): ReminderDao {
        return database.reminderDao()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindReminderRepository(
        impl: ReminderRepositoryImpl
    ): ReminderRepository
}
