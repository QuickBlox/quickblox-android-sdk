package com.quickblox.sample.conference.kotlin.presentation.screens.settings.views.selectionlist

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class SelectionAdapter(private val items: List<Pair<String, Int>>, var selectedResolution: Int?) : RecyclerView.Adapter<SelectionViewHolder>() {
    private var selectionCallBack: CustomSelectionView.SelectionCallBack? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectionViewHolder {
        return SelectionViewHolder.newInstance(parent)
    }

    override fun onBindViewHolder(holder: SelectionViewHolder, position: Int) {
        val item = items[position]
        val isSelected = selectedResolution == item.second
        holder.bind(item, isSelected)
        holder.itemView.setOnClickListener {
            selectedResolution = item.second
            selectionCallBack?.changedValue(selectedResolution)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setCallBack(selectionCallBack: CustomSelectionView.SelectionCallBack) {
        this.selectionCallBack=selectionCallBack
    }
}