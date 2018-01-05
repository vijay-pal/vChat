package com.android.chat.model;


import com.google.firebase.database.DatabaseReference;

public class Message {
  public DatabaseReference reference;
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
}