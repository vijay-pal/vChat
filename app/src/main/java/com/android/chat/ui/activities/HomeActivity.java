package com.android.chat.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;
import android.widget.Toast;

import com.android.chat.BuildConfig;
import com.android.chat.R;
import com.android.chat.data.ChatRoomDB;
import com.android.chat.data.StaticConfig;
import com.android.chat.data.firebase.ChatRoomValueInitializer;
import com.android.chat.data.firebase.GroupValueEventListenerImpl;
import com.android.chat.model.ChatRoom;
import com.android.chat.service.ServiceUtils;
import com.android.chat.ui.adapter.ChatRoomListAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class HomeActivity extends AppCompatActivity implements GroupValueEventListenerImpl.GroupRefreshCompletedListener,
        SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {
    private static String TAG = "HomeActivity";

    private List<ChatRoom> chatRooms;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;

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
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    StaticConfig.UID = user.getUid();
                    if (chatRooms.isEmpty()) {
                        onRefresh();
                    }
                } else {
                    HomeActivity.this.finish();
                    // User is signed in
                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                }
            }
        };
    }

    private void initComponent() {
        chatRooms = ChatRoomDB.getInstance(HomeActivity.this).getChatRooms();
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerChat);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new ChatRoomListAdapter(this, chatRooms);
        recyclerView.setAdapter(mAdapter);
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
        SearchView searchView = (SearchView) menu.findItem(R.id.actionSearch).getActionView();
        searchView.setOnQueryTextListener(this);
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
        mAdapter.changeDataSet(chatRooms);
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
            new ChatRoomValueInitializer(HomeActivity.this, HomeActivity.this);
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
}