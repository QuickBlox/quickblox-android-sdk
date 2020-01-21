package com.quickblox.sample.chat.kotlin.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.quickblox.sample.chat.kotlin.R
import com.quickblox.users.model.QBUser


class CheckboxUsersAdapter(context: Context, users: List<QBUser>) : UsersAdapter(context, users as MutableList<QBUser>) {

    private val initiallySelectedUsers: MutableList<Int> = ArrayList()
    private val _selectedUsers: MutableSet<QBUser> = HashSet()
    val selectedUsers: ArrayList<QBUser>
        get() = ArrayList<QBUser>(_selectedUsers)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val user = getItem(position)
        val holder = view.tag as UsersAdapter.ViewHolder

        holder.userCheckBox.visibility = View.VISIBLE
        val containsUser = _selectedUsers.contains(user)
        holder.userCheckBox.isChecked = containsUser

        if (containsUser) {
            holder.rootLayout.setBackgroundColor(context.resources.getColor(R.color.selected_list_item_color))
        } else {
            holder.rootLayout.setBackgroundColor(context.resources.getColor(android.R.color.transparent))
        }

        return view
    }

    fun onItemClicked(position: Int, convertView: View?, parent: ViewGroup) {
        val user = getItem(position)
        val holder = convertView?.tag as UsersAdapter.ViewHolder

        if (!isAvailableForSelection(user)) {
            return
        }

        holder.userCheckBox.isChecked = !holder.userCheckBox.isChecked
        if (holder.userCheckBox.isChecked) {
            _selectedUsers.add(user)
        } else {
            _selectedUsers.remove(user)
        }
        notifyDataSetChanged()
    }

    override fun getItem(position: Int): QBUser {
        return userList[position]
    }

    override fun isAvailableForSelection(user: QBUser): Boolean {
        return super.isAvailableForSelection(user) && !initiallySelectedUsers.contains(user.id)
    }
}