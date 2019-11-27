package com.encorsa.wandr.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.encorsa.wandr.database.ListMediaDatabaseModel
import com.encorsa.wandr.database.MediaDatabaseModel
import com.encorsa.wandr.databinding.VideoIdItemBinding


class VideosAdapter : RecyclerView.Adapter<VideosAdapter.ViewHolder>() {
    var data = ListMediaDatabaseModel()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item, position)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    class ViewHolder private constructor(val binding: VideoIdItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: MediaDatabaseModel,
            position: Int
        ) {
            binding.playButton.isSelected = item.isSelected
            binding.videoName.text = item.title ?: "Video $position"
        }

        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = VideoIdItemBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }


}