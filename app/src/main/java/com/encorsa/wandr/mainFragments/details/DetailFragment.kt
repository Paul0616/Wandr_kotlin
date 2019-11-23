package com.encorsa.wandr.mainFragments.details

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.databinding.FragmentDetailBinding
import com.encorsa.wandr.utils.makeTransperantStatusBar


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


        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.title = ""
        activity?.window?.let {
            makeTransperantStatusBar(activity?.window!!, true)
        }

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
        binding.detailViewModel = viewModel
        binding.photoGallery.visibility = View.GONE
        binding.videoGallery.visibility = View.GONE

//        viewModel.media.observe(this, Observer {
//            binding.objectiveId.text = it.size.toString()
//        })


        viewModel.networkErrors.observe(this, Observer {
            Log.i("DetailFragment", "Error: ${it}")
        })

        viewModel.displayPhotoGallery.observe(this, Observer {
            if (it != null) {
                this.findNavController()
                    .navigate(DetailFragmentDirections.actionDetailFragmentToImageSliderFragment(it, objective))
            }
        })

        viewModel.displayVideoGallery.observe(this, Observer {
            if (it != null) {
                Toast.makeText(
                    context,
                    it?.size.toString(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
        viewModel.photoGallery.observe(this, Observer {
            if (it.size > 0)
                binding.photoGallery.visibility = View.VISIBLE
        })

        viewModel.videoGallery.observe(this, Observer {
            if (it.size > 0)
                binding.videoGallery.visibility = View.VISIBLE
        })
//        viewModel.selectedObjective.observe(this, Observer {
//
//        })

//        viewModel.viewMenu.observe(this, Observer {
//            when(it){
//                true -> {
//                    //hide red action bar and set transparent one as new toolbar
//                    (activity as AppCompatActivity).supportActionBar?.hide()
//                    (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
//                }
//                false -> {
//                    //set main toolbar as new toolbar
//                    (activity as AppCompatActivity).setSupportActionBar(null)
//                   (activity as AppCompatActivity).supportActionBar?.show()
//                    //
//                }
//            }
//
//        })

        binding.fab.setOnClickListener{
            Log.i("DetailFragment", "FAB WAS CLICKED")
        }

        binding.toolbar.setNavigationOnClickListener { view ->
            view.findNavController().navigateUp()
        }

        var isToolbarShown = false
        binding.detailScrollview.setOnScrollChangeListener(
            NestedScrollView.OnScrollChangeListener { _, _, scrollY, _, _ ->

                // User scrolled past image to height of toolbar and the title text is
                // underneath the toolbar, so the toolbar should be shown.
                Log.i("DetailFragment", "${scrollY}")
                val shouldShowToolbar = scrollY > binding.toolbar.height

                // The new state of the toolbar differs from the previous state; update
                // appbar and toolbar attributes.
                if (isToolbarShown != shouldShowToolbar) {
                    isToolbarShown = shouldShowToolbar

                    // Use shadow animator to add elevation if toolbar is shown
                    binding.appbar.isActivated = shouldShowToolbar

                    // Show the plant name if toolbar is shown
                    binding.toolbarLayout.isTitleEnabled = shouldShowToolbar
                }
            }
        )


    }


}
