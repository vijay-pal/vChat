package com.android.chat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.android.chat.data.StaticConfig;
import com.android.chat.service.ServiceUtils;
import com.android.chat.ui.AddGroupActivity;
import com.android.chat.ui.GroupFragment;
import com.android.chat.ui.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {
  private static String TAG = "HomeActivity";

  private FirebaseAuth mAuth;
  private FirebaseAuth.AuthStateListener mAuthListener;
  private FirebaseUser user;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initToolBar();
    initFirebase();
    initFragment();
  }

  private void initToolBar() {
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    ActionBar actionBar = getSupportActionBar();
    if (actionBar != null) {
      actionBar.setTitle("vChat");
      actionBar.setDisplayHomeAsUpEnabled(true);
    }
    toolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        finish();
      }
    });

  }

  private void initFirebase() {
    //Khoi tao thanh phan de dang nhap, dang ky
    mAuth = FirebaseAuth.getInstance();
    mAuthListener = new FirebaseAuth.AuthStateListener() {
      @Override
      public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        user = firebaseAuth.getCurrentUser();
        if (user != null) {
          StaticConfig.UID = user.getUid();
        } else {
          HomeActivity.this.finish();
          // User is signed in
          startActivity(new Intent(HomeActivity.this, LoginActivity.class));
          Log.d(TAG, "onAuthStateChanged:signed_out");
        }
      }
    };
  }

  private void initFragment() {
    getSupportFragmentManager().beginTransaction().replace(
      R.id.container, new GroupFragment()
    ).commit();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mAuth.addAuthStateListener(mAuthListener);
    ServiceUtils.stopServiceFriendChat(getApplicationContext(), false);
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mAuthListener != null) {
      mAuth.removeAuthStateListener(mAuthListener);
    }
  }

  @Override
  protected void onDestroy() {
    ServiceUtils.startServiceFriendChat(getApplicationContext());
    super.onDestroy();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu_main, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.actionAbout) {
      Toast.makeText(this, "vChat version 1.0", Toast.LENGTH_LONG).show();
      return true;
    } else if (id == R.id.actionCreateGroup) {
      startActivity(new Intent(this, AddGroupActivity.class));
      return true;
    }

    return super.onOptionsItemSelected(item);
  }
}