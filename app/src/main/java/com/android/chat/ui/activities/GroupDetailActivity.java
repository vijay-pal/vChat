package com.android.chat.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import com.android.chat.R;
import com.android.chat.data.GroupDB;
import com.android.chat.data.MemberDB;
import com.android.chat.data.StaticConfig;
import com.android.chat.model.Group;
import com.android.chat.ui.adapter.GroupMemberAdapter;
import com.android.chat.util.GlideUtils;

/**
 * Created by admirar on 1/7/18.
 */

public class GroupDetailActivity extends AppCompatActivity {

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
}
