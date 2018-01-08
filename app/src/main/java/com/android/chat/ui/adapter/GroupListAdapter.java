package com.android.chat.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.chat.R;
import com.android.chat.data.FriendDB;
import com.android.chat.data.StaticConfig;
import com.android.chat.model.Group;
import com.android.chat.model.ListFriend;
import com.android.chat.ui.ChatActivity;
import com.android.chat.ui.GroupFragment;
import com.android.chat.util.GlideUtils;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by vijay on 8/1/18.
 */

public class GroupListAdapter extends RecyclerView.Adapter<GroupListAdapter.ItemGroupViewHolder> {
  private ArrayList<Group> listGroup;
  public static ListFriend listFriend = null;
  private Context context;

  public GroupListAdapter(Context context, ArrayList<Group> listGroup) {
    this.context = context;
    this.listGroup = listGroup;
  }

  @Override
  public ItemGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.rc_item_group, parent, false);
    return new ItemGroupViewHolder(view);
  }

  @Override
  public void onBindViewHolder(ItemGroupViewHolder holder, final int position) {
    final String groupName = listGroup.get(position).groupInfo.get("name");
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
    });
  }

  @Override
  public int getItemCount() {
    return listGroup.size();
  }

  class ItemGroupViewHolder extends RecyclerView.ViewHolder {
    final ImageView iconGroup;
    final TextView txtGroupName;
    final TextView txtStatus;

    public ItemGroupViewHolder(View itemView) {
      super(itemView);
      iconGroup = (ImageView) itemView.findViewById(R.id.img_avatar);
      txtGroupName = (TextView) itemView.findViewById(R.id.txtName);
      txtStatus = (TextView) itemView.findViewById(R.id.txtStatus);
    }
  }
}
