package com.quickblox.sample.conference.kotlin.presentation.screens.createchat.namechat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.FragmentChatNameBinding
import com.quickblox.sample.conference.kotlin.presentation.screens.base.BaseFragment
import com.quickblox.sample.conference.kotlin.presentation.utils.SimpleTextWatcher
import com.quickblox.sample.conference.kotlin.presentation.utils.isValidChatName

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class ChatNameFragment : BaseFragment<FragmentChatNameBinding>() {
    private var createDialogListener: CreateDialogListener? = null

    companion object {
        val TAG = ChatNameFragment::class.java.simpleName

        fun newInstance(createDialogListener: CreateDialogListener): ChatNameFragment {
            val args = Bundle()
            val fragment = ChatNameFragment()
            fragment.arguments = args
            fragment.setCreateDialogListener(createDialogListener)
            return fragment
        }
    }

    override fun getViewBinding(inflater: LayoutInflater, container: ViewGroup?): FragmentChatNameBinding {
        return FragmentChatNameBinding.inflate(inflater, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        binding?.toolbar?.inflateMenu(R.menu.menu_chat_name)
        binding?.toolbar?.menu?.getItem(0)?.isVisible = false
        binding?.flBack?.setOnClickListener {
            activity?.onBackPressed()
        }

        binding?.toolbar?.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.finishCreate -> {
                    createDialogListener?.createDialog(binding?.etChatName?.text.toString())
                }
            }
            return@setOnMenuItemClickListener true
        }

        binding?.etChatName?.addTextChangedListener(object : SimpleTextWatcher() {
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val text = s.toString().replace("  ", " ")
                if (binding?.etChatName?.text.toString() != text) {
                    binding?.etChatName?.setText(text)
                    binding?.etChatName?.setSelection(text.length)
                }
                validateField()
            }
        })

        binding?.ivClear?.setOnClickListener {
            binding?.etChatName?.setText("")
        }
    }

    private fun setCreateDialogListener(createDialogListener: CreateDialogListener) {
        this.createDialogListener = createDialogListener
    }

    private fun validateField() {
        binding?.toolbar?.menu?.getItem(0)?.isVisible = binding?.etChatName.isValidChatName()
        binding?.tvNameHint?.visibility = if (binding?.etChatName.isValidChatName()) View.GONE else View.VISIBLE
    }

    interface CreateDialogListener {
        fun createDialog(chatName: String)
    }
}