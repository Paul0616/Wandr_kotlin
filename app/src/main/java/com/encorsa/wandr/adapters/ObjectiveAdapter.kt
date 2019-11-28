package com.encorsa.wandr.adapters

import android.content.Context
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.encorsa.wandr.R
import com.encorsa.wandr.database.ObjectiveDatabaseModel
import com.encorsa.wandr.databinding.ObjectiveItemViewBinding
import com.encorsa.wandr.utils.Prefs
import com.encorsa.wandr.utils.Translations

enum class ViewClicked { OBJECTIVE, FAVORITE, URL, LOCATION }


class ObjectiveAdapter(private val appContext: Context, val onClickListener: OnClickListener) :
    ListAdapter<ObjectiveDatabaseModel, ObjectiveAdapter.ItemViewHolder>(ObjectiveDiffCallback()) {

//    var translations = Translations()
//        set (value){
//            ItemViewHolder.translations = this.translations
//            //notifyDataSetChanged()
//        }

    private val prefs = Prefs(appContext)
    override fun onBindViewHolder(holder: ObjectiveAdapter.ItemViewHolder, position: Int) {
        val item = getItem(position)
        val userId = prefs.userId
        if (item != null)
            holder.bind(onClickListener, item, userId)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        return ItemViewHolder.from(parent)
    }

    class ItemViewHolder private constructor(val binding: ObjectiveItemViewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            clickListener: OnClickListener,
            item: ObjectiveDatabaseModel,
            userId: String?
        ) {
            binding.clickListener = clickListener
            binding.objective = item
            binding.translation = translations
            if (userId != null) {
                binding.favoritesButton.isEnabled = true
                binding.favoritesButton.setOnClickListener {
                    Log.i(
                        "ObjectiveAdapter", when (it.isSelected) {
                            true -> "REMOVE FAVORITE"
                            false -> "ADD FAVORITE"
                        }
                    )
                    it.isEnabled = false
                    clickListener.onClick(item, ViewClicked.FAVORITE)
                }
            } else
                binding.favoritesButton.isEnabled = false

        }


        companion object {
            var translations: Translations = Translations()
//            set(value) {
//                translations = value
//            }
            fun from(parent: ViewGroup): ItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)

                val binding = ObjectiveItemViewBinding.inflate(layoutInflater, parent, false)
                // val view = layoutInflater.inflate(R.layout.objective_item_view, parent, false)
                return ItemViewHolder(binding)
            }
        }

    }

    class ObjectiveDiffCallback :
        DiffUtil.ItemCallback<ObjectiveDatabaseModel>() {

        override fun areItemsTheSame(
            oldItem: ObjectiveDatabaseModel,
            newItem: ObjectiveDatabaseModel
        ): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(
            oldItem: ObjectiveDatabaseModel,
            newItem: ObjectiveDatabaseModel
        ): Boolean {
            return oldItem == newItem
        }
    }

    class OnClickListener(val clickListener: (objective: ObjectiveDatabaseModel, viewClicked: ViewClicked) -> Unit) {
        fun onClick(objective: ObjectiveDatabaseModel, viewClicked: ViewClicked) =
            clickListener(objective, viewClicked)
    }
}
