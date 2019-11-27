package com.encorsa.wandr.mainFragments.videoPlayer

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.encorsa.wandr.R
import com.encorsa.wandr.databinding.FragmentVideoPlayerBinding
import com.encorsa.wandr.utils.makeTransperantStatusBar
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment



class VideoPlayerFragment : Fragment(), YouTubePlayer.OnInitializedListener {

    companion object {
        fun newInstance() = VideoPlayerFragment()
    }

    private lateinit var viewModel: VideoPlayerViewModel
    private lateinit var binding: FragmentVideoPlayerBinding
    private lateinit var youTubePlayerFragment: YouTubePlayerSupportFragment

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentVideoPlayerBinding.inflate(inflater)
        binding.lifecycleOwner = this
        (activity as AppCompatActivity).setSupportActionBar(binding.videoToolbar)
        activity?.window?.let {
            makeTransperantStatusBar(activity?.window!!, false)
        }
        (activity as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(true)



        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val media = VideoPlayerFragmentArgs.fromBundle(arguments!!).listMedia
        val objective = VideoPlayerFragmentArgs.fromBundle(arguments!!).objective
        viewModel = ViewModelProviders.of(this).get(VideoPlayerViewModel::class.java)
        // TODO: Use the ViewModel

        for (med in media){
            Log.i("VideoPlayerFragment", "${med.mediaUrl}")
        }
        binding.videoToolbar.setNavigationOnClickListener {
            Log.i("VideoPlayerFragment", "navigationup")
            findNavController().navigate(VideoPlayerFragmentDirections.actionVideoPlayerFragmentToDetailFragment(objective))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        youTubePlayerFragment = childFragmentManager.findFragmentById(R.id.youtube_fragment) as YouTubePlayerSupportFragment
        youTubePlayerFragment.initialize(getString(R.string.you_tube_api3_key), this)
    }

    override fun onInitializationSuccess(
        provider: YouTubePlayer.Provider?,
        player: YouTubePlayer?,
        wasRestored: Boolean
    ) {
        Log.i("VideoPlayerFragment", "YouTube initialization succes")
        if (!wasRestored) {
            player?.cueVideo("wKJ9KzGQq0w");
        }
    }

    override fun onInitializationFailure(
        provider: YouTubePlayer.Provider?,
        result: YouTubeInitializationResult?
    ) {
        Log.i("VideoPlayerFragment", "YouTube initialization failure")
    }
}
