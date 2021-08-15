package com.example.newsapp.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.newsapp.models.Article

@Database(entities = [Article::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class ArticleDb : RoomDatabase() {
    abstract fun getArticleDao(): ArticleDao

    companion object {
        @Volatile
        private var instance: ArticleDb? = null
        private val LOCK = Any() //for instance synchronisation
        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: createDatabase(context).also { instance = it }
        } //Called when initialising

        private fun createDatabase(context: Context) = Room.databaseBuilder(context.applicationContext,
        ArticleDb::class.java,
        "article_db.db"
        ).build()
    }
}