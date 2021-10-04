package com.quickblox.sample.conference.kotlin.presentation.screens.main

import android.view.ViewGroup
import androidx.annotation.IntDef
import androidx.collection.ArraySet
import androidx.collection.arraySetOf
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.sample.conference.kotlin.presentation.screens.main.DialogsAdapter.DialogsAdapterStates.Companion.DEFAULT
import com.quickblox.sample.conference.kotlin.presentation.screens.main.DialogsAdapter.DialogsAdapterStates.Companion.SELECT

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class DialogsAdapter(private val items: ArrayList<QBChatDialog>, var dialogAdapterListener: DialogAdapterListener) : RecyclerView.Adapter<DialogViewHolder>() {
    private var adapterState = DEFAULT
    private var selectedDialogs = arraySetOf<QBChatDialog>()

    init {
        setHasStableIds(true)
    }

    @IntDef(DEFAULT, SELECT)
    annotation class DialogsAdapterStates {
        companion object {
            const val DEFAULT = 0
            const val SELECT = 1
        }
    }

    fun getSelectedDialogs(): ArraySet<QBChatDialog> {
        return selectedDialogs
    }

    fun moveToFirst(dialog: QBChatDialog) {
        items.remove(dialog)
        items.add(0, dialog)
        notifyDataSetChanged()
    }

    fun setState(adapterState: Int) {
        this.adapterState = adapterState
        clearSelectedDialogs()
        dialogAdapterListener.onChanged(adapterState)
        notifyDataSetChanged()
    }

    fun clearSelectedDialogs() {
        selectedDialogs.clear()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DialogViewHolder {
        return DialogViewHolder.newInstance(parent, object : DialogViewHolder.ItemListener {
            override fun onClick(dialog: QBChatDialog) {
                dialogAdapterListener.onDialogClicked(dialog)
            }

            override fun onLongClick(dialog: QBChatDialog) {
                adapterState = SELECT
                notifyDataSetChanged()
                selectedDialogs.add(dialog)
                dialogAdapterListener.onChanged(adapterState)
                dialogAdapterListener.onSelected(selectedDialogs.size)
            }

            override fun onSelect(dialog: QBChatDialog) {
                if (selectedDialogs.contains(dialog)) {
                    selectedDialogs.remove(dialog)
                } else {
                    selectedDialogs.add(dialog)
                }
                dialogAdapterListener.onSelected(selectedDialogs.size)
            }
        })
    }

    override fun onBindViewHolder(holder: DialogViewHolder, position: Int) {
        val isSelected = selectedDialogs.contains(items[position])
        items[position].let { holder.bind(it, position, adapterState, isSelected) }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return items[position].hashCode().toLong()
    }

    interface DialogAdapterListener {
        fun onChanged(state: Int)
        fun onSelected(selectedCounter: Int)
        fun onDialogClicked(dialog: QBChatDialog)
    }
}