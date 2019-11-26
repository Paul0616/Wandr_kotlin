package com.encorsa.wandr.mainFragments.imageSlider


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import com.encorsa.wandr.database.ListMediaDatabaseModel
import com.encorsa.wandr.database.ObjectiveDatabaseModel
import com.encorsa.wandr.databinding.FragmentImageSliderBinding

/**
 * A simple [Fragment] subclass.
 */
class ImageSliderFragment : Fragment(){

    private lateinit var binding: FragmentImageSliderBinding
    private lateinit var viewModel: ImageSliderViewModel
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val media = ImageSliderFragmentArgs.fromBundle(arguments!!).listMedia
        val objective = ImageSliderFragmentArgs.fromBundle(arguments!!).objective
        binding = FragmentImageSliderBinding.inflate(inflater)
        binding.setLifecycleOwner(this)
        (activity as AppCompatActivity).setSupportActionBar(binding.sliderToolbar)
        viewModel = ViewModelProviders.of(this).get(ImageSliderViewModel::class.java)

        /*
            set arrows visibility for first page
         */
        viewModel.pageWasChanged(0, media.count())

        val pagerAdapter = ImageSliderPagerAdapter(context?.applicationContext!!, media, ImageSliderPagerAdapter.OnPageChangeListener{currentPage ->
            viewModel.pageWasChanged(currentPage, media.count())
        })

        binding.pager.adapter = pagerAdapter
        binding.pager.addOnPageChangeListener(pagerAdapter.onPageChangeListener)


        binding.sliderToolbar.setNavigationOnClickListener {
            findNavController().navigate(ImageSliderFragmentDirections.actionImageSliderFragmentToDetailFragment(objective))
        }


        viewModel.leftArrowIsVisible.observe(this, Observer {
            Log.i("ImageSliderFragment", "leftArrow ${it}")
            binding.leftArrowVisibility = it
        })
        viewModel.rightArrowIsVisible.observe(this, Observer {
            Log.i("ImageSliderFragment", "rightArrow ${it}")
            binding.rightArrowVisibility = it
        })
        return binding.root
    }


}
