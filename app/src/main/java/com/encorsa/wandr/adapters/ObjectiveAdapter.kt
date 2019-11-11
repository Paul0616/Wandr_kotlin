package com.encorsa.wandr.adapters

import android.os.Build
import android.text.Html
import android.text.Spanned
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


class ObjectiveAdapter(val onClickListener: OnClickListener) :
    ListAdapter<ObjectiveDatabaseModel, ObjectiveAdapter.ItemViewHolder>(ObjectiveDiffCallback()) {

    override fun onBindViewHolder(holder: ObjectiveAdapter.ItemViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bind(item)
            holder.itemImageView.setOnClickListener {
                onClickListener.onClick(item)
            }
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        return ItemViewHolder.from(parent)
    }

    class ItemViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var itemName: TextView
        var address: TextView
        var shortDescription: TextView
        var mMoreInfoTextView: TextView
        var mSubcategoryTextView: TextView
        var itemImageView: ImageView
        var locationButton: ImageView
        var favouritesButton: ImageView
        var urlButton: ImageView
        var line: ConstraintLayout

        init {
            itemName = itemView.findViewById(R.id.attractionTextView)
            address = itemView.findViewById(R.id.addressTextView)
            shortDescription = itemView.findViewById(R.id.shortDescription)
            itemImageView = itemView.findViewById(R.id.ItemImageView)
            locationButton = itemView.findViewById(R.id.locationButton)
            favouritesButton = itemView.findViewById(R.id.favoritesButton)
            urlButton = itemView.findViewById(R.id.urlButton)
            mMoreInfoTextView = itemView.findViewById(R.id.more_info)
            mSubcategoryTextView = itemView.findViewById(R.id.subcategory)
            line = itemView.findViewById(R.id.line) as ConstraintLayout
            line.setVisibility(View.GONE)
        }

        fun bind(
            item: ObjectiveDatabaseModel
        ) {
            itemName.text = item.name ?: itemView.context.getString(R.string.no_info)
            address.text = item.address ?: itemView.context.getString(R.string.no_info)
            val longDescription =
                item.longDescription ?: itemView.context.getString(R.string.no_info)
            val txt = fromHtml(longDescription)
            shortDescription.text = txt.toString()

            mSubcategoryTextView.text = item.subcategoryName

            favouritesButton.isSelected = item.isFavorite
            Glide.with(itemImageView.context)
                .load(item.defaultImageUrl)
                .centerCrop()
                .apply(
                    RequestOptions()
                        .placeholder(R.drawable.loading_animation)
                        .error(R.drawable.ic_no_image)
                )
                .into(itemImageView)

        }

        @SuppressWarnings("deprecation")
        fun fromHtml(html: String): Spanned {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
            } else {
                Html.fromHtml(html)
            }
        }

        companion object {
            fun from(parent: ViewGroup): ItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val view = layoutInflater.inflate(R.layout.objective_item_view, parent, false)
                return ItemViewHolder(view)
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

    class OnClickListener(val clickListener: (objective: ObjectiveDatabaseModel) -> Unit) {
        fun onClick(objective: ObjectiveDatabaseModel) = clickListener(objective)
    }
}
