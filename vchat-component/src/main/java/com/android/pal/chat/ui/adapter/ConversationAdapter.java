package com.android.pal.chat.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.android.pal.chat.R;
import com.android.pal.chat.base.StaticConfig;
import com.android.pal.chat.base.Utility;
import com.android.pal.chat.data.MemberDB;
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
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by admirar on 12/19/17.
 */

public class ConversationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements SeekBar.OnSeekBarChangeListener {

  public static final String MESSAGE_TYPE_TEXT = "text";
  public static final String MESSAGE_TYPE_IMAGE = "image";
  public static final String MESSAGE_TYPE_VIDEO = "video";
  public static final String MESSAGE_TYPE_AUDIO = "audio";

  static final int VIEW_TYPE_USER_TEXT_MESSAGE = 0;
  static final int VIEW_TYPE_FRIEND_TEXT_MESSAGE = 1;
  static final int VIEW_TYPE_USER_IMAGE_MESSAGE = 2;
  static final int VIEW_TYPE_FRIEND_IMAGE_MESSAGE = 3;
  static final int VIEW_TYPE_USER_VIDEO_MESSAGE = 4;
  static final int VIEW_TYPE_FRIEND_VIDEO_MESSAGE = 5;
  static final int VIEW_TYPE_USER_AUDIO_MESSAGE = 6;
  static final int VIEW_TYPE_FRIEND_AUDIO_MESSAGE = 7;

  private Context context;
  private Conversation consersation;
  private HashMap<String, DatabaseReference> bitmapAvataDB;
  private String userAvatar;
  private FirebaseStorage storage;
  private MediaPlayer mediaPlayer;

  public ConversationAdapter(Context context, Conversation consersation, String userAvatar, FirebaseStorage storage) {
    this.context = context;
    this.consersation = consersation;
    this.userAvatar = userAvatar;
    this.storage = storage;
    bitmapAvataDB = new HashMap<>();
    mediaPlayer = new MediaPlayer();
  }

  public void addItem(Message message) {
    consersation.getListMessageData().add(message);
    notifyDataSetChanged();
  }

  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    switch (viewType) {
      case VIEW_TYPE_USER_TEXT_MESSAGE:
        return new ItemTextMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.rc_item_text_message_user, parent, false));

      case VIEW_TYPE_FRIEND_TEXT_MESSAGE:
        return new ItemTextMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.rc_item_text_message_friend, parent, false));

      case VIEW_TYPE_USER_IMAGE_MESSAGE:
        return new ItemImageMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.rc_item_image_message_user, parent, false));

      case VIEW_TYPE_FRIEND_IMAGE_MESSAGE:
        return new ItemImageMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.rc_item_image_message_friend, parent, false));

      case VIEW_TYPE_USER_VIDEO_MESSAGE:
        return new ItemVideoMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.rc_item_video_message_user, parent, false));

      case VIEW_TYPE_FRIEND_VIDEO_MESSAGE:
        return new ItemVideoMessageViewHolder(LayoutInflater.from(context).inflate(R.layout.rc_item_video_message_friend, parent, false));

      case VIEW_TYPE_USER_AUDIO_MESSAGE:
        return new ItemAudioViewHolder(LayoutInflater.from(context).inflate(R.layout.rc_item_audio_message_user, parent, false), mediaPlayer);

      case VIEW_TYPE_FRIEND_AUDIO_MESSAGE:
        return new ItemAudioViewHolder(LayoutInflater.from(context).inflate(R.layout.rc_item_audio_message_friend, parent, false), mediaPlayer);
    }
    return new RecyclerView.ViewHolder(new View(parent.getContext())) {
    };
  }

  @Override
  public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
    switch (holder.getItemViewType()) {
      case VIEW_TYPE_USER_TEXT_MESSAGE:
        bindTextMessageUser((ItemTextMessageViewHolder) holder, consersation.getListMessageData().get(position));
        break;
      case VIEW_TYPE_FRIEND_TEXT_MESSAGE:
        bindTextMessageFriend((ItemTextMessageViewHolder) holder, consersation.getListMessageData().get(position));
        break;

      case VIEW_TYPE_USER_IMAGE_MESSAGE:
        bindImageMessageUser((ItemImageMessageViewHolder) holder, consersation.getListMessageData().get(position));
        break;
      case VIEW_TYPE_FRIEND_IMAGE_MESSAGE:
        bindImageMessageFriend((ItemImageMessageViewHolder) holder, consersation.getListMessageData().get(position));
        break;

      case VIEW_TYPE_USER_VIDEO_MESSAGE:
        bindVideoMessageUser((ItemVideoMessageViewHolder) holder, consersation.getListMessageData().get(position));
        break;
      case VIEW_TYPE_FRIEND_VIDEO_MESSAGE:
        bindVideoMessageFriend((ItemVideoMessageViewHolder) holder, consersation.getListMessageData().get(position));
        break;

      case VIEW_TYPE_USER_AUDIO_MESSAGE:
        bindAudioMessageUser((ItemAudioViewHolder) holder, consersation.getListMessageData().get(position));
        break;

      case VIEW_TYPE_FRIEND_AUDIO_MESSAGE:
        bindAudioMessageFriend((ItemAudioViewHolder) holder, consersation.getListMessageData().get(position));
        break;
    }
  }

  private void bindTextMessageUser(ItemTextMessageViewHolder holder, Message message) {
//    GlideUtils.display(context, userAvatar, holder.avatar, R.drawable.default_avatar);
    holder.txtContent.setText(message.text);
    holder.txtTime.setText(DateUtils.format(message.timestamp, DateUtils.FORMAT_hh_mm_a));
  }

  private void bindTextMessageFriend(ItemTextMessageViewHolder holder, Message message) {
    holder.userName.setText(MemberDB.getInstance(context).getMember(message.idSender).toString());
    holder.userName.setTextColor(Utility.getColor(message.idSender.hashCode()));
   /* GlideUtils.display(context, FirebaseDatabase.getInstance().getReference().child("user/" + message.idSender + "/avatar"),
      holder.avatar, R.drawable.default_avatar);*/
    holder.txtContent.setText(message.text);
    holder.txtTime.setText(DateUtils.format(message.timestamp, DateUtils.FORMAT_hh_mm_a));
  }

  private void bindImageMessageUser(ItemImageMessageViewHolder holder, final Message message) {
//    GlideUtils.display(context, userAvatar, holder.avatar, R.drawable.default_avatar);
    holder.txtTime.setText(DateUtils.format(message.timestamp, DateUtils.FORMAT_hh_mm_a));
    holder.imageMessage.setImageBitmap(ImageUtils.getBitmap(message.thumbnail));
    //holder.imageMessage
    holder.progressBar.setVisibility(message.isUploading ? View.VISIBLE : View.GONE);
    Glide.with(context)
      .load(message.localUri)
      .into(holder.imageMessage);

    holder.imageMessage.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        open(message);
      }
    });
  }

  private void bindImageMessageFriend(ItemImageMessageViewHolder holder, final Message message) {
    holder.userName.setText(MemberDB.getInstance(context).getMember(message.idSender).toString());
    holder.userName.setTextColor(message.idSender.hashCode());
    /*GlideUtils.display(context, FirebaseDatabase.getInstance().getReference().child("user/" + message.idSender + "/avatar"),
      holder.avatar, R.drawable.default_avatar);*/
    holder.txtTime.setText(DateUtils.format(message.timestamp, DateUtils.FORMAT_hh_mm_a));
    holder.imageMessage.setImageBitmap(ImageUtils.getBitmap(message.thumbnail));
    //holder.imageMessage
    holder.progressBar.setVisibility(message.isUploading ? View.VISIBLE : View.GONE);
    if (message.downloadUri != null) {
      try {
        final StorageReference httpsReference = storage.getReferenceFromUrl(message.downloadUri);
        final File file = new File(ImageUtils.getChatFolder("images")
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
//          notifyDataSetChanged();
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

  private void bindVideoMessageUser(ItemVideoMessageViewHolder holder, final Message message) {
//    GlideUtils.display(context, userAvatar, holder.avatar, R.drawable.default_avatar);
    holder.txtTime.setText(DateUtils.format(message.timestamp, DateUtils.FORMAT_hh_mm_a));
    holder.imageMessage.setImageBitmap(ImageUtils.getBitmap(message.thumbnail));
    //holder.imageMessage
    holder.progressBar.setVisibility(message.isUploading ? View.VISIBLE : View.GONE);
    holder.btnPlayVideo.setVisibility(View.VISIBLE);

    Glide.with(context)
      .load(message.localUri)
      .into(holder.imageMessage);
    holder.btnPlayVideo.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        open(message);
      }
    });
    holder.imageMessage.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        open(message);
      }
    });
  }

  private void bindVideoMessageFriend(ItemVideoMessageViewHolder holder, final Message message) {
    holder.userName.setText(MemberDB.getInstance(context).getMember(message.idSender).toString());
    holder.userName.setTextColor(message.idSender.hashCode());
   /* GlideUtils.display(context, FirebaseDatabase.getInstance().getReference().child("user/" + message.idSender + "/avatar"),
      holder.avatar, R.drawable.default_avatar);*/
    holder.txtTime.setText(DateUtils.format(message.timestamp, DateUtils.FORMAT_hh_mm_a));
    holder.imageMessage.setImageBitmap(ImageUtils.getBitmap(message.thumbnail));
    if (message.downloadUri != null) {
      try {
        final StorageReference httpsReference = storage.getReferenceFromUrl(message.downloadUri);
        final File file = new File(ImageUtils.getChatFolder("videos")
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
        holder.btnPlayVideo.setVisibility(file.exists() ? View.VISIBLE : View.GONE);
        holder.btnPlayVideo.setOnClickListener(new View.OnClickListener() {
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

  private void bindAudioMessageUser(final ItemAudioViewHolder holder, final Message message) {
//    GlideUtils.display(context, userAvatar, holder.avatar, R.drawable.default_avatar);
    holder.txtTime.setText(DateUtils.format(message.timestamp, DateUtils.FORMAT_hh_mm_a));
    //holder.imageMessage
    holder.btnPlayAudio.setImageResource(android.R.drawable.ic_media_play);

    holder.btnPlayAudio.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mediaPlayer.isPlaying()) {
          mediaPlayer.pause();
          holder.removeUpdateSeekBar();
          holder.btnPlayAudio.setImageResource(android.R.drawable.ic_media_play);
        } else {
          try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(context, Uri.parse(message.localUri));
            mediaPlayer.prepare();
            mediaPlayer.start();
            holder.seekBar.setMax(mediaPlayer.getDuration());
            holder.updateSeekBar();
          } catch (IOException e) {
            e.printStackTrace();
          }
          holder.btnPlayAudio.setImageResource(android.R.drawable.ic_media_pause);
        }
      }
    });
  }

  @Override
  public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
    super.onViewDetachedFromWindow(holder);
    try {
      if (holder.getItemViewType() == VIEW_TYPE_FRIEND_AUDIO_MESSAGE
        || holder.getItemViewType() == VIEW_TYPE_USER_AUDIO_MESSAGE) {
        if (mediaPlayer.isPlaying()) {
          if (holder instanceof ItemAudioViewHolder) {
            ((ItemAudioViewHolder) holder).removeUpdateSeekBar();
          }
          mediaPlayer.stop();
          mediaPlayer.reset();
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void bindAudioMessageFriend(final ItemAudioViewHolder holder, final Message message) {
    holder.userName.setText(MemberDB.getInstance(context).getMember(message.idSender).toString());
    holder.userName.setTextColor(message.idSender.hashCode());
    /*GlideUtils.display(context, FirebaseDatabase.getInstance().getReference().child("user/" + message.idSender + "/avatar"),
      holder.avatar, R.drawable.default_avatar);*/
    holder.txtTime.setText(DateUtils.format(message.timestamp, DateUtils.FORMAT_hh_mm_a));
    holder.btnPlayAudio.setImageResource(android.R.drawable.ic_media_play);

    if (message.downloadUri != null) {
      try {
        final StorageReference httpsReference = storage.getReferenceFromUrl(message.downloadUri);
        final File file = new File(ImageUtils.getChatFolder("audios")
          + File.separator + message.fileName);
        if (!file.exists() && !message.isUploading) {
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

        holder.btnPlayAudio.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            if (file.exists()) {
              if (mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                holder.btnPlayAudio.setImageResource(android.R.drawable.ic_media_play);
              } else {
                try {
                  mediaPlayer.reset();
                  mediaPlayer.setDataSource(context, Uri.fromFile(file));
                  mediaPlayer.prepare();
                  mediaPlayer.start();
                  holder.seekBar.setMax(mediaPlayer.getDuration());
                } catch (IOException e) {
                  e.printStackTrace();
                }
                holder.btnPlayAudio.setImageResource(android.R.drawable.ic_media_pause);
              }
            }
          }
        });
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  private void open(Message message) {
    if (message.localUri.startsWith("https://maps.googleapis.com/maps/api/staticmap")) {
      return;
    }
    Intent intent = new Intent(context,
      message.type.equalsIgnoreCase(MESSAGE_TYPE_VIDEO) ? VideoViewActivity.class : ImageViewActivity.class);
    intent.putExtra("uri", message.localUri);
    intent.putExtra("is_local", true);
    context.startActivity(intent);
  }

  @Override
  public int getItemViewType(int position) {
    Message message = consersation.getListMessageData().get(position);
    switch (message.type) {
      case MESSAGE_TYPE_TEXT: {
        return message.idSender.equals(StaticConfig.UID) ? VIEW_TYPE_USER_TEXT_MESSAGE : VIEW_TYPE_FRIEND_TEXT_MESSAGE;
      }
      case MESSAGE_TYPE_IMAGE: {
        return message.idSender.equals(StaticConfig.UID) ? VIEW_TYPE_USER_IMAGE_MESSAGE : VIEW_TYPE_FRIEND_IMAGE_MESSAGE;
      }
      case MESSAGE_TYPE_VIDEO: {
        return message.idSender.equals(StaticConfig.UID) ? VIEW_TYPE_USER_VIDEO_MESSAGE : VIEW_TYPE_FRIEND_VIDEO_MESSAGE;
      }
      case MESSAGE_TYPE_AUDIO: {
        return message.idSender.equals(StaticConfig.UID) ? VIEW_TYPE_USER_AUDIO_MESSAGE : VIEW_TYPE_FRIEND_AUDIO_MESSAGE;
      }

    }
    return -1;
  }

  @Override
  public int getItemCount() {
    return consersation.getListMessageData().size();
  }

  @Override
  public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    if (mediaPlayer != null && fromUser) {
      mediaPlayer.seekTo(progress);
    }
  }

  @Override
  public void onStartTrackingTouch(SeekBar seekBar) {

  }

  @Override
  public void onStopTrackingTouch(SeekBar seekBar) {

  }
}

class ItemTextMessageViewHolder extends RecyclerView.ViewHolder {
  final TextView txtContent;
  final TextView txtTime;
  final TextView userName;

  public ItemTextMessageViewHolder(View itemView) {
    super(itemView);
    txtContent = (TextView) itemView.findViewById(R.id.text);
    txtTime = (TextView) itemView.findViewById(R.id.textview_time);
    userName = (TextView) itemView.findViewById(R.id.user_name);
  }
}

class ItemImageMessageViewHolder extends RecyclerView.ViewHolder {
  final TextView txtTime;
  final TextView userName;
  final ProgressBar progressBar;
  final ImageView imageMessage;

  public ItemImageMessageViewHolder(View itemView) {
    super(itemView);
    txtTime = (TextView) itemView.findViewById(R.id.textview_time);
    userName = (TextView) itemView.findViewById(R.id.user_name);
    imageMessage = (ImageView) itemView.findViewById(R.id.image_view);
    progressBar = (ProgressBar) itemView.findViewById(R.id.progress);
  }
}

class ItemVideoMessageViewHolder extends RecyclerView.ViewHolder {
  final TextView txtTime;
  final TextView userName;
  final ProgressBar progressBar;
  final ImageView imageMessage;
  final View btnPlayVideo;

  public ItemVideoMessageViewHolder(View itemView) {
    super(itemView);
    txtTime = (TextView) itemView.findViewById(R.id.textview_time);
    userName = (TextView) itemView.findViewById(R.id.user_name);
    imageMessage = (ImageView) itemView.findViewById(R.id.image_view);
    btnPlayVideo = itemView.findViewById(R.id.btn_play_video);
    progressBar = (ProgressBar) itemView.findViewById(R.id.progress);
  }
}

class ItemAudioViewHolder extends RecyclerView.ViewHolder implements Runnable {
  final TextView txtTime;
  final TextView userName;
  final ImageButton btnPlayAudio;
  final SeekBar seekBar;
  final Handler handler;
  final MediaPlayer mediaPlayer;

  public ItemAudioViewHolder(View itemView, MediaPlayer mediaPlayer) {
    super(itemView);
    this.mediaPlayer = mediaPlayer;
    txtTime = (TextView) itemView.findViewById(R.id.textview_time);
    userName = (TextView) itemView.findViewById(R.id.user_name);
    btnPlayAudio = (ImageButton) itemView.findViewById(R.id.btn_play_audio);
    seekBar = (SeekBar) itemView.findViewById(R.id.seek_bar);
    handler = new Handler();
  }

  void updateSeekBar() {
    handler.postDelayed(this, 100);
  }

  void removeUpdateSeekBar() {
    handler.removeCallbacks(this);
  }

  @Override
  public void run() {
    if (mediaPlayer != null) {
      if (mediaPlayer.isPlaying()) {
        handler.postDelayed(this, 100);
        seekBar.setProgress(mediaPlayer.getCurrentPosition());
      } else {
        if (mediaPlayer != null) {
          mediaPlayer.stop();
          mediaPlayer.reset();
          btnPlayAudio.setImageResource(android.R.drawable.ic_media_play);
        }
      }
    }
  }
}