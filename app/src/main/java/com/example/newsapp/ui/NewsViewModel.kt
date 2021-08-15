package com.example.newsapp.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp.models.Article
import com.example.newsapp.models.NewsResponse
import com.example.newsapp.repository.NewsRepository
import com.example.newsapp.util.Resource
import kotlinx.coroutines.launch
import retrofit2.Response

class NewsViewModel(val newsRepository: NewsRepository): ViewModel() {
    val topHeadlines: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var topHeadlinesPage = 1 //Used to manage pagination
    var topHeadlinesResponse: NewsResponse? = null

    val searchNews: MutableLiveData<Resource<NewsResponse>> = MutableLiveData()
    var searchNewsPage = 1 //Used to manage pagination
    var searchNewsResponse: NewsResponse? = null

    init {
        getTopHeadlines("gb")
    }

    fun getTopHeadlines(countryCode: String) = viewModelScope.launch {
        topHeadlines.postValue(Resource.Loading())
        val response = newsRepository.getTopHeadlines(countryCode, topHeadlinesPage)
        topHeadlines.postValue(handleTopHeadlinesResponse(response))

    }

    fun getSearchNews(searchQuery: String) = viewModelScope.launch {
        searchNews.postValue(Resource.Loading())
        val response = newsRepository.searchNews(searchQuery, searchNewsPage)
        searchNews.postValue(handleSearchNewsResponse(response))
    }

    private fun handleTopHeadlinesResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if(response.isSuccessful) {
            response.body()?.let { resultResponse ->
                topHeadlinesPage++
                if(topHeadlinesResponse == null) {
                    topHeadlinesResponse = resultResponse
                } else {
                    val articleList = topHeadlinesResponse?.articles
                    articleList?.addAll(resultResponse.articles)
                }
                return Resource.Success(topHeadlinesResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    private fun handleSearchNewsResponse(response: Response<NewsResponse>): Resource<NewsResponse> {
        if(response.isSuccessful) {
            response.body()?.let { resultResponse ->
                searchNewsPage++
                if(searchNewsResponse == null) {
                    searchNewsResponse = resultResponse
                } else {
                    val articleList = searchNewsResponse?.articles
                    articleList?.addAll(resultResponse.articles)
                }
                return Resource.Success(searchNewsResponse ?: resultResponse)
            }
        }
        return Resource.Error(response.message())
    }

    fun saveArticle(article: Article) = viewModelScope.launch {
            newsRepository.upsertArticle(article)
        }

    fun getSavedNews() = newsRepository.getSavedNews()

    fun deleteArticle(article: Article) = viewModelScope.launch {
        newsRepository.deleteArticle(article)
    }
}