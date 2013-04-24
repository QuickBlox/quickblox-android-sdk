package com.quickblox.chat_v2.interfaces;

import java.util.List;

import com.quickblox.module.custom.model.QBCustomObject;

public interface OnMessageListDownloaded {
	
	public void messageListDownloaded(List<QBCustomObject> downloadedList);
}
