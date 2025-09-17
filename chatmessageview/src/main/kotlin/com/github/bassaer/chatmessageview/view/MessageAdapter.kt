package com.github.bassaer.chatmessageview.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.recyclerview.widget.RecyclerView

import com.github.bassaer.chatmessageview.R
import com.github.bassaer.chatmessageview.model.Attribute
import com.github.bassaer.chatmessageview.model.Message

import de.hdodenhof.circleimageview.CircleImageView

/**
 * RecyclerView adapter that renders chat messages and date separators.
 */
class MessageAdapter(
    private val context: Context,
    private val objects: List<Any>,
    private var attribute: Attribute
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)

    private var iconClickListener: Message.OnIconClickListener? = null
    private var bubbleClickListener: Message.OnBubbleClickListener? = null
    private var iconLongClickListener: Message.OnIconLongClickListener? = null
    private var bubbleLongClickListener: Message.OnBubbleLongClickListener? = null

    private var usernameTextColor = ContextCompat.getColor(context, R.color.blueGray500)
    private var sendTimeTextColor = ContextCompat.getColor(context, R.color.blueGray500)
    private var dateLabelColor = ContextCompat.getColor(context, R.color.blueGray500)
    private var rightMessageTextColor = Color.WHITE
    private var leftMessageTextColor = Color.BLACK
    private var leftBubbleColor: Int = ContextCompat.getColor(context, R.color.default_left_bubble_color)
    private var rightBubbleColor: Int = ContextCompat.getColor(context, R.color.default_right_bubble_color)
    private var statusColor = ContextCompat.getColor(context, R.color.blueGray500)

    /**
     * Default message item margin top
     */
    private var messageTopMargin = 5

    /**
     * Default message item margin bottom
     */
    private var messageBottomMargin = 5

    companion object {
        private const val TYPE_DATE = 0
        private const val TYPE_LEFT = 1
        private const val TYPE_RIGHT = 2
    }

    override fun getItemCount(): Int = objects.size

    override fun getItemViewType(position: Int): Int {
        val item = objects[position]
        return when (item) {
            is String -> TYPE_DATE
            is Message -> if (item.isRight) TYPE_RIGHT else TYPE_LEFT
            else -> TYPE_DATE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_DATE -> {
                val view = layoutInflater.inflate(R.layout.date_cell, parent, false)
                DateViewHolder(view)
            }
            TYPE_RIGHT -> {
                val view = layoutInflater.inflate(R.layout.message_view_right, parent, false)
                MessageViewHolder(view)
            }
            else -> {
                val view = layoutInflater.inflate(R.layout.message_view_left, parent, false)
                MessageViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = objects[position]
        if (holder is DateViewHolder && item is String) {
            bindDate(holder, item)
        } else if (holder is MessageViewHolder && item is Message) {
            bindMessage(holder, item, position)
        }
    }

    private fun bindDate(holder: DateViewHolder, dateText: String) {
        holder.dateLabelText.text = dateText
        holder.dateLabelText.setTextColor(dateLabelColor)
        holder.dateLabelText.setTextSize(TypedValue.COMPLEX_UNIT_PX, attribute.dateSeparatorFontSize)
    }

    @SuppressLint("InflateParams")
    private fun bindMessage(holder: MessageViewHolder, message: Message, position: Int) {
        holder.clearMessageBindings()

        if (position > 0) {
            val previous = objects[position - 1]
            if (previous is Message && previous.user.getId() == message.user.getId()) {
                message.iconVisibility = false
                message.usernameVisibility = false
            }
        }

        val user = message.user

        holder.iconContainer.removeAllViews()
        holder.usernameContainer.removeAllViews()
        holder.statusContainer.removeAllViews()
        holder.mainMessageContainer.removeAllViews()
        holder.transferContainer?.removeAllViews()
        holder.transferContainer?.visibility = View.GONE

        if (user.getName() != null && message.usernameVisibility) {
            val usernameLayout = layoutInflater.inflate(
                if (message.isRight) R.layout.user_name_right else R.layout.user_name_left,
                holder.usernameContainer,
                true
            )
            holder.username = usernameLayout.findViewById(R.id.message_user_name)
            holder.username?.text = user.getName()
            holder.username?.setTextColor(usernameTextColor)
            holder.username?.setTextSize(TypedValue.COMPLEX_UNIT_PX, attribute.usernameFontSize)
        }

        if (!message.isIconHided) {
            val iconLayout = layoutInflater.inflate(
                if (message.isRight) R.layout.user_icon_right else R.layout.user_icon_left,
                holder.iconContainer,
                true
            )
            holder.icon = iconLayout.findViewById(R.id.user_icon)
            if (message.iconVisibility) {
                user.getIcon()?.let { holder.icon?.setImageBitmap(it) }
            } else {
                holder.icon?.visibility = View.INVISIBLE
            }
        }

        when (message.statusStyle) {
            Message.STATUS_ICON, Message.STATUS_ICON_RIGHT_ONLY, Message.STATUS_ICON_LEFT_ONLY -> {
                val statusLayout = layoutInflater.inflate(R.layout.message_status_icon, holder.statusContainer, true)
                holder.statusIcon = statusLayout.findViewById(R.id.status_icon_image_view)
                holder.statusIcon?.setImageDrawable(message.statusIcon)
                setColorDrawable(statusColor, holder.statusIcon?.drawable)
            }
            Message.STATUS_TEXT, Message.STATUS_TEXT_RIGHT_ONLY, Message.STATUS_TEXT_LEFT_ONLY -> {
                val statusLayout = layoutInflater.inflate(R.layout.message_status_text, holder.statusContainer, true)
                holder.statusText = statusLayout.findViewById(R.id.status_text_view)
                holder.statusText?.text = message.statusText
                holder.statusText?.setTextColor(statusColor)
            }
        }

        when (message.type) {
            Message.Type.PICTURE -> {
                val pictureLayout = layoutInflater.inflate(
                    if (message.isRight) R.layout.message_picture_right else R.layout.message_picture_left,
                    holder.mainMessageContainer,
                    true
                )
                holder.messagePicture = pictureLayout.findViewById(R.id.message_picture)
                holder.messagePicture?.setImageBitmap(message.picture)
            }
            Message.Type.LINK -> {
                val linkLayout = layoutInflater.inflate(
                    if (message.isRight) R.layout.message_link_right else R.layout.message_link_left,
                    holder.mainMessageContainer,
                    true
                )
                holder.messageLink = linkLayout.findViewById(R.id.message_link)
                holder.messageLink?.text = message.text
                setBubbleColor(holder.messageLink, message.isRight)
                holder.messageLink?.setTextColor(if (message.isRight) rightMessageTextColor else leftMessageTextColor)
            }
            Message.Type.VOICE -> {
                val voiceLayout = layoutInflater.inflate(
                    if (message.isRight) R.layout.message_voice_right else R.layout.message_voice_left,
                    holder.mainMessageContainer,
                    true
                )
                holder.voiceDuration = voiceLayout.findViewById(R.id.message_voice_duration)
                holder.voiceDuration?.text = context.getString(R.string.chat_voice_duration_seconds, message.voiceDuration)
                setBubbleColor(voiceLayout, message.isRight)
                holder.voiceDuration?.setTextColor(if (message.isRight) rightMessageTextColor else leftMessageTextColor)
            }
            Message.Type.VIDEO -> {
                val videoLayout = layoutInflater.inflate(
                    if (message.isRight) R.layout.message_video_right else R.layout.message_video_left,
                    holder.mainMessageContainer,
                    true
                )
                holder.videoThumbnail = videoLayout.findViewById(R.id.message_video_thumbnail)
                holder.videoThumbnail?.let { thumbnailView ->
                    if (message.videoThumbnail != null) {
                        thumbnailView.setImageBitmap(message.videoThumbnail)
                    } else {
                        thumbnailView.setImageResource(R.color.blueGray200)
                    }
                }
                setBubbleColor(videoLayout, message.isRight)
            }
            Message.Type.FILE -> {
                val fileLayout = layoutInflater.inflate(
                    if (message.isRight) R.layout.message_file_right else R.layout.message_file_left,
                    holder.mainMessageContainer,
                    true
                )
                holder.fileName = fileLayout.findViewById(R.id.message_file_name)
                holder.fileInfo = fileLayout.findViewById(R.id.message_file_info)
                holder.fileName?.text = message.fileName ?: context.getString(R.string.chat_file_unknown)
                holder.fileInfo?.apply {
                    text = message.fileInfo ?: ""
                    visibility = if (TextUtils.isEmpty(text)) View.GONE else View.VISIBLE
                }
                setBubbleColor(fileLayout, message.isRight)
                val messageTextColor = if (message.isRight) rightMessageTextColor else leftMessageTextColor
                holder.fileName?.setTextColor(messageTextColor)
                holder.fileInfo?.setTextColor(messageTextColor)
            }
            else -> {
                val textLayout = layoutInflater.inflate(
                    if (message.isRight) R.layout.message_text_right else R.layout.message_text_left,
                    holder.mainMessageContainer,
                    true
                )
                holder.messageText = textLayout.findViewById(R.id.message_text)
                holder.messageText?.setTextIsSelectable(attribute.isTextSelectable)
                holder.messageText?.text = message.text
                setBubbleColor(holder.messageText, message.isRight)
                holder.messageText?.setTextColor(if (message.isRight) rightMessageTextColor else leftMessageTextColor)
            }
        }

        holder.timeText.text = message.timeText
        holder.timeText.setTextColor(sendTimeTextColor)
        holder.timeText.setTextSize(TypedValue.COMPLEX_UNIT_PX, attribute.timeLabelFontSize)

        holder.itemView.setPadding(0, messageTopMargin, 0, messageBottomMargin)

        holder.mainMessageContainer.setOnClickListener { bubbleClickListener?.onClick(message) }
        holder.mainMessageContainer.setOnLongClickListener {
            bubbleLongClickListener?.onLongClick(message)
            true
        }

        if (message.iconVisibility && holder.icon != null) {
            holder.icon?.setOnClickListener { iconClickListener?.onIconClick(message) }
            holder.icon?.setOnLongClickListener {
                iconLongClickListener?.onIconLongClick(message)
                true
            }
        }

        holder.messageText?.setTextSize(TypedValue.COMPLEX_UNIT_PX, attribute.messageFontSize)
        holder.messageText?.maxWidth = attribute.messageMaxWidth
        holder.messageLink?.setTextSize(TypedValue.COMPLEX_UNIT_PX, attribute.messageFontSize)
        holder.messageLink?.maxWidth = attribute.messageMaxWidth
        holder.voiceDuration?.setTextSize(TypedValue.COMPLEX_UNIT_PX, attribute.messageFontSize)
        holder.fileName?.setTextSize(TypedValue.COMPLEX_UNIT_PX, attribute.messageFontSize)
        holder.fileInfo?.setTextSize(TypedValue.COMPLEX_UNIT_PX, attribute.messageFontSize * 0.9f)
        holder.fileName?.maxWidth = attribute.messageMaxWidth

        bindTransferStatus(holder, message)
    }

    private fun setBubbleColor(view: View?, isRight: Boolean) {
        view?.background?.let {
            setColorDrawable(if (isRight) rightBubbleColor else leftBubbleColor, it)
        }
    }

    private fun bindTransferStatus(holder: MessageViewHolder, message: Message) {
        val container = holder.transferContainer ?: return
        container.removeAllViews()

        when (message.transferState) {
            Message.TransferState.UPLOADING, Message.TransferState.DOWNLOADING -> {
                container.visibility = View.VISIBLE
                val statusLayout = layoutInflater.inflate(R.layout.message_transfer_status, container, true)
                val progressBar = statusLayout.findViewById<ProgressBar>(R.id.message_transfer_progress)
                val statusText = statusLayout.findViewById<TextView>(R.id.message_transfer_progress_text)
                progressBar.visibility = View.VISIBLE
                progressBar.progress = message.transferProgress
                val textRes = if (message.transferState == Message.TransferState.UPLOADING) {
                    R.string.chat_transfer_uploading
                } else {
                    R.string.chat_transfer_downloading
                }
                statusText.text = context.getString(textRes, message.transferProgress)
                statusText.setTextColor(sendTimeTextColor)
            }
            Message.TransferState.COMPLETED -> {
                container.visibility = View.VISIBLE
                val statusLayout = layoutInflater.inflate(R.layout.message_transfer_status, container, true)
                val progressBar = statusLayout.findViewById<ProgressBar>(R.id.message_transfer_progress)
                val statusText = statusLayout.findViewById<TextView>(R.id.message_transfer_progress_text)
                progressBar.visibility = View.GONE
                statusText.text = context.getString(R.string.chat_transfer_completed)
                statusText.setTextColor(sendTimeTextColor)
            }
            Message.TransferState.FAILED -> {
                container.visibility = View.VISIBLE
                val statusLayout = layoutInflater.inflate(R.layout.message_transfer_status, container, true)
                val progressBar = statusLayout.findViewById<ProgressBar>(R.id.message_transfer_progress)
                val statusText = statusLayout.findViewById<TextView>(R.id.message_transfer_progress_text)
                progressBar.visibility = View.GONE
                statusText.text = context.getString(R.string.chat_transfer_failed)
                statusText.setTextColor(sendTimeTextColor)
            }
            else -> {
                container.visibility = View.GONE
            }
        }
    }

    /**
     * Add color to drawable
     * @param color setting color
     * @param drawable which be set color
     */
    private fun setColorDrawable(color: Int, drawable: Drawable?) {
        if (drawable == null) {
            return
        }
        val colorStateList = ColorStateList.valueOf(color)
        val wrappedDrawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTintList(wrappedDrawable, colorStateList)
    }

    fun setLeftBubbleColor(color: Int) {
        leftBubbleColor = color
        notifyDataSetChanged()
    }

    fun setRightBubbleColor(color: Int) {
        rightBubbleColor = color
        notifyDataSetChanged()
    }

    fun setOnIconClickListener(onIconClickListener: Message.OnIconClickListener) {
        iconClickListener = onIconClickListener
    }

    fun setOnBubbleClickListener(onBubbleClickListener: Message.OnBubbleClickListener) {
        bubbleClickListener = onBubbleClickListener
    }

    fun setOnIconLongClickListener(onIconLongClickListener: Message.OnIconLongClickListener) {
        iconLongClickListener = onIconLongClickListener
    }

    fun setOnBubbleLongClickListener(onBubbleLongClickListener: Message.OnBubbleLongClickListener) {
        bubbleLongClickListener = onBubbleLongClickListener
    }

    fun setUsernameTextColor(usernameTextColor: Int) {
        this.usernameTextColor = usernameTextColor
        notifyDataSetChanged()
    }

    fun setSendTimeTextColor(sendTimeTextColor: Int) {
        this.sendTimeTextColor = sendTimeTextColor
        notifyDataSetChanged()
    }

    fun setDateSeparatorColor(dateSeparatorColor: Int) {
        this.dateLabelColor = dateSeparatorColor
        notifyDataSetChanged()
    }

    fun setRightMessageTextColor(rightMessageTextColor: Int) {
        this.rightMessageTextColor = rightMessageTextColor
        notifyDataSetChanged()
    }

    fun setLeftMessageTextColor(leftMessageTextColor: Int) {
        this.leftMessageTextColor = leftMessageTextColor
        notifyDataSetChanged()
    }

    fun setMessageTopMargin(messageTopMargin: Int) {
        this.messageTopMargin = messageTopMargin
    }

    fun setMessageBottomMargin(messageBottomMargin: Int) {
        this.messageBottomMargin = messageBottomMargin
    }

    fun setStatusColor(statusTextColor: Int) {
        statusColor = statusTextColor
        notifyDataSetChanged()
    }

    fun setAttribute(attribute: Attribute) {
        this.attribute = attribute
        notifyDataSetChanged()
    }

    internal inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var icon: CircleImageView? = null
        val iconContainer: FrameLayout = itemView.findViewById(R.id.userIconContainer)
        val mainMessageContainer: FrameLayout = itemView.findViewById(R.id.mainMessageContainer)
        val usernameContainer: FrameLayout = itemView.findViewById(R.id.usernameContainer)
        val statusContainer: FrameLayout = itemView.findViewById(R.id.statusContainer)
        val timeText: TextView = itemView.findViewById(R.id.timeLabelText)
        val transferContainer: LinearLayout? = itemView.findViewById(R.id.transferStatusContainer)

        var messagePicture: RoundImageView? = null
        var messageLink: TextView? = null
        var messageText: TextView? = null
        var username: TextView? = null
        var statusIcon: ImageView? = null
        var statusText: TextView? = null
        var voiceDuration: TextView? = null
        var videoThumbnail: RoundImageView? = null
        var fileName: TextView? = null
        var fileInfo: TextView? = null

        fun clearMessageBindings() {
            icon = null
            messagePicture = null
            messageLink = null
            messageText = null
            username = null
            statusIcon = null
            statusText = null
            voiceDuration = null
            videoThumbnail = null
            fileName = null
            fileInfo = null
        }
    }

    internal inner class DateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateLabelText: TextView = itemView.findViewById(R.id.dateLabelText)
    }
}
