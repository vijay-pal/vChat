package com.android.chat.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.chat.data.firebase.GroupValueEventListenerImpl;
import com.android.chat.ui.activities.GroupDetailActivity;
import com.android.chat.ui.adapter.ChatRoomListAdapter;
import com.google.firebase.database.FirebaseDatabase;
import com.android.chat.R;
import com.android.chat.data.GroupDB;
import com.android.chat.data.StaticConfig;
import com.android.chat.model.Group;
import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import java.util.ArrayList;
import java.util.List;


public class GroupFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

  private RecyclerView recyclerListGroups;
  private ArrayList<Group> listGroup;
  private ChatRoomListAdapter adapter;
  private SwipeRefreshLayout mSwipeRefreshLayout;

  LovelyProgressDialog progressDialog, waitingLeavingGroup;

  public GroupFragment() {
    // Required empty public constructor
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View layout = inflater.inflate(R.layout.fragment_group, container, false);

    listGroup = GroupDB.getInstance(getContext()).getListGroups();
    recyclerListGroups = (RecyclerView) layout.findViewById(R.id.recycleListGroup);
    mSwipeRefreshLayout = (SwipeRefreshLayout) layout.findViewById(R.id.swipeRefreshLayout);
    mSwipeRefreshLayout.setOnRefreshListener(this);
    recyclerListGroups.setLayoutManager(new LinearLayoutManager(getContext()));
//    adapter = new ChatRoomListAdapter(getContext(), listGroup);
//    recyclerListGroups.setAdapter(adapter);
    progressDialog = new LovelyProgressDialog(getContext())
      .setCancelable(false)
      .setIcon(R.drawable.ic_dialog_delete_group)
      .setTitle("Deleting....")
      .setTopColorRes(R.color.colorAccent);

    waitingLeavingGroup = new LovelyProgressDialog(getContext())
      .setCancelable(false)
      .setIcon(R.drawable.ic_dialog_delete_group)
      .setTitle("Group leaving....")
      .setTopColorRes(R.color.colorAccent);

    if (listGroup.size() == 0) {
      //Ket noi server hien thi group
      mSwipeRefreshLayout.setRefreshing(true);
      getListGroup();
    }
    return layout;
  }

  private void getListGroup() {
//    FirebaseDatabase.getInstance().getReference().child("user/" + StaticConfig.UID + "/group")
//      .addListenerForSingleValueEvent(new GroupValueEventListenerImpl(this, listGroup, getContext()));
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == GroupDetailActivity.REQUEST_EDIT_GROUP && resultCode == Activity.RESULT_OK) {
      listGroup.clear();
      ChatRoomListAdapter.listFriend = null;
      GroupDB.getInstance(getContext()).dropDB();
      getListGroup();
    }
  }

 /* private void getGroupInfo(final int indexGroup) {
    if (indexGroup == listGroup.size()) {
      adapter.notifyDataSetChanged();
      mSwipeRefreshLayout.setRefreshing(false);
    } else {
      FirebaseDatabase.getInstance().getReference().child("group/" + listGroup.get(indexGroup).id).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          if (dataSnapshot.getValue() != null) {
            HashMap mapGroup = (HashMap) dataSnapshot.getValue();
            ArrayList<String> member = (ArrayList<String>) mapGroup.get("member");
            HashMap mapGroupInfo = (HashMap) mapGroup.get("groupInfo");
            for (String idMember : member) {
              listGroup.get(indexGroup).member.add(idMember);
            }
            listGroup.get(indexGroup).groupInfo.put("name", (String) mapGroupInfo.get("name"));
            listGroup.get(indexGroup).groupInfo.put("admin", (String) mapGroupInfo.get("admin"));
            listGroup.get(indexGroup).groupInfo.put("avatar", (String) mapGroupInfo.get("avatar"));
          }
          GroupDB.getInstance(getContext()).addGroup(listGroup.get(indexGroup));
          getGroupInfo(indexGroup + 1);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
      });
    }
  }*/

  @Override
  public void onRefresh() {
    listGroup.clear();
    ChatRoomListAdapter.listFriend = null;
    GroupDB.getInstance(getContext()).dropDB();
    adapter.notifyDataSetChanged();
    getListGroup();
  }
}
