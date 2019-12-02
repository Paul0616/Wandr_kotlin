package com.encorsa.wandr.mainFragments.videoPlayer

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.encorsa.wandr.R
import com.encorsa.wandr.adapters.VideosAdapter
import com.encorsa.wandr.database.ListMediaDatabaseModel
import com.encorsa.wandr.database.WandrDatabase
import com.encorsa.wandr.databinding.FragmentVideoPlayerBinding
import com.encorsa.wandr.utils.DEFAULT_LANGUAGE
import com.encorsa.wandr.utils.Prefs
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
    private lateinit var videoId:String
    private lateinit var messageYouTubeConnectionError: String
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

        if (resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE)
            (activity as AppCompatActivity).supportActionBar?.hide()
        else
            (activity as AppCompatActivity).supportActionBar?.show()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val media: ListMediaDatabaseModel = VideoPlayerFragmentArgs.fromBundle(arguments!!).listMedia
        val objective = VideoPlayerFragmentArgs.fromBundle(arguments!!).objective

        val application = requireNotNull(activity).application
        val dataSource = WandrDatabase.getInstance(application).wandrDatabaseDao
        val viewModelFactory = VideoPlayerModelFactory(application, dataSource)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(VideoPlayerViewModel::class.java)

        messageYouTubeConnectionError = getString(R.string.you_tube_connection_error)


        var videos = media.map {
            it.withVideoId()
        }.toList()
        val adapter = VideosAdapter(VideosAdapter.OnClickListener{video ->
            viewModel.videoWasClicked(video)
        })


       // videos.first().isSelected = true
        viewModel.setVideos(0, videos)
        binding.videosList.adapter = adapter
        val currLang = Prefs(application.applicationContext).currentLanguage
        viewModel.getLabelByTagAndLanguage(currLang ?: DEFAULT_LANGUAGE)

        /*  --------------------------
         *   observe translation change
         *  ----------------------------
         */

        viewModel.translations.observe(this, Observer{
            messageYouTubeConnectionError = it.youTubeConnectionerror ?: ""
        })

        binding.videoToolbar.setNavigationOnClickListener {
            Log.i("VideoPlayerFragment", "navigationup")
            findNavController().navigate(VideoPlayerFragmentDirections.actionVideoPlayerFragmentToDetailFragment(objective))
        }

        viewModel.videos.observe(this, Observer {
            adapter.data = it
        })


        viewModel.videoId.observe(this, Observer {
            videoId = it
            youTubePlayerFragment.onDestroy()
            youTubePlayerFragment.initialize(getString(R.string.you_tube_api3_key), this)
            Toast.makeText(context, videoId, Toast.LENGTH_SHORT).show()
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        youTubePlayerFragment = childFragmentManager.findFragmentById(R.id.youtube_fragment) as YouTubePlayerSupportFragment
    }

    override fun onInitializationSuccess(
        provider: YouTubePlayer.Provider?,
        player: YouTubePlayer?,
        wasRestored: Boolean
    ) {
        Log.i("VideoPlayerFragment", "YouTube initialization succes")
        if (!wasRestored) {
            player?.cueVideo(videoId);
        }
    }

    override fun onInitializationFailure(
        provider: YouTubePlayer.Provider?,
        result: YouTubeInitializationResult?
    ) {
        Toast.makeText(context, "${messageYouTubeConnectionError}: ${result.toString()}", Toast.LENGTH_LONG).show()

    }
}
