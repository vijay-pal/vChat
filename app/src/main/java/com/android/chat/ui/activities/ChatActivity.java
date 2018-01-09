package com.android.chat.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import com.android.chat.R;
import com.android.chat.data.MemberDB;
import com.android.chat.data.SharedPreferenceHelper;
import com.android.chat.data.StaticConfig;
import com.android.chat.data.firebase.MessageChildChangeListenerImpl;
import com.android.chat.data.firebase.MessageMemberChangeListenerImpl;
import com.android.chat.model.Conversation;
import com.android.chat.model.Member;
import com.android.chat.model.Message;
import com.android.chat.ui.adapter.ConversationAdapter;
import com.android.chat.util.GPSTracker;
import com.android.chat.util.GlideUtils;
import com.android.chat.util.ImageUtils;
import com.android.chat.util.VideoUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener, MessageChildChangeListenerImpl.MessageChangeListener {
  private static final int PICK_IMAGE_REQUEST = 101;
  private static final int CAMERA_REQUEST = 102;
  private static final int PICK_VIDEO_REQUEST = 103;
  private RecyclerView recyclerChat;
  private ConversationAdapter adapter;
  private String roomId;
  private String roomAvatar;
  private ArrayList<CharSequence> idFriend;
  private List<Member> members = new ArrayList<>();
  private Conversation consersation;
  private ImageButton btnSend;
  private ImageButton btnPlus;
  private View layoutBottom;
  private EditText editWriteMessage;
  private LinearLayoutManager linearLayoutManager;
  public static HashMap<String, Bitmap> bitmapAvatarFriend;
  private Uri filePath;

  //Firebase
  private FirebaseStorage storage;
  private StorageReference storageReference;

  private GPSTracker gpsTracker;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_chat);
    initToolBar();

    Intent intentData = getIntent();
    idFriend = intentData.getCharSequenceArrayListExtra(StaticConfig.INTENT_KEY_CHAT_ID);
    roomId = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID);
    roomAvatar = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_AVATA);
    String nameFriend = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND);

    storage = FirebaseStorage.getInstance();
    storageReference = storage.getReference();

    consersation = new Conversation();
    btnSend = (ImageButton) findViewById(R.id.btnSend);
    btnPlus = (ImageButton) findViewById(R.id.btnPlus);
    layoutBottom = findViewById(R.id.bottom_layout);

    btnSend.setOnClickListener(this);
    btnPlus.setOnClickListener(this);

    findViewById(R.id.btn_camera).setOnClickListener(this);
    findViewById(R.id.btn_gallery).setOnClickListener(this);
    findViewById(R.id.btn_video).setOnClickListener(this);
    findViewById(R.id.btn_location).setOnClickListener(this);

    String userAvatar = SharedPreferenceHelper.getInstance(this).getUserInfo().avatar;

    editWriteMessage = (EditText) findViewById(R.id.editWriteMessage);
    editWriteMessage.setOnClickListener(this);

    gpsTracker = new GPSTracker(this);

    if (idFriend != null && nameFriend != null) {
      linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
      recyclerChat = (RecyclerView) findViewById(R.id.recyclerChat);
      recyclerChat.setLayoutManager(linearLayoutManager);
      adapter = new ConversationAdapter(this, consersation, userAvatar, storage);

      FirebaseDatabase.getInstance().getReference().child("group/" + roomId + "/member").addChildEventListener(new MessageMemberChangeListenerImpl(this, roomId, members));
      FirebaseDatabase.getInstance().getReference().child("message/" + roomId).addChildEventListener(new MessageChildChangeListenerImpl(this));
      recyclerChat.setAdapter(adapter);

      setToolbarTitle(nameFriend);
    }
  }


  @Override
  public void onChildAdded(Message message) {
    if (consersation.getListMessageData().contains(message)) {
      return;
    }
    consersation.getListMessageData().add(message);
    adapter.notifyDataSetChanged();
    linearLayoutManager.scrollToPosition(consersation.getListMessageData().size() - 1);
  }

  private void initToolBar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    toolbar.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent intent = new Intent(ChatActivity.this, GroupDetailActivity.class);
        intent.putExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID, roomId);
        startActivity(intent);
      }
    });

    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });
  }

  private void setToolbarTitle(String nameGroup) {
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      if (roomAvatar != null) {
        GlideUtils.setRoundedDrawable(this, roomAvatar, actionBar);
      }
      actionBar.setDisplayUseLogoEnabled(true);
      actionBar.setTitle(nameGroup);
      String subTitle = MemberDB.getInstance(this).getMembers(roomId).toString();
      actionBar.setSubtitle(subTitle.substring(1, subTitle.length() - 1));
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      Intent result = new Intent();
      result.putExtra("idFriend", idFriend.get(0));
      setResult(RESULT_OK, result);
      this.finish();
    }
    return true;
  }

  @Override
  public void onBackPressed() {
    Intent result = new Intent();
    result.putExtra("idFriend", idFriend.get(0));
    setResult(RESULT_OK, result);
    this.finish();
  }

  private void chooseImage() {
    Intent intent = new Intent();
// Show only images, no videos or anything else
    intent.setType("image/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
// Always show the chooser (if there are multiple options available)
    startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);

  }

  private void chooseVideo() {
    Intent intent = new Intent();
    intent.setType("video/*");
    intent.setAction(Intent.ACTION_GET_CONTENT);
    startActivityForResult(Intent.createChooser(intent, "Select Video"), PICK_VIDEO_REQUEST);
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
      Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
      cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
      startActivityForResult(cameraIntent, CAMERA_REQUEST);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void onClick(View view) {
    int id = view.getId();
    if (id == R.id.btnSend) {
      String content = editWriteMessage.getText().toString().trim();
      if (content.length() > 0) {
        editWriteMessage.setText("");
        new Message.Builder(StaticConfig.UID, roomId).type(ConversationAdapter.MESSAGE_TYPE_TEXT)
          .text(content).push(roomId);
      }
    } else if (id == R.id.btnPlus) {
      layoutBottom.setVisibility(View.VISIBLE);
    } else if (id == R.id.editWriteMessage) {
      layoutBottom.setVisibility(View.GONE);
    } else if (id == R.id.btn_camera) {
      captureImage();
      layoutBottom.setVisibility(View.GONE);
    } else if (id == R.id.btn_gallery) {
      chooseImage();
      layoutBottom.setVisibility(View.GONE);
    } else if (id == R.id.btn_video) {
      chooseVideo();
      layoutBottom.setVisibility(View.GONE);
    } else if (id == R.id.btn_location) {
      String uri = "https://maps.googleapis.com/maps/api/staticmap?zoom=17&scale=2&size=340x200&markers=color:red%7Clabel:C%7C" + gpsTracker.getLatitude() + "," + gpsTracker.getLongitude();
      new Message.Builder(StaticConfig.UID, roomId).type(ConversationAdapter.MESSAGE_TYPE_IMAGE)
        .thumbnail("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mNMugEAAaIBPGBKmFEAAAAASUVORK5CYII=")
        .fileName(UUID.randomUUID().toString() + ".vChat").localUri(uri).downloadUri(uri).push(roomId);

      layoutBottom.setVisibility(View.GONE);
    }
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode != RESULT_OK) return;

    if (requestCode == PICK_IMAGE_REQUEST && data != null && data.getData() != null) {
      filePath = data.getData();
      uploadImage(ConversationAdapter.MESSAGE_TYPE_IMAGE);
    } else if (requestCode == CAMERA_REQUEST) {
      filePath = Uri.fromFile(photoFile);
      uploadImage(ConversationAdapter.MESSAGE_TYPE_IMAGE);
    } else if (requestCode == PICK_VIDEO_REQUEST && data != null && data.getData() != null) {
      filePath = data.getData();
      uploadImage(ConversationAdapter.MESSAGE_TYPE_VIDEO);
    }
  }

  private void uploadImage(String type) {
    if (filePath != null) {
      new SendFile(this).roomId(roomId).adapter(adapter).uri(filePath)
        .storageReference(storageReference).linearLayoutManager(linearLayoutManager)
        .send(type);
    }
  }

  public static class SendFile {
    Context context;

    public SendFile(Context context) {
      this.context = context;
    }

    private ConversationAdapter adapter;

    public SendFile adapter(ConversationAdapter adapter) {
      this.adapter = adapter;
      return this;
    }

    private StorageReference storageReference;

    public SendFile storageReference(StorageReference storageReference) {
      this.storageReference = storageReference;
      return this;
    }

    private Uri filePath;

    public SendFile uri(Uri filePath) {
      this.filePath = filePath;
      return this;
    }

    private String roomId;

    public SendFile roomId(String roomId) {
      this.roomId = roomId;
      return this;
    }

    private LinearLayoutManager linearLayoutManager;

    public SendFile linearLayoutManager(LinearLayoutManager linearLayoutManager) {
      this.linearLayoutManager = linearLayoutManager;
      return this;
    }

    public void send(String type) {
      if (filePath == null) {
        throw new RuntimeException("file uri can't be null!!");
      }
      if (adapter == null) {
        throw new RuntimeException("Adapter can't be null!!!!");
      }
      switch (type) {
        case ConversationAdapter.MESSAGE_TYPE_IMAGE:
          adapter.addItem(uploadImage(filePath));
          linearLayoutManager.scrollToPosition(adapter.getItemCount() - 1);
          break;
        case ConversationAdapter.MESSAGE_TYPE_VIDEO:
          adapter.addItem(uploadVideo());
          linearLayoutManager.scrollToPosition(adapter.getItemCount() - 1);
          break;
      }
    }

    private Message uploadImage(Uri filePath) {
      final Message newMessage = new Message();
      newMessage.type = ConversationAdapter.MESSAGE_TYPE_IMAGE;
      newMessage.thumbnail = ImageUtils.getBase64String(context, filePath);
      newMessage.fileName = UUID.randomUUID().toString() + "_" + ImageUtils.getFileName(ImageUtils.getPath(context, filePath));
      newMessage.idSender = StaticConfig.UID;
      newMessage.idReceiver = roomId;
      newMessage.localUri = filePath.toString();
      newMessage.timestamp = System.currentTimeMillis();
      StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
      ref.putFile(filePath)
        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
          @Override
          public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            Log.i("Response", "url:" + taskSnapshot.getDownloadUrl());
            newMessage.downloadUri = taskSnapshot.getDownloadUrl().toString();
            newMessage.isUploading = false;
            adapter.notifyDataSetChanged();
            FirebaseDatabase.getInstance().getReference().child("message/" + roomId).push().setValue(newMessage);
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            newMessage.isUploading = false;
            adapter.notifyDataSetChanged();
          }
        })
        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
          @Override
          public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
              .getTotalByteCount());
          }
        });
      return newMessage;
    }

    private Message uploadVideo() {
      final Message newMessage = new Message();
      newMessage.type = ConversationAdapter.MESSAGE_TYPE_VIDEO;
      newMessage.fileName = UUID.randomUUID().toString() + "_" + ImageUtils.getFileName(ImageUtils.getPath(context, filePath));
      newMessage.idSender = StaticConfig.UID;
      newMessage.idReceiver = roomId;
      newMessage.localUri = VideoUtils.getVideoPath(context, filePath);
      newMessage.thumbnail = VideoUtils.getBase64StringThumbnail(newMessage.localUri);
      newMessage.timestamp = System.currentTimeMillis();
      StorageReference ref = storageReference.child("videos/" + UUID.randomUUID().toString());
      ref.putFile(filePath)
        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
          @Override
          public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
            Log.i("Response", "url:" + taskSnapshot.getDownloadUrl());
            newMessage.downloadUri = taskSnapshot.getDownloadUrl().toString();
            newMessage.isUploading = false;
            adapter.notifyDataSetChanged();
            FirebaseDatabase.getInstance().getReference().child("message/" + roomId).push().setValue(newMessage);
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            newMessage.isUploading = false;
            adapter.notifyDataSetChanged();
          }
        })
        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
          @Override
          public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
              .getTotalByteCount());
          }
        });
      return newMessage;
    }
  }
}