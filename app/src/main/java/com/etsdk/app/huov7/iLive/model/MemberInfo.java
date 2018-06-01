package com.etsdk.app.huov7.iLive.model;

public class MemberInfo {

    private String userId = "";
    private String userName = "";
    private String avatar = "";
    private boolean isVoice = false;
    private boolean isOnVideoChat = false;

    public boolean isVoice() {
        return isVoice;
    }

    public void setVoice(boolean voice) {
        isVoice = voice;
    }

    public void setOnVideoChat(boolean onVideoChat) {
        isOnVideoChat = onVideoChat;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public boolean isOnVideoChat() {
        return isOnVideoChat;
    }

    public void setIsOnVideoChat(boolean isOnVideoChat) {
        this.isOnVideoChat = isOnVideoChat;
    }
}