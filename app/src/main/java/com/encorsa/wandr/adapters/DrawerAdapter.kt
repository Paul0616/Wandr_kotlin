package com.encorsa.wandr.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.encorsa.wandr.R
import com.encorsa.wandr.database.CategoryDatabaseModel
import com.encorsa.wandr.models.CategoryModel
import com.encorsa.wandr.utils.Prefs


class DrawerAdapter(private val context: Context, private val menuItems: List<CategoryDatabaseModel>, val onClickListener: OnClickListener): RecyclerView.Adapter<DrawerAdapter.MenuViewHolder>() {

    val prefs = Prefs(context)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        return MenuViewHolder.from(parent)
    }

    override fun getItemCount(): Int {
        return menuItems.size
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
        val current = menuItems[position]
        holder.menuItem.text = current.name
        holder.menuItem.setOnClickListener {
            onClickListener.onClick(current)
            notifyDataSetChanged()
        }
        holder.menuIcon.isSelected = current.id  == prefs.currentCategoryId ?: ""
    }

    class MenuViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var menuItem:TextView
        var menuIcon:ImageView

        init {
            menuItem  = itemView.findViewById(R.id.tv_menu_category)
            menuIcon = itemView.findViewById(R.id.imageView5)
        }


        companion object {
            fun from(parent: ViewGroup): MenuViewHolder {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.menu_item_view, parent, false)
                return MenuViewHolder(view)
            }
        }
    }

    class OnClickListener(val clickListener: (menuItem: CategoryDatabaseModel) -> Unit) {
        fun onClick(menuItem: CategoryDatabaseModel) = clickListener(menuItem)
    }

}