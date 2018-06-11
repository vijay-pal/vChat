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
       Intent intent = new Intent(this, HomeActivity.class);
       startActivity(intent);
       ```
       ```HomeActivity.java``` is an activity of ```vchat-component``` module 
    
       ```LoginAuth``` class provides two method of login and sigup, manual and other is Google Account.
    
  - Login on firebase console and enable Authentication Sig-In method.
    
