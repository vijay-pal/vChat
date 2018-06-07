package com.android.pal.chat.ui.activities;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.pal.chat.R;
import com.android.pal.chat.base.BaseActivity;
import com.android.pal.chat.base.StaticConfig;
import com.android.pal.chat.base.Utility;
import com.android.pal.chat.data.GroupDB;
import com.android.pal.chat.data.MemberDB;
import com.android.pal.chat.model.Group;
import com.android.pal.chat.ui.adapter.ConversationAdapter;
import com.android.pal.chat.ui.adapter.GroupMemberAdapter;
import com.android.pal.chat.util.GlideUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import static com.android.pal.chat.ui.activities.ChatActivity.CAMERA_REQUEST;
import static com.android.pal.chat.ui.activities.ChatActivity.PICK_IMAGE_REQUEST;

/**
 * Created by admirar on 1/7/18.
 */

public class GroupDetailActivity extends BaseActivity {

  public static final int CONTEXT_MENU_DELETE = 1;
  public static final int CONTEXT_MENU_EDIT = 2;
  public static final int CONTEXT_MENU_LEAVE = 3;

  public static final int CONTEXT_MENU_CALL = 4;
  public static final int CONTEXT_MENU_SMS = 5;

  public static final int REQUEST_EDIT_GROUP = 0;
  public static final String CONTEXT_MENU_KEY_INTENT_DATA_POS = "pos";

  private CollapsingToolbarLayout collapsingToolbarLayout;
  private String groupId;
  private GroupMemberAdapter mAdaper;
  private Group group;

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

    group = GroupDB.getInstance(this).getGroup(groupId);
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

    DisplayMetrics displayMetrics = new DisplayMetrics();
    getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
    int width = displayMetrics.widthPixels;
//    collapsingToolbarLayout.getLayoutParams().height = width;

    ImageView imageView = (ImageView) findViewById(R.id.image_view);
    GlideUtils.displayWithFixedWidth(this, group.groupInfo.get("avatar"), imageView, R.drawable.default_group_avatar, width);

    collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.collapsedappbar);
    collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.expandedappbar);

    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_group_detail, menu);
    return super.onCreateOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.actionEditGroupIcon) {
      selectImage();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    menu.setHeaderTitle("Select The Action");
    menu.add(v.getId(), CONTEXT_MENU_CALL, 0, "Call");
    menu.add(v.getId(), CONTEXT_MENU_SMS, 0, "SMS");
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {

    switch (item.getItemId()) {
      case CONTEXT_MENU_CALL:
        try {
          Intent callIntent = new Intent(Intent.ACTION_CALL);
          callIntent.setData(Uri.parse("tel:" + mAdaper.getItem(item.getOrder()).mobile));
          startActivity(callIntent);
        } catch (ActivityNotFoundException e) {
          Log.e("calling", "Call failed", e);
        }

        break;
      case CONTEXT_MENU_SMS:
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("address", mAdaper.getItem(item.getOrder()).mobile);
        smsIntent.putExtra("sms_body", "");
        startActivity(smsIntent);
        break;
      /*case CONTEXT_MENU_DELETE:
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
        break;*/
    }

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
  private void chooseImage() {
    Intent intent = new Intent();
// Show only images, no videos or anything else
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
// Always show the chooser (if there are multiple options available)
    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

  }

  private File photoFile;

  private void captureImage() {
    try {
      File storageDir = Environment.getExternalStoragePublicDirectory(
        Environment.DIRECTORY_PICTURES);
      photoFile = File.createTempFile(
        "IMG_" + System.currentTimeMillis(),  // prefix
        ".jpg",         // suffix
        storageDir      // directory
      );
      Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
      cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
      startActivityForResult(cameraIntent, CAMERA_REQUEST);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void selectImage() {
    final CharSequence[] items = {"Take Photo", "Choose from Library",
      "Cancel"};
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Add Photo!");
    builder.setItems(items, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int item) {
        boolean result = Utility.checkPermission(GroupDetailActivity.this);
        if (items[item].equals("Take Photo")) {
          if (result)
            captureImage();
        } else if (items[item].equals("Choose from Library")) {
          if (result)
            chooseImage();
        } else if (items[item].equals("Cancel")) {
          dialog.dismiss();
        }
      }
    });
    builder.show();
  }

  private Uri filePath;

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (resultCode != RESULT_OK) return;

    if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
      filePath = data.getData();
      uploadImage();
    } else if (requestCode == CAMERA_REQUEST) {
      filePath = Uri.fromFile(photoFile);
      uploadImage();
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  ProgressDialog dialog;

  private void uploadImage() {
    if (dialog == null) {
      dialog = new ProgressDialog(this);
    }
    dialog.show();
    if (filePath != null) {
      StorageReference ref = FirebaseStorage.getInstance().getReference().child("images/" + UUID.randomUUID().toString());
      ref.putFile(filePath)
        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
          @Override
          public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            FirebaseDatabase.getInstance().getReference().child("group").child(groupId).child("groupInfo").child("avatar").setValue(taskSnapshot.getDownloadUrl().toString());
            if (group != null) {
              group.groupInfo.put("avatar", taskSnapshot.getDownloadUrl().toString());
              GroupDB.getInstance(GroupDetailActivity.this).addGroup(group);
              ImageView imageView = (ImageView) findViewById(R.id.image_view);
              GlideUtils.display(GroupDetailActivity.this, taskSnapshot.getDownloadUrl().toString(), imageView, R.drawable.default_group_avatar);
            }
            dialog.dismiss();
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            Toast.makeText(GroupDetailActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            dialog.dismiss();
          }
        })
        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
          @Override
          public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
              .getTotalByteCount());
          }
        });
    }
  }
}
