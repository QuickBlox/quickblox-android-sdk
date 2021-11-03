package com.quickblox.sample.conference.kotlin.presentation.screens.call

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import com.quickblox.sample.conference.kotlin.R
import com.quickblox.sample.conference.kotlin.databinding.CustomCoverstionLayoutBinding
import com.quickblox.sample.conference.kotlin.domain.call.entities.CallEntity
import java.util.*

/*
 * Created by Injoit in 2021-09-30.
 * Copyright © 2021 Quickblox. All rights reserved.
 */
class CustomLinearLayout : LinearLayout, ConversationItem.ConversationItemListener {
    private lateinit var binding: CustomCoverstionLayoutBinding
    private var callEntities: SortedSet<CallEntity>? = null
    private var conversationItemListener: ConversationItemListener? = null

    constructor(context: Context) : super(context) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init(context)
    }

    private fun init(context: Context) {
        val rootView: View = inflate(context, R.layout.custom_coverstion_layout, this)
        binding = CustomCoverstionLayoutBinding.bind(rootView)
        this.orientation = VERTICAL
    }

    fun setCallEntities(callEntities: SortedSet<CallEntity>) {
        this.callEntities = callEntities
    }

    fun setClickListener(conversationItemListener: ConversationItemListener) {
        this.conversationItemListener = conversationItemListener
    }

    fun updateViews(height: Int, width: Int) {
        removeAllViews()

        when (callEntities?.size) {
            1 -> {
                val callEntity = callEntities?.first()
                val view = ConversationItem(context)
                callEntity?.let { view.setView(it) }
                view.setClickListener(this)
                view.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                this.addView(view)
            }
            2 -> {
                callEntities?.forEach { callEntity ->
                    val view = ConversationItem(context)
                    view.setView(callEntity)
                    view.setClickListener(this)
                    view.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height / 2)
                    this.addView(view)
                }
            }
            3 -> {
                callEntities?.let { callEntities ->
                    val listCallEntities = callEntities.toList()
                    this.addView(expandableLayout(width, height / 2, listCallEntities.get(0), listCallEntities.get(1)))
                    val view = ConversationItem(context)
                    listCallEntities.get(2)?.let { view.setView(it) }
                    view.setClickListener(this)
                    view.layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height / 2)
                    this.addView(view)
                }
            }
            else -> {
                callEntities?.let { callEntities ->
                    val listCallEntities = callEntities.toList()
                    val displayScheme = calcViewTable(callEntities.size)
                    var triple = displayScheme.first
                    var dual = displayScheme.second
                    val countRows = triple + dual

                    var tmpIndex = 0
                    for (index in 0 .. listCallEntities.size step 3) {
                        if (triple-- > 0) {
                            this.addView(expandableLayout(width, height / countRows, listCallEntities[index], listCallEntities[index + 1], listCallEntities[index + 2]))
                        } else {
                            break
                        }
                        tmpIndex = index + 3
                    }

                    for (index in tmpIndex .. listCallEntities.size step 2) {
                        if (dual-- > 0) {
                            this.addView(expandableLayout(width, height / countRows, listCallEntities[index], listCallEntities[index + 1]))
                        }
                    }
                }
            }
        }
    }

    private fun expandableLayout(width: Int, height: Int, vararg entities: CallEntity?): LinearLayout {
        val expandableLayout = LinearLayout(context)
        expandableLayout.layoutParams = LayoutParams(width, height)
        expandableLayout.orientation = HORIZONTAL
        expandableLayout.removeAllViews()

        entities.forEach { callEntity ->
            callEntity?.let {
                val view = ConversationItem(context)
                view.setView(it)
                view.setClickListener(this)
                view.layoutParams = FrameLayout.LayoutParams(width / entities.size, ViewGroup.LayoutParams.MATCH_PARENT)
                expandableLayout.addView(view)
            }
        }
        return expandableLayout
    }

    private fun calcViewTable(opponentsQuantity: Int): Pair<Int, Int> {
        var rowsThreeUsers = 0
        var rowsTwoUsers = 0
        when (opponentsQuantity % 3) {
            0 -> rowsThreeUsers = opponentsQuantity / 3
            1 -> {
                rowsTwoUsers = 2
                rowsThreeUsers = (opponentsQuantity - 2) / 3
            }
            2 -> {
                rowsTwoUsers = 1
                rowsThreeUsers = (opponentsQuantity - 1) / 3
            }
        }
        return Pair(rowsThreeUsers, rowsTwoUsers)
    }

    override fun onItemClick(callEntity: CallEntity) {
        conversationItemListener?.onItemClick(callEntity)
    }

    interface ConversationItemListener {
        fun onItemClick(callEntity: CallEntity)
    }
}