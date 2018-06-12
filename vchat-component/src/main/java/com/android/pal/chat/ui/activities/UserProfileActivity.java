package com.android.pal.chat.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.pal.chat.R;
import com.android.pal.chat.base.BaseActivity;
import com.android.pal.chat.base.StaticConfig;
import com.android.pal.chat.base.data.SharedPreferenceHelper;
import com.android.pal.chat.base.model.User;
import com.android.pal.chat.data.FriendDB;
import com.android.pal.chat.data.GroupDB;
import com.android.pal.chat.model.Configuration;
import com.android.pal.chat.service.ServiceUtils;
import com.android.pal.chat.util.ImageUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class UserProfileActivity extends BaseActivity {
  private ImageView avatar;

  private List<Configuration> listConfig = new ArrayList<>();
  private RecyclerView recyclerView;
  private UserInfoAdapter infoAdapter;

  private static final String USERNAME_LABEL = "Username";
  private static final String EMAIL_LABEL = "Email";
  private static final String SIGNOUT_LABEL = "Sign out";
  private static final String RESETPASS_LABEL = "Change Password";

  private static final int PICK_IMAGE = 1994;
  private ProgressDialog waitingDialog;

  private DatabaseReference userDB;
  private FirebaseAuth mAuth;
  private User myAccount;

  private CollapsingToolbarLayout collapsingToolbarLayout;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_info);

    userDB = FirebaseDatabase.getInstance().getReference().child("user").child(StaticConfig.UID);
    SharedPreferenceHelper prefHelper = SharedPreferenceHelper.getInstance(this);
    myAccount = prefHelper.getUserInfo();
    initToolbar();
    userDB.addListenerForSingleValueEvent(userListener);
    mAuth = FirebaseAuth.getInstance();

    // Inflate the layout for this fragment
    avatar = (ImageView) findViewById(R.id.img_avatar);
    avatar.setOnClickListener(onAvatarClick);

    setupArrayListInfo(myAccount);
    setImageAvatar(this, myAccount.avatar);

    recyclerView = (RecyclerView) findViewById(R.id.info_recycler_view);
    infoAdapter = new UserInfoAdapter(listConfig);
    RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setItemAnimator(new DefaultItemAnimator());
    recyclerView.setAdapter(infoAdapter);

    waitingDialog = new ProgressDialog(this);
  }

  private void initToolbar() {
    enableToolbar(R.id.toolbar);
    collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
    collapsingToolbarLayout.setTitle(myAccount.name);
  }

  private ValueEventListener userListener = new ValueEventListener() {
    @Override
    public void onDataChange(DataSnapshot dataSnapshot) {
      listConfig.clear();
      myAccount = dataSnapshot.getValue(User.class);

      setupArrayListInfo(myAccount);
      if (infoAdapter != null) {
        infoAdapter.notifyDataSetChanged();
      }

      setImageAvatar(UserProfileActivity.this, myAccount.avatar);
      SharedPreferenceHelper preferenceHelper = SharedPreferenceHelper.getInstance(UserProfileActivity.this);
      preferenceHelper.saveUserInfo(myAccount);
    }

    @Override
    public void onCancelled(DatabaseError databaseError) {
    }
  };

  private View.OnClickListener onAvatarClick = new View.OnClickListener() {
    @Override
    public void onClick(View view) {

      new AlertDialog.Builder(UserProfileActivity.this)
          .setTitle("Avatar")
          .setMessage("Are you sure want to change avatar profile?")
          .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
              Intent intent = new Intent();
              intent.setType("image/*");
              intent.setAction(Intent.ACTION_PICK);
              startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
              dialogInterface.dismiss();
            }
          })
          .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
              dialogInterface.dismiss();
            }
          }).show();
    }
  };

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
      if (data == null) {
        Toast.makeText(this, "Có lỗi xảy ra, vui lòng thử lại", Toast.LENGTH_LONG).show();
        return;
      }
      try {
        InputStream inputStream = getContentResolver().openInputStream(data.getData());

        Bitmap imgBitmap = BitmapFactory.decodeStream(inputStream);
        imgBitmap = ImageUtils.cropToSquare(imgBitmap);
        InputStream is = ImageUtils.convertBitmapToInputStream(imgBitmap);
        final Bitmap liteImage = ImageUtils.makeImageLite(is,
            imgBitmap.getWidth(), imgBitmap.getHeight(),
            ImageUtils.AVATAR_WIDTH, ImageUtils.AVATAR_HEIGHT);

        String imageBase64 = ImageUtils.encodeBase64(liteImage);
        myAccount.avatar = imageBase64;

        waitingDialog.setCancelable(false);
        waitingDialog.setTitle("Avatar updating....");
        waitingDialog.show();

        userDB.child("avatar").setValue(imageBase64)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
              @Override
              public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                  waitingDialog.dismiss();
                  SharedPreferenceHelper preferenceHelper = SharedPreferenceHelper.getInstance(UserProfileActivity.this);
                  preferenceHelper.saveUserInfo(myAccount);
                  avatar.setImageDrawable(ImageUtils.roundedImage(UserProfileActivity.this, liteImage));

                  Toast.makeText(UserProfileActivity.this, "Update avatar successfully!", Toast.LENGTH_LONG).show();
                }
              }
            })
            .addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(@NonNull Exception e) {
                waitingDialog.dismiss();
                Toast.makeText(UserProfileActivity.this, "Failed to update avatar", Toast.LENGTH_LONG).show();
              }
            });
      } catch (FileNotFoundException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * @param myAccount
   */
  public void setupArrayListInfo(User myAccount) {
    listConfig.clear();
    Configuration userNameConfig = new Configuration(USERNAME_LABEL, myAccount.name, R.drawable.ic_account_circle_black_24dp);
    listConfig.add(userNameConfig);

    Configuration emailConfig = new Configuration(EMAIL_LABEL, myAccount.email, R.drawable.ic_email_black_24dp);
    listConfig.add(emailConfig);

    Configuration resetPass = new Configuration(RESETPASS_LABEL, "", R.drawable.ic_update_black_24dp);
    listConfig.add(resetPass);

    Configuration signOut = new Configuration(SIGNOUT_LABEL, "", R.drawable.ic_power_settings_new_black_24dp);
    listConfig.add(signOut);
  }

  private void setImageAvatar(Context context, String imgBase64) {
    try {
      Resources res = getResources();
      Bitmap src;
      if (imgBase64.equals("default")) {
        src = BitmapFactory.decodeResource(res, R.drawable.default_avatar);
      } else {
        byte[] decodedString = Base64.decode(imgBase64, Base64.DEFAULT);
        src = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
      }

      avatar.setImageDrawable(ImageUtils.roundedImage(context, src));
    } catch (Exception e) {
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
  }

  public class UserInfoAdapter extends RecyclerView.Adapter<UserInfoAdapter.ViewHolder> {
    private List<Configuration> profileConfig;

    public UserInfoAdapter(List<Configuration> profileConfig) {
      this.profileConfig = profileConfig;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View itemView = LayoutInflater.from(parent.getContext())
          .inflate(R.layout.list_info_item_layout, parent, false);
      return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
      final Configuration config = profileConfig.get(position);
      holder.label.setText(config.getLabel());
      holder.value.setText(config.getValue());
      holder.icon.setImageResource(config.getIcon());
      ((RelativeLayout) holder.label.getParent()).setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
          if (config.getLabel().equals(SIGNOUT_LABEL)) {
            FirebaseAuth.getInstance().signOut();
            FriendDB.getInstance(UserProfileActivity.this).dropDB();
            GroupDB.getInstance(UserProfileActivity.this).dropDB();
            ServiceUtils.stopServiceFriendChat(UserProfileActivity.this.getApplicationContext(), true);
            UserProfileActivity.this.finish();
          }

          if (config.getLabel().equals(USERNAME_LABEL)) {
            View vewInflater = LayoutInflater.from(UserProfileActivity.this)
                .inflate(R.layout.dialog_edit_username, null);
            final EditText input = (EditText) vewInflater.findViewById(R.id.edit_username);
            input.setText(myAccount.name);
            new AlertDialog.Builder(UserProfileActivity.this)
                .setTitle("Edit username")
                .setView(vewInflater)
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialogInterface, int i) {
                    String newName = input.getText().toString();
                    if (!myAccount.name.equals(newName)) {
                      changeUserName(newName);
                    }
                    dialogInterface.dismiss();
                  }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                  }
                }).show();
          }

          if (config.getLabel().equals(RESETPASS_LABEL)) {
            new AlertDialog.Builder(UserProfileActivity.this)
                .setTitle("Password")
                .setMessage("Are you sure want to reset password?")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialogInterface, int i) {
                    resetPassword(myAccount.email);
                    dialogInterface.dismiss();
                  }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                  @Override
                  public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                  }
                }).show();
          }
        }
      });
    }

    /**
     * Cập nhật username mới vào SharedPreference và thay đổi trên giao diện
     */
    private void changeUserName(String newName) {
      userDB.child("name").setValue(newName);


      myAccount.name = newName;
      SharedPreferenceHelper prefHelper = SharedPreferenceHelper.getInstance(UserProfileActivity.this);
      prefHelper.saveUserInfo(myAccount);

//      tvUserName.setText(newName);
      setupArrayListInfo(myAccount);
    }

    void resetPassword(final String email) {
      mAuth.sendPasswordResetEmail(email)
          .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
              Toast.makeText(UserProfileActivity.this, "Recovery email sent to " + email, Toast.LENGTH_LONG).show();
            }
          })
          .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
              Toast.makeText(UserProfileActivity.this, "Failed to sending recovery email sent to " + email, Toast.LENGTH_LONG).show();
            }
          });
    }

    @Override
    public int getItemCount() {
      return profileConfig.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
      // each data item is just a string in this case
      public TextView label, value;
      public ImageView icon;

      public ViewHolder(View view) {
        super(view);
        label = (TextView) view.findViewById(R.id.tv_title);
        value = (TextView) view.findViewById(R.id.tv_detail);
        icon = (ImageView) view.findViewById(R.id.img_icon);
      }
    }

  }

}
