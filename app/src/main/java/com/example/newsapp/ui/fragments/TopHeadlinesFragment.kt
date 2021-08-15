package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AbsListView
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newsapp.R
import com.example.newsapp.adapters.NewsAdapter
import com.example.newsapp.util.Constants
import com.example.newsapp.util.Resource
import kotlinx.android.synthetic.main.fragment_top_headlines.*

class TopHeadlinesFragment: BaseNewsFragment(R.layout.fragment_top_headlines) {
    val TAG = "TOP_HEADLINES_FRAGMENT"

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
                viewModel.getTopHeadlines("gb")
                isScrolling = false
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()

        newsAdapter.setOnItemClickListener {
            val bundle = Bundle().apply {
                putSerializable("article", it)
            }
            findNavController().navigate(R.id.action_topHeadlinesFragment_to_articleFragment, bundle)
        }

        viewModel.topHeadlines.observe(viewLifecycleOwner, Observer { response ->
            when(response) {
                is Resource.Success -> {
                    hideProgressBar()
                    response.data?.let {
                        newsResponse ->
                        newsAdapter.differ.submitList(newsResponse.articles.toList()) // Differ doesn't like mutable lists
                        val totalPages = newsResponse.totalResults / Constants.QUERY_PAGE_SIZE + 2 //+2 for rounding and for last empty page
                        isLastPage = viewModel.topHeadlinesPage == totalPages
                        if(isLastPage) {
                            topHeadlinesRecyclerView.setPadding(0,0,0,0) //Necessary for the progress bar
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
        topHeadlinesRecyclerView.apply {
            adapter = newsAdapter
            layoutManager = LinearLayoutManager(activity)
            addOnScrollListener(customScrollListener)
        }
    }

}