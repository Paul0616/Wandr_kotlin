package com.encorsa.wandr.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.encorsa.wandr.R
import com.encorsa.wandr.models.CategoryModel


class DrawerAdapter(private val menuItems: List<CategoryModel>): RecyclerView.Adapter<DrawerAdapter.MenuViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        return MenuViewHolder.from(parent)
    }

    override fun getItemCount(): Int {
        return menuItems.size
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
       val current = menuItems[position]
        holder.menuItem.text = current.categoryNames.get(0).name
    }

    class MenuViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var menuItem:TextView

        init {
            menuItem  = itemView.findViewById(R.id.tv_menu_category)
        }

        companion object {
            fun from(parent: ViewGroup): MenuViewHolder {
                val view =
                    LayoutInflater.from(parent.context).inflate(R.layout.menu_item_view, parent, false)
                return MenuViewHolder(view)
            }
        }
    }


}