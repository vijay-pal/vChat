package com.android.pal.chat.model;


import com.google.firebase.database.FirebaseDatabase;

public class Message {
  public String fileName;
  public String thumbnail;
  public String type = "";
  public String idSender;
  public String idReceiver;
  public String text;
  public String downloadUri;
  public String localUri;
  public long timestamp;
  public boolean isUploading;

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof Message) {
      return ((Message) obj).idSender == this.idSender && ((Message) obj).timestamp == this.timestamp;
    }
    return super.equals(obj);
  }

  public static class Builder {
    private Message message;

    public Builder(String idSender, String idReceiver) {
      message = new Message();
      message.idSender = idSender;
      message.idReceiver = idReceiver;
      message.timestamp = System.currentTimeMillis();
    }

    public Builder thumbnail(String thumbnail) {
      message.thumbnail = thumbnail;
      return this;
    }

    public Builder type(String type) {
      message.type = type;
      return this;
    }

    public Builder text(String text) {
      message.text = text;
      return this;
    }

    public Builder downloadUri(String downloadUri) {
      message.downloadUri = downloadUri;
      return this;
    }

    public Builder fileName(String fileName) {
      message.fileName = fileName;
      return this;
    }

    public Builder localUri(String localUri) {
      message.localUri = localUri;
      return this;
    }

    public Builder isUploading(boolean isUploading) {
      message.isUploading = isUploading;
      return this;
    }

    public Message build() {
      return message;
    }

    public void push(String roomId) {
      if (message != null) {
        FirebaseDatabase.getInstance().getReference().child("message/" + roomId).push().setValue(message);
      }
    }
  }
}