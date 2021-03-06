# vChat Android native Chat Appliction

## Description
  This repository contains all the code related **vChat**. The chat is based on the google firebase realtime data. This chat      application provide multiple features- 
  - Text Message
  - Image Message
  - Video Message
  - Audio Message
  - Share your current location on google map image
  
You can also integrate this project module in your existing project to add chat feature in your application.

## How to integrate in your existing project
  - Download Project from github 
  - include two module in in your project 
      ```
      implementation project(':base-module')
      implementation project(':vchat-component')
      ```
  - Update ```google-services.json``` by your google account
    > Create a project in google firebase account [Google Firebase](https://console.firebase.google.com/) and the steps and download ```google-services.json``` 
  - Implement below interface in your login class
      ```
      public interface TaskListener {
        void startProgress();

        void dismissProgress();

        void result(FirebaseUser user);

        void userCreated();

        void loginSuccess();
      }
      ```
  - Create and intanace of ```loginAuth = new LoginAuth(this, this);``` your login activity 
  - Register and remove AuthListener by calling its method from 
      ```
      protected void onStart() {
        super.onStart();
        loginAuth.addAuthStateListener();
      }
      
      protected void onStop() {
        super.onStop();
        loginAuth.removeAuthStateListener();
      } 
      ```
  - And Start chat room activity by the 
       ```
       Intent intent = new Intent(this, ChatRoomsActivity.class);
       startActivity(intent);
       ```
       ```ChatRoomsActivity.java``` is an activity of ```vchat-component``` module
    
       ```LoginAuth``` class provides two method of login and sigup, manual and other is Google Account.
    
  - Login on firebase console and enable Authentication Sign-In method.
    
## How to customize Chat Room
  There is a static method class ```VChatSettings.java``` in base-module, you can use its methods and remove items like search, create group etc.
  
  ```VChatSettings.enableGlobalSearch(boolean b);
         
  VChatSettings.enableCreateGroup(boolean b) ;
       
  VChatSettings.enableShowProfile(boolean b);
       
  VChatSettings.enableSearch(boolean b);
       
  VChatSettings.enableAboutApp(boolean b) ;
       
  VChatSettings.enableOneToOneChatRoom(boolean b);
  
  ```
  
  _By-default all features are enabled. you can disable above by passing_ ```false``` _value_.
