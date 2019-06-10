package com.quickblox.sample.videochat.conference.kotlin.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.sample.videochat.conference.kotlin.R
import com.quickblox.sample.videochat.conference.kotlin.utils.getColor
import com.quickblox.sample.videochat.conference.kotlin.utils.getColorCircleDrawable
import com.quickblox.users.model.QBUser
import kotlinx.android.synthetic.main.list_item_user.view.*


class CheckboxUsersAdapter(val context: Context, private var usersList: List<QBUser>,
                           private val currentUser: QBUser?) : RecyclerView.Adapter<CheckboxUsersAdapter.ViewHolder>() {

    private val selectedUsers: MutableList<QBUser> = ArrayList()

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = usersList[position]

        if (isUserMe(user)) {
            holder.loginTextView.text = context.getString(R.string.placeholder_username_you, currentUser?.fullName)
            selectedUsers.add(currentUser as QBUser)
        } else {
            holder.loginTextView.text = user.fullName
        }

        if (isAvailableForSelection(user)) {
            holder.loginTextView.setTextColor(getColor(R.color.text_color_black))
        } else {
            holder.loginTextView.setTextColor(getColor(R.color.text_color_medium_grey))
        }

        holder.userImageView.setBackgroundDrawable(getColorCircleDrawable(position))
        holder.userCheckBox.isChecked = selectedUsers.contains(user)

        holder.rootLayout.setOnClickListener {
            if (isAvailableForSelection(user)) {
                holder.userCheckBox.isChecked = !holder.userCheckBox.isChecked
                if (holder.userCheckBox.isChecked) {
                    selectedUsers.add(user)
                } else {
                    selectedUsers.remove(user)
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return usersList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(context).inflate(R.layout.list_item_user, parent, false))
    }

    private fun isUserMe(user: QBUser): Boolean {
        return currentUser != null && currentUser.id == user.id
    }

    private fun isAvailableForSelection(user: QBUser): Boolean {
        return currentUser == null || currentUser.id != user.id
    }

    fun getSelectedUsers(): List<QBUser> {
        return selectedUsers
    }

    fun updateUsers(usersList: List<QBUser>) {
        this.usersList = usersList
        selectedUsers.clear()
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userImageView = view.image_user
        val loginTextView = view.text_user_login
        val userCheckBox = view.checkbox_user
        val rootLayout = view.linear_layout_root
    }
}