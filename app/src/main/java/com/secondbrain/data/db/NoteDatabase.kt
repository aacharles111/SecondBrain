package com.secondbrain.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.secondbrain.data.model.Note
import com.secondbrain.data.model.Card

@Database(entities = [Note::class, Card::class], version = 3, exportSchema = false)
@TypeConverters(Converters::class)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun cardDao(): CardDao
}
