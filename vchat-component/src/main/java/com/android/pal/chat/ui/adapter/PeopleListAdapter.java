package com.android.pal.chat.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.pal.chat.R;
import com.android.pal.chat.model.Group;
import com.android.pal.chat.model.ListFriend;
import com.android.pal.chat.util.GlideUtils;

import java.util.Set;


/**
 * Created by vijay on 8/1/18.
 */

public class PeopleListAdapter extends RecyclerView.Adapter<PeopleListAdapter.ItemFriendHolder> {
  private Context context;
  private ListFriend listFriend;
  private Set<String> listIDChoose;
  private Set<String> listIDRemove;
  private boolean isEdit;
  private Group editGroup;

  public PeopleListAdapter(Context context, ListFriend listFriend, Set<String> listIDChoose, Set<String> listIDRemove, boolean isEdit, Group editGroup) {
    this.context = context;
    this.listFriend = listFriend;
    this.listIDChoose = listIDChoose;
    this.listIDRemove = listIDRemove;

    this.isEdit = isEdit;
    this.editGroup = editGroup;
  }

  @Override
  public ItemFriendHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    return new ItemFriendHolder(LayoutInflater.from(context).inflate(R.layout.rc_item_add_friend, parent, false));
  }

  @Override
  public void onBindViewHolder(ItemFriendHolder holder, int position) {
    String avatar = listFriend.getListFriend().get(position).avatar;
    GlideUtils.display(context, avatar, holder.avatar, R.drawable.default_avatar);

    holder.txtName.setText(listFriend.getListFriend().get(position).name);
    holder.txtEmail.setText(listFriend.getListFriend().get(position).email);
    final String id = listFriend.getListFriend().get(position).id;

    holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override
      public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        if (b) {
          listIDChoose.add(id);
          listIDRemove.remove(id);
        } else {
          listIDRemove.add(id);
          listIDChoose.remove(id);
        }
        if (listIDChoose.size() >= 3) {
//                    btnAddGroup.setBackgroundColor(context.getResources().getColor(R.color.colorPrimary));
        } else {
//                    btnAddGroup.setBackgroundColor(context.getResources().getColor(R.color.grey_500));
        }
      }
    });
    if (isEdit && editGroup.member.contains(id)) {
      holder.checkBox.setChecked(true);
    } else if (editGroup != null && !editGroup.member.contains(id)) {
      holder.checkBox.setChecked(false);
    }
  }

  @Override
  public int getItemCount() {
    return listFriend.getListFriend().size();
  }

  class ItemFriendHolder extends RecyclerView.ViewHolder {
    public TextView txtName, txtEmail;
    public ImageView avatar;
    public CheckBox checkBox;

    public ItemFriendHolder(View itemView) {
      super(itemView);
      txtName = (TextView) itemView.findViewById(R.id.txtName);
      txtEmail = (TextView) itemView.findViewById(R.id.txtEmail);
      avatar = (ImageView) itemView.findViewById(R.id.icon_avata);
      checkBox = (CheckBox) itemView.findViewById(R.id.checkAddPeople);
    }
  }
}
