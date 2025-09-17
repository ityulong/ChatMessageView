package com.github.bassaer.chatmessageview.view

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View

import com.github.bassaer.chatmessageview.model.Attribute
import com.github.bassaer.chatmessageview.model.Message
import com.github.bassaer.chatmessageview.util.MessageDateComparator
import com.github.bassaer.chatmessageview.util.TimeUtils

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import java.lang.ref.WeakReference
import java.util.*

import kotlin.collections.ArrayList

/**
 * Simple chat view
 * Created by nakayama on 2016/08/08.
 */
class MessageView : RecyclerView, View.OnFocusChangeListener {

    /**
     * All contents such as right message, left message, date label
     */
    private val chatList: MutableList<Any> = ArrayList()
    /**
     * Only messages
     */
    val messageList: MutableList<Message> = ArrayList()

    private lateinit var messageAdapter: MessageAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var keyboardAppearListener: OnKeyboardAppearListener

    /**
     * MessageView is refreshed at this time
     */
    private var refreshInterval: Long = 60000

    private var attribute: Attribute

    interface OnKeyboardAppearListener {
        fun onKeyboardAppeared(hasChanged: Boolean)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        attribute = Attribute(context, attrs)
        initialize(context)
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        attribute = Attribute(context, attrs)
        initialize(context)
    }


    fun init(list: List<Message>) {
        messageList.clear()
        messageList.addAll(list)
        refresh()
    }

    fun init(attribute: Attribute) {
        this.attribute = attribute
        if (::messageAdapter.isInitialized) {
            messageAdapter.setAttribute(attribute)
        }
    }

    /**
     * Initialize list
     */
    fun init() {
        if (!::messageAdapter.isInitialized) {
            initialize(context)
        } else {
            messageAdapter.notifyDataSetChanged()
        }
    }

    /**
     * Set new message and refresh
     * @param message new message
     */
    fun setMessage(message: Message) {
        addMessage(message)
        refresh()
        messageAdapter.notifyDataSetChanged()
    }

    /**
    * Dynamically update message status and refresh, updating the status icon
    * @param message message to update
    * @param status new status to be applied
    */
    fun updateMessageStatus(message: Message, status: Int)
    {
        val indexOfMessage = messageList.indexOf(message)
        val messageToUpdate = messageList[indexOfMessage]
        messageToUpdate.status = status
        notifyItemChanged(messageToUpdate)
    }

    fun updateMessageTransfer(message: Message, state: Message.TransferState, progress: Int) {
        val indexOfMessage = messageList.indexOf(message)
        if (indexOfMessage < 0) {
            return
        }
        val messageToUpdate = messageList[indexOfMessage]
        messageToUpdate.transferState = state
        messageToUpdate.transferProgress = progress.coerceIn(0, 100)
        notifyItemChanged(messageToUpdate)
    }

    /**
     * Add message to chat list and message list.
     * Set date text before set message if sent at the different day.
     * @param message new message
     */
    private fun addMessage(message: Message) {
        messageList.add(message)
        if (messageList.size == 1) {
            chatList.add(message.dateSeparateText)
            chatList.add(message)
            return
        }
        val prevMessage = messageList[messageList.size - 2]
        if (!TimeUtils.isSameDay(prevMessage.sendTime, message.sendTime)) {
            chatList.add(message.dateSeparateText)
        }
        chatList.add(message)
    }

    private fun refresh() {
        sortMessages(messageList)
        chatList.clear()
        chatList.addAll(insertDateSeparator(messageList))
        messageAdapter.notifyDataSetChanged()
    }

    fun remove(message: Message) {
        messageList.remove(message)
        refresh()
    }

    fun removeAll() {
        messageList.clear()
        refresh()
    }

    private fun insertDateSeparator(list: List<Message>): List<Any> {
        val result = ArrayList<Any>()
        if (list.isEmpty()) {
            return result
        }
        result.add(list[0].dateSeparateText)
        result.add(list[0])
        if (list.size < 2) {
            return result
        }
        for (i in 1 until list.size) {
            val prevMessage = list[i - 1]
            val currMessage = list[i]
            if (!TimeUtils.isSameDay(prevMessage.sendTime, currMessage.sendTime)) {
                result.add(currMessage.dateSeparateText)
            }
            result.add(currMessage)
        }
        return result
    }

    /**
     * Sort messages
     */
    private fun sortMessages(list: List<Message>?) {
        val dateComparator = MessageDateComparator()
        if (list != null) {
            Collections.sort(list, dateComparator)
        }
    }

    fun setOnKeyboardAppearListener(listener: OnKeyboardAppearListener) {
        keyboardAppearListener = listener
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)

        // if ListView became smaller
        if (::keyboardAppearListener.isInitialized && height < oldHeight) {
            keyboardAppearListener.onKeyboardAppeared(true)
        }
    }

    override fun onFocusChange(view: View, hasFocus: Boolean) {
        if (hasFocus) {
            //Scroll to end
            scrollToEnd()
        }
    }

    fun scrollToEnd() {
        if (!::messageAdapter.isInitialized) {
            return
        }
        val targetPosition = messageAdapter.itemCount - 1
        if (targetPosition >= 0) {
            smoothScrollToPosition(targetPosition)
        }
    }

    fun setLeftBubbleColor(color: Int) {
        messageAdapter.setLeftBubbleColor(color)
    }

    fun setRightBubbleColor(color: Int) {
        messageAdapter.setRightBubbleColor(color)
    }

    fun setUsernameTextColor(color: Int) {
        messageAdapter.setUsernameTextColor(color)
    }

    fun setSendTimeTextColor(color: Int) {
        messageAdapter.setSendTimeTextColor(color)
    }

    fun setMessageStatusColor(color: Int) {
        messageAdapter.setStatusColor(color)
    }

    fun setDateSeparatorTextColor(color: Int) {
        messageAdapter.setDateSeparatorColor(color)
    }

    fun setRightMessageTextColor(color: Int) {
        messageAdapter.setRightMessageTextColor(color)
    }

    fun setLeftMessageTextColor(color: Int) {
        messageAdapter.setLeftMessageTextColor(color)
    }

    fun setOnBubbleClickListener(listener: Message.OnBubbleClickListener) {
        messageAdapter.setOnBubbleClickListener(listener)
    }

    fun setOnBubbleLongClickListener(listener: Message.OnBubbleLongClickListener) {
        messageAdapter.setOnBubbleLongClickListener(listener)
    }

    fun setOnIconClickListener(listener: Message.OnIconClickListener) {
        messageAdapter.setOnIconClickListener(listener)
    }

    fun setOnIconLongClickListener(listener: Message.OnIconLongClickListener) {
        messageAdapter.setOnIconLongClickListener(listener)
    }

    fun setMessageMarginTop(px: Int) {
        messageAdapter.setMessageTopMargin(px)
    }

    fun setMessageMarginBottom(px: Int) {
        messageAdapter.setMessageBottomMargin(px)
    }

    fun setRefreshInterval(refreshInterval: Long) {
        this.refreshInterval = refreshInterval
    }

    fun setMessageFontSize(size: Float) {
        attribute.messageFontSize = size
        setAttribute()
    }

    fun setUsernameFontSize(size: Float) {
        attribute.usernameFontSize = size
        setAttribute()
    }

    fun setTimeLabelFontSize(size: Float) {
        attribute.timeLabelFontSize = size
        setAttribute()
    }

    fun setMessageMaxWidth(width: Int) {
        attribute.messageMaxWidth = width
        setAttribute()
    }

    fun setDateSeparatorFontSize(size: Float) {
        attribute.dateSeparatorFontSize = size
        setAttribute()
    }

    private fun setAttribute() {
        messageAdapter.setAttribute(attribute)
    }

    private fun initialize(context: Context) {
        linearLayoutManager = LinearLayoutManager(context)
        layoutManager = linearLayoutManager
        messageAdapter = MessageAdapter(context, chatList, attribute)
        adapter = messageAdapter
        val weakMessageAdapter: WeakReference<MessageAdapter> = WeakReference(messageAdapter)
        val handler = Handler(Looper.getMainLooper())
        val refreshTimer = Timer(true)
        refreshTimer.schedule(object : TimerTask() {
            override fun run() {
                handler.post { weakMessageAdapter.get()?.notifyDataSetChanged() }
            }
        }, 1000, refreshInterval)
        setOnFocusChangeListener(this)
    }

    private fun notifyItemChanged(message: Message) {
        if (!::messageAdapter.isInitialized) {
            return
        }
        val adapterIndex = chatList.indexOf(message)
        if (adapterIndex >= 0) {
            messageAdapter.notifyItemChanged(adapterIndex)
        } else {
            messageAdapter.notifyDataSetChanged()
        }
    }
}
