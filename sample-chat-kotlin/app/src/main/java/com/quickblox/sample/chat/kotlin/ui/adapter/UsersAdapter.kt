package com.quickblox.sample.chat.kotlin.ui.adapter

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.quickblox.chat.QBChatService
import com.quickblox.sample.chat.kotlin.R
import com.quickblox.sample.chat.kotlin.utils.getColorCircleDrawable
import com.quickblox.users.model.QBUser


open class UsersAdapter(val context: Context, val userList: MutableList<QBUser>) : BaseAdapter() {
    private var currentUser: QBUser? = QBChatService.getInstance().user

    fun addNewList(users: List<QBUser>) {
        userList.clear()
        userList.addAll(users)

        for (user in users) {
            if (isUserMe(user)) {
                userList.remove(user)
            }
        }
        notifyDataSetChanged()
    }

    fun addUsers(users: List<QBUser>) {
        users.forEach {
            if (!userList.contains(it)) {
                userList.add(it)
            }
        }
        notifyDataSetChanged()
    }

    fun removeUsers(users: List<QBUser>) {
        for (user in users) {
            userList.remove(user)
        }
        notifyDataSetChanged()
    }

    fun clearList() {
        userList.clear()
        notifyDataSetChanged()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val holder: ViewHolder
        var modifiedView = convertView
        val user = getItem(position)

        if (modifiedView == null) {
            modifiedView = LayoutInflater.from(context).inflate(R.layout.list_item_user, parent, false) as View
            holder = ViewHolder()
            holder.rootLayout = modifiedView.findViewById(R.id.item_root_layout)
            holder.userImageView = modifiedView.findViewById(R.id.image_user)
            holder.loginTextView = modifiedView.findViewById(R.id.text_user_login)
            holder.userCheckBox = modifiedView.findViewById(R.id.checkbox_user)
            holder.userAvatarTitle = modifiedView.findViewById(R.id.text_user_avatar_title)
            modifiedView.tag = holder
        } else {
            holder = modifiedView.tag as ViewHolder
        }

        val username = if (TextUtils.isEmpty(user.fullName)) {
            user.login
        } else {
            user.fullName
        }

        if (isUserMe(user)) {
            holder.loginTextView.text = context.getString(R.string.placeholder_username_you, username)
        } else {
            holder.loginTextView.text = username
        }

        if (isAvailableForSelection(user)) {
            holder.loginTextView.setTextColor(context.resources.getColor(R.color.text_color_black))
        } else {
            holder.loginTextView.setTextColor(context.resources.getColor(R.color.text_color_medium_grey))
        }

        holder.userImageView.setBackgroundDrawable(getColorCircleDrawable(user.id.hashCode()))
        holder.userCheckBox.visibility = View.GONE

        if (!TextUtils.isEmpty(user.fullName)) {
            val avatarTitle = user.fullName.get(0).toString().toUpperCase()
            holder.userAvatarTitle.text = avatarTitle
        }

        return modifiedView
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return userList.size
    }

    override fun getItem(position: Int): QBUser {
        return userList[position]
    }

    private fun isUserMe(user: QBUser): Boolean {
        return currentUser?.id == user.id
    }

    protected open fun isAvailableForSelection(user: QBUser): Boolean {
        return currentUser?.id != user.id
    }

    protected class ViewHolder {
        lateinit var rootLayout: LinearLayout
        lateinit var userImageView: ImageView
        lateinit var loginTextView: TextView
        lateinit var userCheckBox: CheckBox
        lateinit var userAvatarTitle: TextView
    }
}