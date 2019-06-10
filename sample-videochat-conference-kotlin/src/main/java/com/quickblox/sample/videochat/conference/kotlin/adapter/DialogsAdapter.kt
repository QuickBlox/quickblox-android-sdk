package com.quickblox.sample.videochat.conference.kotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.chat.model.QBChatDialog
import com.quickblox.sample.videochat.conference.kotlin.R
import com.quickblox.sample.videochat.conference.kotlin.utils.getColor
import com.quickblox.sample.videochat.conference.kotlin.utils.getGreyCircleDrawable
import kotlinx.android.synthetic.main.list_item_dialog.view.*


class DialogsAdapter(val context: Context,
                     private var items: List<QBChatDialog>,
                     private val clickListener: OnItemClickListener) : RecyclerView.Adapter<DialogsAdapter.ViewHolder>() {

    private val selectedItems: MutableList<QBChatDialog> = ArrayList()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.nameTextView.text = items[position].name
        holder.dialogImageView.setBackgroundDrawable(getGreyCircleDrawable())

        val colorResource = if (isItemSelected(items[position])) {
            getColor(R.color.selected_list_item_color)
        } else {
            getColor(android.R.color.transparent)
        }

        holder.rootLayout.setBackgroundColor(colorResource)
        holder.rootLayout.setOnClickListener { clickListener.onShortClick(items[position]) }
        holder.rootLayout.setOnLongClickListener { clickListener.onLongClick(items[position]);true }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_dialog, parent, false))
    }

    fun updateDialogs(items: List<QBChatDialog>) {
        this.items = items
        notifyDataSetChanged()
    }

    private fun isItemSelected(item: QBChatDialog): Boolean {
        return selectedItems.contains(item)
    }

    fun getSelectedItems(): List<QBChatDialog> {
        return selectedItems
    }

    fun clearSelectedItems() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun selectItem(dialog: QBChatDialog) {
        if (selectedItems.contains(dialog)) {
            return
        }
        selectedItems.add(dialog)
        notifyDataSetChanged()
    }

    fun toggleSelection(dialog: QBChatDialog) {
        if (selectedItems.contains(dialog)) {
            selectedItems.remove(dialog)
        } else {
            selectedItems.add(dialog)
        }
        notifyDataSetChanged()
    }

    fun toggleOneItem(dialog: QBChatDialog) {
        if (selectedItems.contains(dialog)) {
            selectedItems.remove(dialog)
        } else {
            selectedItems.clear()
            selectedItems.add(dialog)
        }
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView = view.text_dialog_name
        val dialogImageView = view.image_dialog_icon
        val rootLayout = view.root
    }

    interface OnItemClickListener {
        fun onShortClick(dialog: QBChatDialog)

        fun onLongClick(dialog: QBChatDialog)
    }
}