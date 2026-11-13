package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.ChatThread
import com.example.data.model.FeedPost
import com.example.data.model.NotificationItem
import com.example.data.model.PostComment
import com.example.data.model.ReelItem
import com.example.data.model.StoryItem
import com.example.data.model.UserAccount

@Database(
    entities = [
        UserAccount::class,
        StoryItem::class,
        FeedPost::class,
        PostComment::class,
        ReelItem::class,
        ChatThread::class,
        ChatMessage::class,
        NotificationItem::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class FreegramDatabase : RoomDatabase() {
    abstract fun freegramDao(): FreegramDao

    companion object {
        @Volatile
        private var instance: FreegramDatabase? = null

        fun getInstance(context: Context): FreegramDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FreegramDatabase::class.java,
                    "freegram.db"
                ).build().also { instance = it }
            }
        }
    }
}
