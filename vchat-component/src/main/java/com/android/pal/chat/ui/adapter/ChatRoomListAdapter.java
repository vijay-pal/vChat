package com.android.pal.chat.ui.adapter;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.android.pal.chat.R;
import com.android.pal.chat.base.BaseActivity;
import com.android.pal.chat.base.StaticConfig;
import com.android.pal.chat.data.firebase.AddFriendChatRoom;
import com.android.pal.chat.data.firebase.GroupMessage;
import com.android.pal.chat.data.firebase.SearchPeopleValueEvent;
import com.android.pal.chat.model.ChatRoom;
import com.android.pal.chat.ui.activities.ChatActivity;
import com.android.pal.chat.util.GlideUtils;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by vijay on 8/1/18.
 */

public class ChatRoomListAdapter extends RecyclerView.Adapter<ChatRoomListAdapter.ItemGroupViewHolder>
  implements Filterable, AddFriendChatRoom.FriendAddedListener {
  private List<ChatRoom> chatRooms = new ArrayList<>();
  private List<ChatRoom> originalChatRooms;
  private BaseActivity context;
  private Filter mFilter;
  private SearchPeopleValueEvent.SearchPeopleListener mListener;
  private GroupMessage groupMessage;

  public ChatRoomListAdapter(BaseActivity context, List<ChatRoom> originalChatRooms, SearchPeopleValueEvent.SearchPeopleListener mListener) {
    this.context = context;
    this.originalChatRooms = originalChatRooms;
    this.chatRooms = originalChatRooms;
    this.mListener = mListener;
    this.groupMessage = new GroupMessage();
  }

  @Override
  public ItemGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.rc_item_group, parent, false);
    return new ItemGroupViewHolder(view);
  }

  @Override
  public void onBindViewHolder(ItemGroupViewHolder holder, final int position) {
    final ChatRoom chatRoom = chatRooms.get(position);
    GlideUtils.display(context, chatRoom.avatar, holder.iconChatRoom, chatRoom.isGroup ? R.drawable.default_group_avatar : R.drawable.default_avatar);
    holder.txtName.setText(chatRoom.name);
    groupMessage.setMessage(chatRoom.roomId, holder.txtMessage, holder.txtDate);

    ((View) holder.txtName.getParent()).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (TextUtils.isEmpty(chatRoom.roomId)) {
          new AddFriendChatRoom(context, chatRoom, ChatRoomListAdapter.this);
        } else {
          onAdded(chatRoom);
        }
      }
    });
  }

  public void changeDataSet(List<ChatRoom> chatRooms, boolean isChange) {
    if (chatRooms != null) {
      this.chatRooms = chatRooms;
      if (isChange) {
        this.originalChatRooms = chatRooms;
      }
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

  @Override
  public void onAdded(ChatRoom chatRoom) {
    Intent intent = new Intent(context, ChatActivity.class);
    intent.putExtra(StaticConfig.INTENT_KEY_CHAT_ROOM, chatRoom.name);
    intent.putExtra(StaticConfig.INTENT_KEY_CHAT_ID, chatRoom.id);
    intent.putExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID, chatRoom.roomId);
    intent.putExtra(StaticConfig.INTENT_KEY_CHAT_AVATAR, chatRoom.avatar);
    intent.putExtra(StaticConfig.INTENT_KEY_CHAT_IS_GROUP, chatRoom.isGroup);
    context.startActivityForResult(intent, StaticConfig.REQUEST_CODE_CHAT);
  }

  @Override
  public void onFailed() {

  }

  class ItemGroupViewHolder extends RecyclerView.ViewHolder {
    final CircleImageView iconChatRoom;
    final TextView txtName;
    final TextView txtMessage;
    final TextView txtDate;

    ItemGroupViewHolder(View itemView) {
      super(itemView);
      iconChatRoom = (CircleImageView) itemView.findViewById(R.id.img_avatar);
      txtName = (TextView) itemView.findViewById(R.id.txtName);
      txtMessage = (TextView) itemView.findViewById(R.id.txtMessage);
      txtDate = (TextView) itemView.findViewById(R.id.txtDate);
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
      chatRooms = (List<ChatRoom>) results.values;
      if (results.count == 0 && StaticConfig.IS_ENABLE_GLOBAL_SEARCH) {
        new SearchPeopleValueEvent(mListener, constraint.toString());
      }
      notifyDataSetChanged();
    }
  }
}
