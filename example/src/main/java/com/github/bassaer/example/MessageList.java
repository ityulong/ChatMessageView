package com.github.bassaer.example;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;

import com.github.bassaer.chatmessageview.model.Message;
import com.github.bassaer.chatmessageview.model.IChatUser;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * Wrapper class {@link ArrayList} for save data
 * Created by nakayama on 2017/01/13.
 */
public class MessageList {

    private List<SaveMessage> mMessages;

    public MessageList() {
        mMessages = new ArrayList<>();
    }

    public ArrayList<Message> getMessages() {
        ArrayList<Message> messages = new ArrayList<>();
        for (SaveMessage saveMessage : mMessages) {
            messages.add(convertMessage(saveMessage));
        }
        return messages;
    }

    public void setMessages(List<Message> messages) {
        for (Message message : messages) {
            mMessages.add(convertMessage(message));
        }
    }

    public void add(Message message) {
        mMessages.add(convertMessage(message));
    }

    public Message get(int index) {
        return convertMessage(mMessages.get(index));
    }

    public List<Message> get() {
        List<Message> list = new ArrayList<>();
        for (SaveMessage message : mMessages) {
            list.add(convertMessage(message));
        }
        return list;
    }

    public int size() {
        return mMessages.size();
    }

    private SaveMessage convertMessage(Message message) {
        SaveMessage saveMessage = new SaveMessage(
                Integer.valueOf(message.getUser().getId()),
                message.getUser().getName(),
                message.getText(),
                message.getSendTime(),
                message.isRight());

        saveMessage.setType(message.getType());

        if (message.getType() == Message.Type.PICTURE
                && message.getPicture() != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            message.getPicture().compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            saveMessage.setPictureString(Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT));
        }

        if (message.getVoiceUri() != null) {
            saveMessage.setVoiceUri(message.getVoiceUri().toString());
        }
        saveMessage.setVoiceDuration(message.getVoiceDuration());

        if (message.getVideoUri() != null) {
            saveMessage.setVideoUri(message.getVideoUri().toString());
        }
        if (message.getVideoThumbnail() != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            message.getVideoThumbnail().compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            saveMessage.setVideoThumbnailString(Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT));
        }

        if (message.getFileUri() != null) {
            saveMessage.setFileUri(message.getFileUri().toString());
        }
        saveMessage.setFileName(message.getFileName());
        saveMessage.setFileInfo(message.getFileInfo());
        saveMessage.setTransferState(message.getTransferState().name());
        saveMessage.setTransferProgress(message.getTransferProgress());

        return saveMessage;
    }

    private Message convertMessage(SaveMessage saveMessage) {
        IChatUser user = new User(saveMessage.getId(), saveMessage.getUsername(), null);

        Message message = new Message.Builder()
                .setUser(user)
                .setText(saveMessage.getContent())
                .setRight(saveMessage.isRightMessage())
                .setSendTime(saveMessage.getCreatedAt())
                .setType(saveMessage.getType())
                .build();

        if (saveMessage.getPictureString() != null) {
            byte[] bytes = Base64.decode(saveMessage.getPictureString().getBytes(), Base64.DEFAULT);
            message.setPicture(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
        }
        if (saveMessage.getVoiceUri() != null) {
            message.setVoiceUri(Uri.parse(saveMessage.getVoiceUri()));
            message.setVoiceDuration(saveMessage.getVoiceDuration());
        }
        if (saveMessage.getVideoUri() != null) {
            message.setVideoUri(Uri.parse(saveMessage.getVideoUri()));
        }
        if (saveMessage.getVideoThumbnailString() != null) {
            byte[] videoBytes = Base64.decode(saveMessage.getVideoThumbnailString().getBytes(), Base64.DEFAULT);
            message.setVideoThumbnail(BitmapFactory.decodeByteArray(videoBytes, 0, videoBytes.length));
        }
        if (saveMessage.getFileUri() != null) {
            message.setFileUri(Uri.parse(saveMessage.getFileUri()));
        }
        message.setFileName(saveMessage.getFileName());
        message.setFileInfo(saveMessage.getFileInfo());
        if (saveMessage.getTransferState() != null) {
            message.setTransferState(Message.TransferState.valueOf(saveMessage.getTransferState()));
        }
        message.setTransferProgress(saveMessage.getTransferProgress());
        return message;
    }

    private class SaveMessage {
        private int mId;
        private String mUsername;
        private String mContent;
        private Calendar mCreatedAt;
        private boolean mRightMessage;
        private String mPictureString;
        private Message.Type mType;
        private String mVoiceUri;
        private int mVoiceDuration;
        private String mVideoUri;
        private String mVideoThumbnailString;
        private String mFileUri;
        private String mFileName;
        private String mFileInfo;
        private String mTransferState;
        private int mTransferProgress;

        public SaveMessage(int id, String username, String content, Calendar createdAt, boolean isRightMessage) {
            mId = id;
            mUsername = username;
            mContent = content;
            mCreatedAt = createdAt;
            mRightMessage = isRightMessage;
        }

        public int getId() {
            return mId;
        }

        public String getUsername() {
            return mUsername;
        }

        public String getContent() {
            return mContent;
        }

        public Calendar getCreatedAt() {
            return mCreatedAt;
        }

        public boolean isRightMessage() {
            return mRightMessage;
        }

        public String getPictureString() {
            return mPictureString;
        }

        public void setPictureString(String pictureString) {
            mPictureString = pictureString;
        }

        public Message.Type getType() {
            return mType;
        }

        public void setType(Message.Type type) {
            mType = type;
        }

        public String getVoiceUri() {
            return mVoiceUri;
        }

        public void setVoiceUri(String voiceUri) {
            mVoiceUri = voiceUri;
        }

        public int getVoiceDuration() {
            return mVoiceDuration;
        }

        public void setVoiceDuration(int voiceDuration) {
            mVoiceDuration = voiceDuration;
        }

        public String getVideoUri() {
            return mVideoUri;
        }

        public void setVideoUri(String videoUri) {
            mVideoUri = videoUri;
        }

        public String getVideoThumbnailString() {
            return mVideoThumbnailString;
        }

        public void setVideoThumbnailString(String videoThumbnailString) {
            mVideoThumbnailString = videoThumbnailString;
        }

        public String getFileUri() {
            return mFileUri;
        }

        public void setFileUri(String fileUri) {
            mFileUri = fileUri;
        }

        public String getFileName() {
            return mFileName;
        }

        public void setFileName(String fileName) {
            mFileName = fileName;
        }

        public String getFileInfo() {
            return mFileInfo;
        }

        public void setFileInfo(String fileInfo) {
            mFileInfo = fileInfo;
        }

        public String getTransferState() {
            return mTransferState;
        }

        public void setTransferState(String transferState) {
            mTransferState = transferState;
        }

        public int getTransferProgress() {
            return mTransferProgress;
        }

        public void setTransferProgress(int transferProgress) {
            mTransferProgress = transferProgress;
        }
    }
}
