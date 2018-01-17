package com.android.pal.chat.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.pal.chat.R;
import com.android.pal.chat.base.StaticConfig;
import com.android.pal.chat.model.Conversation;
import com.android.pal.chat.model.Message;
import com.android.pal.chat.ui.activities.ImageViewActivity;
import com.android.pal.chat.ui.activities.VideoViewActivity;
import com.android.pal.chat.util.DateUtils;
import com.android.pal.chat.util.GlideUtils;
import com.android.pal.chat.util.ImageUtils;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;

/**
 * Created by admirar on 12/19/17.
 */

public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final String MESSAGE_TYPE_TEXT = "text";
    public static final String MESSAGE_TYPE_IMAGE = "image";
    public static final String MESSAGE_TYPE_VIDEO = "video";

    static final int VIEW_TYPE_USER_MESSAGE = 0;
    static final int VIEW_TYPE_FRIEND_MESSAGE = 1;

    private Context context;
    private Conversation consersation;
    private HashMap<String, DatabaseReference> bitmapAvataDB;
    private String userAvatar;
    private FirebaseStorage storage;

    public ConversationAdapter(Context context, Conversation consersation, String userAvatar, FirebaseStorage storage) {
        this.context = context;
        this.consersation = consersation;
        this.userAvatar = userAvatar;
        this.storage = storage;
        bitmapAvataDB = new HashMap<>();
    }

    public void addItem(Message message) {
        consersation.getListMessageData().add(message);
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_FRIEND_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.rc_item_message_friend, parent, false);
            return new ItemMessageFriendHolder(view);
        } else if (viewType == VIEW_TYPE_USER_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.rc_item_message_user, parent, false);
            return new ItemMessageUserHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_USER_MESSAGE:
                bindUserMessage((ItemMessageUserHolder) holder, position);
                break;
            case VIEW_TYPE_FRIEND_MESSAGE:
                bindFriendMessage((ItemMessageFriendHolder) holder, position);
                break;
        }
    }

    private void bindUserMessage(ItemMessageUserHolder holder, int position) {
        final Message message = consersation.getListMessageData().get(position);
        GlideUtils.display(context, userAvatar, holder.avatar, R.drawable.default_avatar);

        holder.txtTime.setText(DateUtils.format(message.timestamp, DateUtils.FORMAT_hh_mm_a));

        if (TextUtils.isEmpty(message.type) || message.type.equalsIgnoreCase(MESSAGE_TYPE_TEXT)) {
            holder.txtContent.setText(message.text);
            holder.txtContent.setVisibility(View.VISIBLE);
            holder.layoutImage.setVisibility(View.GONE);
        } else if (message.type.equalsIgnoreCase(MESSAGE_TYPE_IMAGE) || message.type.equalsIgnoreCase(MESSAGE_TYPE_VIDEO)) {
            holder.txtContent.setVisibility(View.GONE);
            holder.layoutImage.setVisibility(View.VISIBLE);
            holder.btnPlayVideo.setVisibility(message.type.equalsIgnoreCase(MESSAGE_TYPE_VIDEO) ? View.VISIBLE : View.GONE);
            holder.imageMessage.setImageBitmap(ImageUtils.getBitmap(message.thumbnail));
            //holder.imageMessage
            holder.progressBar.setVisibility(message.isUploading ? View.VISIBLE : View.GONE);

            Glide.with(context)
                    .load(message.localUri)
                    .into(holder.imageMessage);

            holder.imageMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (message.localUri.startsWith("https://maps.googleapis.com/maps/api/staticmap")) {
                        return;
                    }
                    Intent intent = new Intent(context,
                            message.type.equalsIgnoreCase(MESSAGE_TYPE_VIDEO) ? VideoViewActivity.class : ImageViewActivity.class);
                    intent.putExtra("uri", message.localUri);
                    intent.putExtra("is_local", true);
                    context.startActivity(intent);
                }
            });
        }

    }

    private void bindFriendMessage(final ItemMessageFriendHolder holder, int position) {
        final Message message = consersation.getListMessageData().get(position);
        GlideUtils.display(context, FirebaseDatabase.getInstance().getReference().child("user/" + message.idSender + "/avatar"),
                holder.avatar, R.drawable.default_avatar);

        holder.txtTime.setText(DateUtils.format(message.timestamp, DateUtils.FORMAT_hh_mm_a));
        if (TextUtils.isEmpty(message.type) || message.type.equalsIgnoreCase(MESSAGE_TYPE_TEXT)) {
            holder.txtContent.setText(message.text);
            holder.txtContent.setVisibility(View.VISIBLE);
            holder.layoutImage.setVisibility(View.GONE);
        } else if (message.type.equalsIgnoreCase(MESSAGE_TYPE_IMAGE) || message.type.equalsIgnoreCase(MESSAGE_TYPE_VIDEO)) {
            holder.txtContent.setVisibility(View.GONE);
            holder.layoutImage.setVisibility(View.VISIBLE);
            holder.btnPlayVideo.setVisibility(message.type.equalsIgnoreCase(MESSAGE_TYPE_VIDEO) ? View.VISIBLE : View.GONE);
            holder.imageMessage.setImageBitmap(ImageUtils.getBitmap(message.thumbnail));
            if (message.downloadUri != null) {
                try {
                    final StorageReference httpsReference = storage.getReferenceFromUrl(message.downloadUri);
                    final File file = new File(ImageUtils.getChatFolder(message.type.equalsIgnoreCase(MESSAGE_TYPE_IMAGE) ? "images" : "videos")
                            + File.separator + message.fileName);
                    if (file.exists()) {
                        Glide.with(context)
                                .load(Uri.fromFile(file))
                                .into(holder.imageMessage);
                    } else if (message.localUri.startsWith("https://maps.googleapis.com/maps/api/staticmap")) {
                        Glide.with(context)
                                .load(message.localUri)
                                .into(holder.imageMessage);
                    } else if (!message.isUploading) {
                        message.isUploading = true;
                        httpsReference.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                message.isUploading = false;
                                notifyDataSetChanged();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                message.isUploading = false;
                            }
                        });
                        notifyDataSetChanged();
                    }
                    holder.imageMessage.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (file.exists()) {
                                Intent intent = new Intent(context,
                                        message.type.equalsIgnoreCase(MESSAGE_TYPE_VIDEO) ? VideoViewActivity.class : ImageViewActivity.class);
                                intent.putExtra("uri", Uri.fromFile(file).toString());
                                intent.putExtra("is_local", true);
                                context.startActivity(intent);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return consersation.getListMessageData().get(position).idSender.equals(StaticConfig.UID) ? VIEW_TYPE_USER_MESSAGE : VIEW_TYPE_FRIEND_MESSAGE;
    }

    @Override
    public int getItemCount() {
        return consersation.getListMessageData().size();
    }
}

class ItemMessageUserHolder extends RecyclerView.ViewHolder {
    final TextView txtContent;
    final TextView txtTime;
    final ImageView avatar;
    final ImageView imageMessage;
    final View layoutImage;
    final ProgressBar progressBar;
    final View btnPlayVideo;

    public ItemMessageUserHolder(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.textContentUser);
        txtTime = (TextView) itemView.findViewById(R.id.textview_time);
        avatar = (ImageView) itemView.findViewById(R.id.imageView2);
        imageMessage = (ImageView) itemView.findViewById(R.id.imageContentUser);
        layoutImage = itemView.findViewById(R.id.layout_image);
        btnPlayVideo = itemView.findViewById(R.id.btn_play_video);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progress);
    }
}

class ItemMessageFriendHolder extends RecyclerView.ViewHolder {
    final TextView txtContent;
    final TextView txtTime;
    final ImageView avatar;
    final View layoutImage;
    final ProgressBar progressBar;
    final ImageView imageMessage;
    final View btnPlayVideo;

    public ItemMessageFriendHolder(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.textContentFriend);
        txtTime = (TextView) itemView.findViewById(R.id.textview_time);
        avatar = (ImageView) itemView.findViewById(R.id.imageView3);
        imageMessage = (ImageView) itemView.findViewById(R.id.imageContentFriend);
        layoutImage = itemView.findViewById(R.id.layout_image);
        btnPlayVideo = itemView.findViewById(R.id.btn_play_video);
        progressBar = (ProgressBar) itemView.findViewById(R.id.progress);
    }
}
