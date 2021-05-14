package com.forest.forestchat.ui.chats

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.forest.forestchat.databinding.NavigationChatsBinding

class ChatsNavigationView : ConstraintLayout {

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    private val binding: NavigationChatsBinding

    init {
        val layoutInflater = LayoutInflater.from(context)
        binding = NavigationChatsBinding.inflate(layoutInflater, this)
    }

}