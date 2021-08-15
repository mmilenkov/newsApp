package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.util.Constants
import com.example.newsapp.util.Resource
import kotlinx.android.synthetic.main.fragment_search_news.*
import kotlinx.android.synthetic.main.fragment_search_news.progressBar
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchNewsFragment: BaseNewsFragment(R.layout.fragment_search_news) {
    val TAG = "SEARCH_NEWS_FRAGMENT"

    lateinit var newsAdapter: NewsAdapter
    var isLoading = false
    var isLastPage = false
    var isScrolling = false

    val customScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if(newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) { //Currently scrolling
                isScrolling = true
            }
        }

        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)
            val layoutManager  = recyclerView.layoutManager as LinearLayoutManager

            val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
            val visibleItemCount = layoutManager.childCount
            val totalItemCount = layoutManager.itemCount

            val isNotLoadingAndNotAtLastPage = !isLoading && !isLastPage
            val isAtLastItem = firstVisibleItemPosition + visibleItemCount >= totalItemCount
            val isNotAtStart = firstVisibleItemPosition >= 0
            val isTotalMoreThanVisible = totalItemCount >= Constants.QUERY_PAGE_SIZE
            val shouldPaginate = isNotLoadingAndNotAtLastPage && isAtLastItem &&
                    isNotAtStart && isTotalMoreThanVisible && isScrolling
            if(shouldPaginate) {
                viewModel.getSearchNews(searchBox.text.toString())
                isScrolling = false
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        var job: Job? = null
        searchBox.addTextChangedListener { editable ->
            job?.cancel()
            job = MainScope().launch {
                delay(Constants.SEARCH_DELAY)
                editable?.let {
                    if(it.toString().isNotEmpty()) {
                        viewModel.getSearchNews(it.toString())
                    }
                }
            }
        }

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_searchNewsFragment_to_articleFragment, bundle)
        }

        viewModel.searchNews.observe(viewLifecycleOwner, Observer { response ->
            when(response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let {
                            newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList())
                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2 //+2 for rounding and for last empty page
                        isLastPage = viewModel.searchNewsPage == totalPages
                        if(isLastPage) {
                            searchNewsRecyclerView.setPadding(0,0,0,0) //Necessary for the progress bar
                        }
                    }
                }
                is Resource.Error -> {
                    hideProgressBar()
                    response.message?.let { message ->
                        Log.e(TAG, "An error occured: $message")
                    }
                }
                is Resource.Loading -> {
                    showProgressBar()
                }
            }
        })
    }

    private fun hideProgressBar() {
        progressBar.visibility = View.INVISIBLE
        isLoading = false
    }

    private fun showProgressBar() {
        progressBar.visibility = View.VISIBLE
        isLoading = true
    }

    private fun setupRecyclerView() {
        newsAdapter = NewsAdapter()
        searchNewsRecyclerView.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(customScrollListener)
        }
    }


}