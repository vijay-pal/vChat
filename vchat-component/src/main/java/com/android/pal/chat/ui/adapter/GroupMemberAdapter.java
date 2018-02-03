package com.android.pal.chat.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.pal.chat.R;
import com.android.pal.chat.model.Member;
import com.android.pal.chat.util.GlideUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by admirar on 1/8/18.
 */

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ItemViewHolder> {

    private List<Member> members = new ArrayList<>();
    private Context context;
    private String adminId;

    public GroupMemberAdapter(Context context, String adminId) {
        this.context = context;
        this.adminId = adminId;
    }

    @Override
    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.rc_item_group_member, parent, false));
    }

    @Override
    public void onBindViewHolder(ItemViewHolder holder, int position) {
        Member member = members.get(position);
        GlideUtils.display(context, member.avatar, holder.imgAvatar, R.drawable.default_avatar);
        holder.txtName.setText(member.toString());
        holder.txtAdmin.setVisibility(member.isAdmin(adminId) ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public void addData(List<Member> members) {
        if (members != null) {
            this.members.clear();
            this.members.addAll(members);
            notifyDataSetChanged();
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {

        final ImageView imgAvatar;
        final TextView txtName;
        final TextView txtStatus;
        final TextView txtAdmin;

        ItemViewHolder(View itemView) {
            super(itemView);
            imgAvatar = (ImageView) itemView.findViewById(R.id.img_avatar);
            txtName = (TextView) itemView.findViewById(R.id.txtName);
            txtStatus = (TextView) itemView.findViewById(R.id.txtStatus);
            txtAdmin = (TextView) itemView.findViewById(R.id.txtAdmin);
        }
    }
}
