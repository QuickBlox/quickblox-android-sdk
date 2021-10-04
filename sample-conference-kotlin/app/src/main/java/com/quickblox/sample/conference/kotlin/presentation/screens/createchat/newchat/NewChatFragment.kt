package com.quickblox.sample.conference.kotlin.presentation.screens.createchat.newchat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.collection.ArraySet
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.FragmentNewChatBinding
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseFragment
import com.quickblox.users.model.QBUser
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
@AndroidEntryPoint
class NewChatFragment : BaseFragment<FragmentNewChatBinding>() {
    private val viewModel: NewChatViewModel by activityViewModels()
    private var newChatListener: NewChatListener? = null
    private var usersSearchAdapter: UsersSearchAdapter? = null
    private val onScrollListenerImpl = OnScrollListenerImpl()

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentNewChatBinding {
        return FragmentNewChatBinding.inflate(inflater, container, false)
    }

    companion object {
        val TAG = NewChatFragment::class.java.simpleName

        fun newInstance(newChatListener: NewChatListener): NewChatFragment {
            val args = Bundle()
            val fragment = NewChatFragment()
            fragment.setListener(newChatListener)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initAdapter()
        initScrollListeners()

        viewModel.liveData.observe(viewLifecycleOwner, { result ->
            result?.let { (state, data) ->
                when (state) {
                    ViewState.PROGRESS -> {
                        showProgress()
                    }
                    ViewState.ERROR -> {
                        hideProgress()
                        Toast.makeText(context, "$data", Toast.LENGTH_SHORT).show()
                    }
                    ViewState.SHOW_USERS -> {
                        hideProgress()

                        if (viewModel.users.isEmpty()) {
                            binding?.tvPlaceHolder?.visibility = View.VISIBLE
                            binding?.rvUsers?.visibility = View.GONE
                        } else {
                            binding?.tvPlaceHolder?.visibility = View.GONE
                            binding?.rvUsers?.visibility = View.VISIBLE
                        }
                        usersSearchAdapter?.notifyDataSetChanged()
                    }
                }
            }
        })
    }

    private fun initView() {
        binding?.toolbar?.inflateMenu(R.menu.menu_new_chat)
        binding?.toolbar?.menu?.getItem(0)?.isVisible = false
        binding?.toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.createChat -> {
                    newChatListener?.onSelectedUsers(viewModel.selectedUsers)
                }
            }
            return@setOnMenuItemClickListener true
        }

        binding?.flBack?.setOnClickListener {
            activity?.onBackPressed()
        }

        binding?.searchView?.setOnQueryTextListener(SearchQueryListener())
    }

    override fun onResume() {
        super.onResume()
        changeToolbar()
    }

    private fun changeToolbar() {
        val selectedCounter = viewModel.selectedUsers.size
        binding?.toolbar?.menu?.getItem(0)?.isVisible = selectedCounter > 0

        if (selectedCounter == 0) {
            binding?.tvSubTitle?.visibility = View.GONE
            return
        } else {
            binding?.tvSubTitle?.visibility = View.VISIBLE
        }

        binding?.tvSubTitle?.text = if (selectedCounter > 1) {
            getString(R.string.subtitle_new_chat_users, selectedCounter.toString())
        } else {
            getString(R.string.subtitle_new_chat_user, selectedCounter.toString())
        }
    }

    private fun setListener(newChatListener: NewChatListener) {
        this.newChatListener = newChatListener
    }

    private fun initAdapter() {
        usersSearchAdapter = UsersSearchAdapter(viewModel.users, object : UsersSearchAdapter.UsersAdapterListener {
            override fun onSelected() {
                changeToolbar()
            }
        }, viewModel.selectedUsers)
        binding?.rvUsers?.adapter = usersSearchAdapter
    }

    private fun initScrollListeners() {
        val mLayoutManager = LinearLayoutManager(context)
        binding?.rvUsers?.layoutManager = mLayoutManager
        binding?.rvUsers?.addOnScrollListener(onScrollListenerImpl)
    }

    private fun showProgress() {
        binding?.progressBar?.visibility = View.VISIBLE
    }

    private fun hideProgress() {
        onScrollListenerImpl.isLoad = false
        binding?.progressBar?.visibility = View.GONE
    }

    interface NewChatListener {
        fun onSelectedUsers(selectedUsers: ArraySet<QBUser>)
    }

    private inner class SearchQueryListener : SearchView.OnQueryTextListener {
        override fun onQueryTextSubmit(query: String): Boolean {
            return false
        }

        override fun onQueryTextChange(newText: String): Boolean {
            viewModel.onQueryTextChange(newText)
            return false
        }
    }

    inner class OnScrollListenerImpl : RecyclerView.OnScrollListener() {
        var isLoad: Boolean = false

        override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
            super.onScrollStateChanged(recyclerView, newState)
            if (!recyclerView.canScrollVertically(1) || isLoad) {
                isLoad = true
                viewModel.loadUsers()
            }
        }
    }
}