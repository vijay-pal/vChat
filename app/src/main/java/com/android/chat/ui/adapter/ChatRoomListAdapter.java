package com.android.chat.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.chat.R;
import com.android.chat.data.StaticConfig;
import com.android.chat.model.ChatRoom;
import com.android.chat.model.Group;
import com.android.chat.model.ListFriend;
import com.android.chat.ui.activities.ChatActivity;
import com.android.chat.util.GlideUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by vijay on 8/1/18.
 */

public class ChatRoomListAdapter extends RecyclerView.Adapter<ChatRoomListAdapter.ItemGroupViewHolder>
        implements Filterable {
    private List<ChatRoom> chatRooms = new ArrayList<>();
    private List<ChatRoom> originalChatRooms;
    private Context context;
    private Filter mFilter;

    public ChatRoomListAdapter(Context context, List<ChatRoom> originalChatRooms) {
        this.context = context;
        this.originalChatRooms = originalChatRooms;
        this.chatRooms.addAll(originalChatRooms);
    }

    @Override
    public ItemGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_group, parent, false);
        return new ItemGroupViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ItemGroupViewHolder holder, final int position) {
        final ChatRoom chatRoom = chatRooms.get(position);

        GlideUtils.display(context, chatRoom.avatar, holder.iconChatRoom, chatRoom.name, R.drawable.default_group_avatar);
        holder.txtName.setText(chatRoom.name);
        holder.txtStatus.setVisibility(View.GONE);

        ((View) holder.txtName.getParent()).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra(StaticConfig.INTENT_KEY_CHAT_ROOM, chatRoom.name);
                intent.putExtra(StaticConfig.INTENT_KEY_CHAT_ID, chatRoom.id);
                intent.putExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID, chatRoom.roomId);
                intent.putExtra(StaticConfig.INTENT_KEY_CHAT_AVATAR, chatRoom.avatar);
                intent.putExtra(StaticConfig.INTENT_KEY_CHAT_IS_GROUP, chatRoom.isGroup);
                context.startActivity(intent);
            }
        });
    }

    public void changeDataSet(List<ChatRoom> chatRooms) {
        if (chatRooms != null) {
            this.chatRooms.clear();
            this.chatRooms.addAll(chatRooms);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new FilterImpl();
        }
        return mFilter;
    }

    class ItemGroupViewHolder extends RecyclerView.ViewHolder {
        final ImageView iconChatRoom;
        final TextView txtName;
        final TextView txtStatus;

        ItemGroupViewHolder(View itemView) {
            super(itemView);
            iconChatRoom = (ImageView) itemView.findViewById(R.id.img_avatar);
            txtName = (TextView) itemView.findViewById(R.id.txtName);
            txtStatus = (TextView) itemView.findViewById(R.id.txtStatus);
        }
    }

    class FilterImpl extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            String charString = constraint.toString();
            FilterResults filterResults = new FilterResults();
            if (TextUtils.isEmpty(charString)) {
                filterResults.values = originalChatRooms;
                filterResults.count = originalChatRooms.size();
            } else {
                List<ChatRoom> chatRooms = new ArrayList<>();
                for (ChatRoom chatRoom : originalChatRooms) {
                    if (chatRoom.containsIgnoreCare(charString)) {
                        chatRooms.add(chatRoom);
                    }
                }
                filterResults.values = chatRooms;
                filterResults.count = chatRooms.size();
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            chatRooms.clear();
            chatRooms.addAll((Collection<? extends ChatRoom>) results.values);
            notifyDataSetChanged();
        }
    }
}
