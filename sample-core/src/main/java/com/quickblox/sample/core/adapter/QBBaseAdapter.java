package com.quickblox.sample.core.adapter;

import android.widget.ImageView;
import com.quickblox.chat.model.QBChatMessage;
import java.util.List;

public interface QBBaseAdapter<T> {

    T getItem(int position);

    int getItemViewType(int position);

    void add(T item);

    List<T> getList();

    void addList(List<T> items);

    void showAttachment(QBMessagesAdapter.QBMessagesAdapterViewHolder holder, int position);

    void displayAvatarImage (String uri, ImageView imageView);

    String obtainAvatarUrl(QBMessagesAdapter.ViewTypes type, QBChatMessage chatMessage);

}
