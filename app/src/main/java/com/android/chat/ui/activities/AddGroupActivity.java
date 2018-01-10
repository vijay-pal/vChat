package com.android.chat.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.chat.R;
import com.android.chat.data.FriendDB;
import com.android.chat.data.GroupDB;
import com.android.chat.data.StaticConfig;
import com.android.chat.model.Group;
import com.android.chat.model.ListFriend;
import com.android.chat.model.Room;
import com.android.chat.ui.adapter.PeopleListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashSet;
import java.util.Set;


public class AddGroupActivity extends AppCompatActivity {

  private RecyclerView recyclerListFriend;
  private PeopleListAdapter adapter;
  private ListFriend listFriend;
  private LinearLayout btnAddGroup;
  private Set<String> listIDChoose;
  private Set<String> listIDRemove;
  private EditText editTextGroupName;
  private TextView txtGroupIcon, txtActionName;
  private ProgressDialog dialogWait;
  private boolean isEditGroup;
  private Group groupEdit;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_group);

    Intent intentData = getIntent();
    txtActionName = (TextView) findViewById(R.id.txtActionName);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
    listFriend = FriendDB.getInstance(this).getListFriend();
    listIDChoose = new HashSet<>();
    listIDRemove = new HashSet<>();
    listIDChoose.add(StaticConfig.UID);
    btnAddGroup = (LinearLayout) findViewById(R.id.btnAddGroup);
    editTextGroupName = (EditText) findViewById(R.id.editGroupName);
    txtGroupIcon = (TextView) findViewById(R.id.icon_group);


    dialogWait = new ProgressDialog(this);
    dialogWait.setCancelable(false);
    editTextGroupName.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

      }

      @Override
      public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if (charSequence.length() >= 1) {
          txtGroupIcon.setText((charSequence.charAt(0) + "").toUpperCase());
        } else {
          txtGroupIcon.setText("G");
        }
      }

      @Override
      public void afterTextChanged(Editable editable) {

      }
    });

    btnAddGroup.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        if (listIDChoose.size() < 2) {
          Toast.makeText(AddGroupActivity.this, "Add at lease one people to create group", Toast.LENGTH_SHORT).show();
        } else {
          if (editTextGroupName.getText().length() == 0) {
            Toast.makeText(AddGroupActivity.this, "Enter group name", Toast.LENGTH_SHORT).show();
          } else {
            if (isEditGroup) {
              editGroup();
            } else {
              createGroup();
            }
          }
        }
      }
    });

    if (intentData.getStringExtra("groupId") != null) {
      isEditGroup = true;
      String idGroup = intentData.getStringExtra("groupId");
      txtActionName.setText("Save");
      btnAddGroup.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
      groupEdit = GroupDB.getInstance(this).getGroup(idGroup);
      editTextGroupName.setText(groupEdit.groupInfo.get("name"));
    } else {
      isEditGroup = false;
    }

    recyclerListFriend = (RecyclerView) findViewById(R.id.recycleListFriend);
    recyclerListFriend.setLayoutManager(linearLayoutManager);
    adapter = new PeopleListAdapter(this, listFriend, btnAddGroup, listIDChoose, listIDRemove, isEditGroup, groupEdit);
    recyclerListFriend.setAdapter(adapter);


  }

  private void editGroup() {
    //Show dialog wait
    dialogWait.setIcon(R.drawable.ic_add_group_dialog);
    dialogWait.setTitle("Editing....");
    dialogWait.show();
    //Delete group
    final String idGroup = groupEdit.id;
    Room room = new Room();
    for (String id : listIDChoose) {
      room.member.add(id);
    }
    room.groupInfo.put("name", editTextGroupName.getText().toString());
    room.groupInfo.put("admin", StaticConfig.UID);
    FirebaseDatabase.getInstance().getReference().child("group/" + idGroup).setValue(room)
      .addOnCompleteListener(new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
          addRoomForUser(idGroup, 0);
        }
      })
      .addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          dialogWait.dismiss();
          Toast.makeText(AddGroupActivity.this, "Cannot connect database", Toast.LENGTH_LONG).show();
        }
      })
    ;
  }

  private void createGroup() {
    //Show dialog wait
    dialogWait.setIcon(R.drawable.ic_add_group_dialog);
    dialogWait.setTitle("Registering....");
    dialogWait.show();

    final String idGroup = (StaticConfig.UID + System.currentTimeMillis()).hashCode() + "";
    Room room = new Room();
    for (String id : listIDChoose) {
      room.member.add(id);
    }
    room.groupInfo.put("name", editTextGroupName.getText().toString());
    room.groupInfo.put("admin", StaticConfig.UID);
    FirebaseDatabase.getInstance().getReference().child("group/" + idGroup).setValue(room).addOnCompleteListener(new OnCompleteListener<Void>() {
      @Override
      public void onComplete(@NonNull Task<Void> task) {
        addRoomForUser(idGroup, 0);
      }
    });
  }

  private void deleteRoomForUser(final String roomId, final int userIndex) {
    if (userIndex == listIDRemove.size()) {
      dialogWait.dismiss();
      Toast.makeText(this, "Edit group success", Toast.LENGTH_SHORT).show();
      setResult(RESULT_OK, null);
      AddGroupActivity.this.finish();
    } else {
      FirebaseDatabase.getInstance().getReference().child("user/" + listIDRemove.toArray()[userIndex] + "/group/" + roomId).removeValue()
        .addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {
            deleteRoomForUser(roomId, userIndex + 1);
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            dialogWait.dismiss();
            Toast.makeText(AddGroupActivity.this, "Cannot connect database", Toast.LENGTH_LONG).show();
          }
        });
    }
  }

  private void addRoomForUser(final String roomId, final int userIndex) {
    if (userIndex == listIDChoose.size()) {
      if (!isEditGroup) {
        dialogWait.dismiss();
        Toast.makeText(this, "Create group success", Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK, null);
        AddGroupActivity.this.finish();
      } else {
        deleteRoomForUser(roomId, 0);
      }
    } else {
      FirebaseDatabase.getInstance().getReference().child("user/" + listIDChoose.toArray()[userIndex] + "/group/" + roomId).setValue(roomId).addOnCompleteListener(new OnCompleteListener<Void>() {
        @Override
        public void onComplete(@NonNull Task<Void> task) {
          addRoomForUser(roomId, userIndex + 1);
        }
      }).addOnFailureListener(new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
          dialogWait.dismiss();
          Toast.makeText(AddGroupActivity.this, "Create group false", Toast.LENGTH_LONG).show();
        }
      });
    }
  }
}


