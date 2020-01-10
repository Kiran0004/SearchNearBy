package com.fueled.search.nearbyfood.detail

import  android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import androidx.databinding.DataBindingUtil
import kotlinx.android.synthetic.main.activity_details.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import android.text.util.Linkify.WEB_URLS
import android.text.util.Linkify
import androidx.lifecycle.Observer
import com.fueled.search.nearbyfood.R
import com.fueled.search.nearbyfood.databinding.ActivityDetailsBinding
import com.google.android.material.snackbar.Snackbar

/**
 * Created by Kiran.
 */


const val KEY_VENUE_ID = "VENUE_ID"

class ItemDetailActivity : AppCompatActivity() {

    private val detailViewModel: DetailViewModel by viewModel {
        parametersOf(
            intent?.extras?.getString(
                KEY_VENUE_ID
            )
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        val binding =
            DataBindingUtil.setContentView<ActivityDetailsBinding>(
                this,
                R.layout.activity_details
            )
        binding.viewModel = detailViewModel

        Linkify.addLinks(binding.tvUrl, WEB_URLS)

        binding.toolbar.setNavigationOnClickListener { finish() }

        detailViewModel.errorLiveData.observe(this, Observer {
            Snackbar.make(binding.rootView, it, Snackbar.LENGTH_LONG).show()
        })
    }
}
