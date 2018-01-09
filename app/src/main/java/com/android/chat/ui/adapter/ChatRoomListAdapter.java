package com.android.chat.ui.adapter;

import android.content.Context;
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
import com.android.chat.model.ChatRoom;
import com.android.chat.model.Group;
import com.android.chat.model.ListFriend;
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
  private ArrayList<Group> listGroup;
  public static ListFriend listFriend = null;
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
    ChatRoom chatRoom = chatRooms.get(position);
    GlideUtils.display(context, chatRoom.avatar, holder.iconGroup, chatRoom.name, R.drawable.default_group_avatar);
    holder.txtGroupName.setText(chatRoom.name);
    holder.txtStatus.setVisibility(View.GONE);

    /*final String groupName = listGroup.get(position).groupInfo.get("name");
    final String avatar = listGroup.get(position).groupInfo.get("avatar");

    GlideUtils.display(context, avatar, holder.iconGroup, groupName, R.drawable.default_group_avatar);
    holder.txtGroupName.setText(groupName);
    holder.txtStatus.setVisibility(View.GONE);

    ((View) holder.txtGroupName.getParent()).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (listFriend == null) {
          listFriend = FriendDB.getInstance(context).getListFriend();
        }
        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND, groupName);
        ArrayList<CharSequence> idFriend = new ArrayList<>();
        ChatActivity.bitmapAvatarFriend = new HashMap<>();
        for (String id : listGroup.get(position).member) {
          idFriend.add(id);
          String avatar = listFriend.getAvatarById(id);
          if (!avatar.equals(StaticConfig.STR_DEFAULT_BASE64)) {
            byte[] decodedString = Base64.decode(avatar, Base64.DEFAULT);
            ChatActivity.bitmapAvatarFriend.put(id, BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length));
          } else if (avatar.equals(StaticConfig.STR_DEFAULT_BASE64)) {
            ChatActivity.bitmapAvatarFriend.put(id, BitmapFactory.decodeResource(context.getResources(), R.drawable.default_avatar));
          } else {
            ChatActivity.bitmapAvatarFriend.put(id, null);
          }
        }
        intent.putCharSequenceArrayListExtra(StaticConfig.INTENT_KEY_CHAT_ID, idFriend);
        intent.putExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID, listGroup.get(position).id);
        intent.putExtra(StaticConfig.INTENT_KEY_CHAT_AVATA, avatar);
        context.startActivity(intent);
      }
    });*/
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
    final ImageView iconGroup;
    final TextView txtGroupName;
    final TextView txtStatus;

    ItemGroupViewHolder(View itemView) {
      super(itemView);
      iconGroup = (ImageView) itemView.findViewById(R.id.img_avatar);
      txtGroupName = (TextView) itemView.findViewById(R.id.txtName);
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
      } else {
        List<ChatRoom> chatRooms = new ArrayList<>();
        for (ChatRoom chatRoom : originalChatRooms) {
          if (chatRoom.name.equalsIgnoreCase(charString) || chatRoom.email.equalsIgnoreCase(charString)) {
            chatRooms.add(chatRoom);
          }
        }
        filterResults.values = chatRooms;
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
