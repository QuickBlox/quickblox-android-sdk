package com.quickblox.sample.conference.kotlin.presentation.screens.settings.views.selectionlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.sample.conference.kotlin.databinding.ItemSelectBinding

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class SelectionViewHolder(private val binding: ItemSelectBinding) : RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun newInstance(parent: ViewGroup): SelectionViewHolder {
            return SelectionViewHolder(ItemSelectBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    fun bind(item: Pair<String, Int>, isSelected: Boolean) {
        binding.tvText.text = item.first
        if (isSelected) {
            binding.ivSelected.visibility = View.VISIBLE
        } else {
            binding.ivSelected.visibility = View.GONE
        }
    }
}