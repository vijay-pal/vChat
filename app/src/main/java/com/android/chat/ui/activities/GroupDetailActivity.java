package com.android.chat.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.chat.R;
import com.android.chat.data.GroupDB;
import com.android.chat.data.MemberDB;
import com.android.chat.data.StaticConfig;
import com.android.chat.model.Group;
import com.android.chat.ui.adapter.GroupMemberAdapter;
import com.android.chat.util.GlideUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;

import java.util.ArrayList;

/**
 * Created by admirar on 1/7/18.
 */

public class GroupDetailActivity extends AppCompatActivity {

  public static final int CONTEXT_MENU_DELETE = 1;
  public static final int CONTEXT_MENU_EDIT = 2;
  public static final int CONTEXT_MENU_LEAVE = 3;
  public static final int REQUEST_EDIT_GROUP = 0;
  public static final String CONTEXT_MENU_KEY_INTENT_DATA_POS = "pos";

  private CollapsingToolbarLayout collapsingToolbarLayout;
  private String groupId;
  private GroupMemberAdapter mAdaper;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Intent intent = getIntent();
    if (intent != null) {
      groupId = intent.getStringExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID);
    }

    setContentView(R.layout.activity_group_detail);
    RecyclerView recyclerView = (RecyclerView) findViewById(R.id.info_recycler_view);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));

    Group group = GroupDB.getInstance(this).getGroup(groupId);
    initToolbar(group);

    mAdaper = new GroupMemberAdapter(this, group.groupInfo.get("admin"));
    mAdaper.addData(MemberDB.getInstance(this).getMembers(groupId));
    recyclerView.setAdapter(mAdaper);
  }

  private void initToolbar(Group group) {
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
    collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
    collapsingToolbarLayout.setTitle(group.groupInfo.get("name"));

    ImageView imageView = (ImageView) findViewById(R.id.image_view);
    GlideUtils.display(this, group.groupInfo.get("avatar"), imageView, R.drawable.default_group_avatar);

    collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.collapsedappbar);
    collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.expandedappbar);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {

    /*switch (item.getItemId()) {
      case CONTEXT_MENU_DELETE:
        int posGroup = item.getIntent().getIntExtra(CONTEXT_MENU_KEY_INTENT_DATA_POS, -1);
        if (((String) listGroup.get(posGroup).groupInfo.get("admin")).equals(StaticConfig.UID)) {
          Group group = listGroup.get(posGroup);
          listGroup.remove(posGroup);
          if (group != null) {
            deleteGroup(group, 0);
          }
        } else {
          Toast.makeText(getActivity(), "You are not admin", Toast.LENGTH_LONG).show();
        }
        break;
      case CONTEXT_MENU_EDIT:
        int posGroup1 = item.getIntent().getIntExtra(CONTEXT_MENU_KEY_INTENT_DATA_POS, -1);
        if (((String) listGroup.get(posGroup1).groupInfo.get("admin")).equals(StaticConfig.UID)) {
          Intent intent = new Intent(getContext(), AddGroupActivity.class);
          intent.putExtra("groupId", listGroup.get(posGroup1).id);
          startActivityForResult(intent, REQUEST_EDIT_GROUP);
        } else {
          Toast.makeText(getActivity(), "You are not admin", Toast.LENGTH_LONG).show();
        }

        break;

      case CONTEXT_MENU_LEAVE:
        int position = item.getIntent().getIntExtra(CONTEXT_MENU_KEY_INTENT_DATA_POS, -1);
        if (((String) listGroup.get(position).groupInfo.get("admin")).equals(StaticConfig.UID)) {
          Toast.makeText(getActivity(), "Admin cannot leave group", Toast.LENGTH_LONG).show();
        } else {
          waitingLeavingGroup.show();
          Group groupLeaving = listGroup.get(position);
          leaveGroup(groupLeaving);
        }
        break;
    }*/

    return super.onContextItemSelected(item);
  }

 /* public void deleteGroup(final Group group, final int index) {
    if (index == group.member.size()) {
      FirebaseDatabase.getInstance().getReference().child("group/" + group.id).removeValue()
        .addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {
            progressDialog.dismiss();
            GroupDB.getInstance(getContext()).deleteGroup(group.id);
            listGroup.remove(group);
            adapter.notifyDataSetChanged();
            Toast.makeText(getContext(), "Deleted group", Toast.LENGTH_SHORT).show();
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            progressDialog.dismiss();
            new LovelyInfoDialog(getContext())
              .setTopColorRes(R.color.colorAccent)
              .setIcon(R.drawable.ic_dialog_delete_group)
              .setTitle("False")
              .setMessage("Cannot delete group right now, please try again.")
              .setCancelable(false)
              .setConfirmButtonText("Ok")
              .show();
          }
        })
      ;
    } else {
      FirebaseDatabase.getInstance().getReference().child("user/" + group.member.get(index) + "/group/" + group.id).removeValue()
        .addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {
            deleteGroup(group, index + 1);
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            progressDialog.dismiss();
            new LovelyInfoDialog(getContext())
              .setTopColorRes(R.color.colorAccent)
              .setIcon(R.drawable.ic_dialog_delete_group)
              .setTitle("False")
              .setMessage("Cannot connect server")
              .setCancelable(false)
              .setConfirmButtonText("Ok")
              .show();
          }
        })
      ;
    }

  }

  public void leaveGroup(final Group group) {
    FirebaseDatabase.getInstance().getReference().child("group/" + group.id + "/member")
      .orderByValue().equalTo(StaticConfig.UID)
      .addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {

          if (dataSnapshot.getValue() == null) {
            //email not found
            waitingLeavingGroup.dismiss();
            new LovelyInfoDialog(getContext())
              .setTopColorRes(R.color.colorAccent)
              .setTitle("Error")
              .setMessage("Error occurred during leaving group")
              .show();
          } else {
            String memberIndex = "";
            ArrayList<String> result = ((ArrayList<String>) dataSnapshot.getValue());
            for (int i = 0; i < result.size(); i++) {
              if (result.get(i) != null) {
                memberIndex = String.valueOf(i);
              }
            }

            FirebaseDatabase.getInstance().getReference().child("user").child(StaticConfig.UID)
              .child("group").child(group.id).removeValue();
            FirebaseDatabase.getInstance().getReference().child("group/" + group.id + "/member")
              .child(memberIndex).removeValue()
              .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                  waitingLeavingGroup.dismiss();

                  listGroup.remove(group);
                  adapter.notifyDataSetChanged();
                  GroupDB.getInstance(getContext()).deleteGroup(group.id);
                  new LovelyInfoDialog(getContext())
                    .setTopColorRes(R.color.colorAccent)
                    .setTitle("Success")
                    .setMessage("Group leaving successfully")
                    .show();
                }
              })
              .addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                  waitingLeavingGroup.dismiss();
                  new LovelyInfoDialog(getContext())
                    .setTopColorRes(R.color.colorAccent)
                    .setTitle("Error")
                    .setMessage("Error occurred during leaving group")
                    .show();
                }
              });
          }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
          //email not found
          waitingLeavingGroup.dismiss();
          new LovelyInfoDialog(getContext())
            .setTopColorRes(R.color.colorAccent)
            .setTitle("Error")
            .setMessage("Error occurred during leaving group")
            .show();
        }
      });

  }*/
}
