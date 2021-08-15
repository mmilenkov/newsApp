package com.example.newsapp.ui.fragments

import android.os.Bundle
import android.view.View
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.newsapp.R
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.fragment_article.*

class ArticleFragment: BaseNewsFragment(R.layout.fragment_article) {

    val args: ArticleFragmentArgs by navArgs()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val article = args.article
        webView.apply {
            webViewClient = WebViewClient()
            loadUrl(article.url ?: "https://google.com/news") //Alternatively do nothing?
        }

        floatingActionButton.setOnClickListener {
            viewModel.saveArticle(article)
            Snackbar.make(view, "Article has been saved", Snackbar.LENGTH_LONG)
                .show()
        }
    }
}