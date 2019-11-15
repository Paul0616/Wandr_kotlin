package com.encorsa.wandr.mainFragments.details

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions

import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.databinding.FragmentDetailBinding

class DetailFragment : Fragment() {

    companion object {
        fun newInstance() = DetailFragment()
    }

    private lateinit var viewModel: DetailViewModel
    private lateinit var binding: FragmentDetailBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentDetailBinding.inflate(inflater)
        binding.lifecycleOwner = this

       // (activity as AppCompatActivity).supportActionBar?.hide()
       // (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val application = requireNotNull(activity).application
        val dataSource = WandrDatabase.getInstance(application).wandrDatabaseDao

        val objective = DetailFragmentArgs.fromBundle(arguments!!).selectedObjective
        val viewModelFactory = DetailModelFactory(application, dataSource, objective)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(DetailViewModel::class.java)
        binding.objectiveId.text = objective.id

        viewModel.media.observe(this, Observer {
            binding.objectiveId.text = it.size.toString()
        })

        viewModel.networkErrors.observe(this, Observer {
            Log.i("DetailFragment", "Error: ${it}")
        })

        viewModel.imageNumber.observe(this, Observer {
            binding.imagesNum.text = it.toString()
        })

        viewModel.videoNumber.observe(this, Observer {
            binding.videosNum.text = it.toString()
        })
        viewModel.selectedObjective.observe(this, Observer {
            if (!it.defaultImageUrl.isNullOrEmpty()) {
                Glide.with(binding.detailImage.context)
                    .load(it.defaultImageUrl)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.detailImage)
            }
        })

        binding.detailScrollview.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->

                // User scrolled past image to height of toolbar and the title text is
                // underneath the toolbar, so the toolbar should be shown.
                Log.i("DetailFragment", "${scrollY}")
               // val shouldShowToolbar = scrollY > toolbar.height

                // The new state of the toolbar differs from the previous state; update
                // appbar and toolbar attributes.
//                if (isToolbarShown != shouldShowToolbar) {
//                    isToolbarShown = shouldShowToolbar
//
//                    // Use shadow animator to add elevation if toolbar is shown
//                    appbar.isActivated = shouldShowToolbar
//
//                    // Show the plant name if toolbar is shown
//                    toolbarLayout.isTitleEnabled = shouldShowToolbar
//                }
            }
        )

    }

}
