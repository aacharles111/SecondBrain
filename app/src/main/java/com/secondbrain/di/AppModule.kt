package com.secondbrain.di

import android.content.Context
import androidx.room.Room
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
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideNoteDao(database: NoteDatabase) = database.noteDao()
}
