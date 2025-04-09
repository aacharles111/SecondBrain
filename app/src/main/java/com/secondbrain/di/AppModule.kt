package com.secondbrain.di

import android.content.Context
import androidx.room.Room
import com.secondbrain.data.db.CardDao
import com.secondbrain.data.db.NoteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideNoteDatabase(@ApplicationContext context: Context): NoteDatabase {
        return Room.databaseBuilder(
            context,
            NoteDatabase::class.java,
            "notes_database"
        )
        .addMigrations(object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Create the new table
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `cards` " +
                    "(`id` TEXT NOT NULL, " +
                    "`title` TEXT NOT NULL, " +
                    "`content` TEXT NOT NULL, " +
                    "`summary` TEXT NOT NULL, " +
                    "`type` TEXT NOT NULL, " +
                    "`source` TEXT NOT NULL, " +
                    "`tags` TEXT NOT NULL, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "`updatedAt` INTEGER NOT NULL, " +
                    "`language` TEXT NOT NULL, " +
                    "`aiModel` TEXT NOT NULL, " +
                    "`summaryType` TEXT NOT NULL, " +
                    "`thumbnailUrl` TEXT, " +
                    "PRIMARY KEY(`id`))"
                )
            }
        })
        .build()
    }

    @Provides
    @Singleton
    fun provideNoteDao(database: NoteDatabase) = database.noteDao()

    @Provides
    @Singleton
    fun provideCardDao(database: NoteDatabase) = database.cardDao()
}
