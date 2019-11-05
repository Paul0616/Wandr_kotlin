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
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.encorsa.wandr.R
import com.encorsa.wandr.database.ObjectiveDatabaseModel
import com.encorsa.wandr.network.models.ObjectiveModel


class ObjectiveAdapter : ListAdapter<ObjectiveDatabaseModel, ObjectiveAdapter.ItemViewHolder>(ObjectiveDiffCallback()){
//    var objectives = mutableListOf<ObjectiveDatabaseModel>()
//        set(value) {
//            field = value
//            notifyDataSetChanged()
//        }

//    fun addData(listItems: List<ObjectiveDatabaseModel>) {
//        var size = objectives.size
//        objectives.addAll(listItems)
//        var sizeNew = objectives.size
//        notifyItemRangeChanged(size, sizeNew)
//    }

    //    override fun getItemCount(): Int = objectives.size


    override fun onBindViewHolder(holder: ObjectiveAdapter.ItemViewHolder, position: Int) {
//        val item = objectives[position]
        val item = getItem(position)
        if (item != null)
            holder.bind(item)
    }





    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ItemViewHolder {
        return ItemViewHolder.from(parent)
    }


    class ItemViewHolder private constructor(itemView: View) : RecyclerView.ViewHolder(itemView){
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

//            if (moreInfo != null)
//                mMoreInfoTextView.setText(moreInfo)
//            locationButton.setOnClickListener(object : View.OnClickListener {
//                override fun onClick(view: View) {
//                    selectLocationView()
//                }
//            })
//            urlButton.setOnClickListener(object : View.OnClickListener {
//                override fun onClick(view: View) {
//                    if (attractions.get(adapterPosition).getUrl() != null)
//                        Toast.makeText(
//                            this@MainActivity,
//                            attractions.get(adapterPosition).getUrl(),
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    val i = Intent(this@MainActivity, ViewUrlActivity::class.java)
//                    i.putExtra("url", attractions.get(adapterPosition).getUrl())
//                    startActivity(i)
//                    //                        Intent i = new Intent(Intent.ACTION_VIEW);
//                    //                        i.setData(Uri.parse(Helper.URL_PRIVACY));
//                    //                        startActivity(i);
//                }
//            })
//            favouritesButton.setOnClickListener(object : View.OnClickListener {
//                override fun onClick(view: View) {
//                    //favouritesButton.setSelected(!favouritesButton.isSelected());
//                    if (!isLoading) {
//                        if (favouritesButton.isSelected())
//                            attemptDeleteFromFavorites(getFavoriteId(attractions.get(adapterPosition).getFavorites()))
//                        else
//                            attemptAddToFavorites(attractions.get(adapterPosition).getId())
//                    }
//                }
//            })
//            view.setOnClickListener(object : View.OnClickListener {
//                override fun onClick(view: View) {
//                    //Toast.makeText(MainActivity.this, attractions.get(getAdapterPosition()).getName(), Toast.LENGTH_SHORT).show();
//                    val i = Intent(this@MainActivity, DetailActivity::class.java)
//                    i.putExtra("objectiveId", attractions.get(adapterPosition).getId())
//                    startActivity(i)
//                }
//            })
        }

        fun bind(
            item: ObjectiveDatabaseModel
        ) {
            itemName.text = item.name
            address.text = item.address
            val longDescription = item.longDescription
            longDescription?.let {
                val txt = fromHtml(longDescription)
                shortDescription.text = txt.toString()
            }
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

        override fun areItemsTheSame(oldItem: ObjectiveDatabaseModel, newItem: ObjectiveDatabaseModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ObjectiveDatabaseModel, newItem: ObjectiveDatabaseModel): Boolean {
            return oldItem == newItem
        }
    }
}
