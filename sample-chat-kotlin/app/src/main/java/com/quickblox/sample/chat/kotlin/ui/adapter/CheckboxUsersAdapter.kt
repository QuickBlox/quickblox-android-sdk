package com.quickblox.sample.chat.kotlin.ui.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.quickblox.users.model.QBUser


class CheckboxUsersAdapter(context: Context, users: List<QBUser>) : UsersAdapter(context, users as MutableList<QBUser>) {

    private val initiallySelectedUsers: MutableList<Int> = ArrayList()
    private val _selectedUsers: MutableSet<QBUser> = HashSet()
    val selectedUsers: ArrayList<*>
        get() = ArrayList<QBUser>(_selectedUsers)

    fun addSelectedUsers(userIdList: List<Int>) {
        for (user in userList) {
            for (userId in userIdList) {
                if (user.id == userId) {
                    _selectedUsers.add(user)
                    initiallySelectedUsers.add(user.id)
                    break
                }
            }
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)

        val user = getItem(position)
        val holder = view.tag as UsersAdapter.ViewHolder

        view.setOnClickListener(View.OnClickListener {
            if (!isAvailableForSelection(user)) {
                return@OnClickListener
            }

            holder.userCheckBox.isChecked = !holder.userCheckBox.isChecked
            if (holder.userCheckBox.isChecked) {
                _selectedUsers.add(user)
            } else {
                _selectedUsers.remove(user)
            }
        })

        holder.userCheckBox.visibility = View.VISIBLE
        val containsUser = _selectedUsers.contains(user)
        holder.userCheckBox.isChecked = containsUser

        if (isUserMe(user)) {
            holder.userCheckBox.isChecked = true
        }

        return view
    }

    override fun getItem(position: Int): QBUser {
        return userList[position]
    }

    override fun isAvailableForSelection(user: QBUser): Boolean {
        return super.isAvailableForSelection(user) && !initiallySelectedUsers.contains(user.id)
    }
}