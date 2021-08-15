package com.example.newsapp.repository

import com.example.newsapp.api.RetrofitInstance
import com.example.newsapp.db.ArticleDb
import com.example.newsapp.models.Article

class NewsRepository(val db: ArticleDb) {
    suspend fun getTopHeadlines(countryCode: String, pageNumber: Int) =
        RetrofitInstance.apiFascade.getTopHeadlines(countryCode, pageNumber)

    suspend fun searchNews(searchQuery: String, pageNumber: Int) =
        RetrofitInstance.apiFascade.getSearchResults(searchQuery, pageNumber)

    suspend fun upsertArticle(article: Article) =
        db.getArticleDao().upsertArticle(article)

    fun getSavedNews() =
        db.getArticleDao().getAllArticles()

    suspend fun deleteArticle(article: Article) =
        db.getArticleDao().deleteArticle(article)

}