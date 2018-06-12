package com.android.pal.chat.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.pal.chat.BuildConfig;
import com.android.pal.chat.R;
import com.android.pal.chat.base.BaseActivity;
import com.android.pal.chat.base.StaticConfig;
import com.android.pal.chat.data.ChatRoomDB;
import com.android.pal.chat.data.firebase.ChatRoomValueInitializer;
import com.android.pal.chat.data.firebase.GroupValueEventListenerImpl;
import com.android.pal.chat.data.firebase.SearchPeopleValueEvent;
import com.android.pal.chat.model.ChatRoom;
import com.android.pal.chat.service.LoginAuth;
import com.android.pal.chat.service.ServiceUtils;
import com.android.pal.chat.ui.adapter.ChatRoomListAdapter;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ChatRoomsActivity extends BaseActivity implements GroupValueEventListenerImpl.GroupRefreshCompletedListener,
  SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener, SearchPeopleValueEvent.SearchPeopleListener,
  LoginAuth.UserSessionListener {

  private List<ChatRoom> chatRooms;
  private LoginAuth loginAuth;

  private SwipeRefreshLayout mSwipeRefreshLayout;
  private ChatRoomListAdapter mAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    initToolBar();
    initComponent();
    initFirebase();
  }

  private void initToolBar() {
    Toolbar toolbar = findViewById(R.id.toolbar);
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
    loginAuth = new LoginAuth(this, this);
  }

  private void initComponent() {
    chatRooms = ChatRoomDB.getInstance(ChatRoomsActivity.this).getChatRooms();
    mSwipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
    mSwipeRefreshLayout.setOnRefreshListener(this);
    RecyclerView recyclerView = findViewById(R.id.recyclerChat);
    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    mAdapter = new ChatRoomListAdapter(this, chatRooms, this);
    recyclerView.setAdapter(mAdapter);
  }

  @Override
  protected void onStart() {
    super.onStart();
    loginAuth.addAuthStateListener();
    ServiceUtils.stopServiceFriendChat(getApplicationContext(), false);
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (loginAuth != null) {
      loginAuth.removeAuthStateListener();
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

    if (!StaticConfig.IS_ENABLE_SEARCH_OPTION) {
      menu.findItem(R.id.actionSearch).setVisible(false);
    } else {
      SearchView searchView = (SearchView) menu.findItem(R.id.actionSearch).getActionView();
      searchView.setOnQueryTextListener(this);
    }
    if (!StaticConfig.IS_ENABLE_ABOUT_OPTION) {
      menu.findItem(R.id.actionAbout).setVisible(false);
    }
    if (!StaticConfig.IS_ENABLE_CREATE_GROUP) {
      menu.findItem(R.id.actionCreateGroup).setVisible(false);
    }
    if (!StaticConfig.IS_ENABLE_MY_PROFILE) {
      menu.findItem(R.id.actionSettings).setVisible(false);
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == R.id.actionAbout) {
      Toast.makeText(this, "vChat version " + BuildConfig.VERSION_NAME, Toast.LENGTH_LONG).show();
      return true;
    } else if (id == R.id.actionCreateGroup) {
      startActivity(new Intent(this, AddGroupActivity.class));
      return true;
    } else if (id == R.id.actionSettings) {
      startActivity(new Intent(this, UserProfileActivity.class));
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onCompleted(List<ChatRoom> chatRooms) {
    ChatRoomDB.getInstance(this).deleteAll();
    ChatRoomDB.getInstance(this).addChatRooms(chatRooms);
    mAdapter.changeDataSet(chatRooms, true);
    mSwipeRefreshLayout.setRefreshing(false);
  }

  @Override
  public void onCancelled() {
    mSwipeRefreshLayout.setRefreshing(false);
  }

  @Override
  public void fetchFriend() {

  }

  @Override
  public void onRefresh() {
    if (ServiceUtils.isNetworkConnected(this)) {
      mSwipeRefreshLayout.setRefreshing(true);
      new ChatRoomValueInitializer(ChatRoomsActivity.this, ChatRoomsActivity.this);
    }
  }

  @Override
  public boolean onQueryTextSubmit(String query) {
    return false;
  }

  @Override
  public boolean onQueryTextChange(String newText) {
    mAdapter.getFilter().filter(newText);
    return true;
  }

  @Override
  public void onSearchCompleted(List<ChatRoom> chatRooms) {
    mAdapter.changeDataSet(chatRooms, false);
  }

  @Override
  public void onSearchCancelled() {

  }

  @Override
  public void result(FirebaseUser user) {
    StaticConfig.UID = user.getUid();
    if (chatRooms.isEmpty()) {
      onRefresh();
    }
  }

  @Override
  public void sessionExpired() {
    finish();
  }

  @Override
  public void userNotExits() {

  }
}