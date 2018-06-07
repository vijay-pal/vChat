package com.pal.android.vChat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.android.pal.chat.base.BaseActivity;
import com.android.pal.chat.service.LoginAuth;
import com.android.pal.chat.ui.activities.HomeActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;

/**
 * Activity to demonstrate basic retrieval of the Google user's ID, email address, and basic
 * profile.
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener, LoginAuth.TaskListener {

  private static final String TAG = "SignInActivity";
  private static final int RC_SIGN_IN = 9001;

  private GoogleSignInClient mGoogleSignInClient;
  private LoginAuth loginAuth;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    loginAuth = new LoginAuth(this, this);
    // Views

    // [START configure_signin]
    // Configure sign-in to request the user's ID, email address, and basic
    // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
    GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
      .requestIdToken(getString(R.string.default_web_client_id))
      .requestEmail()
      .build();
    // [END configure_signin]

    // [START build_client]
    mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    // [END build_client]

    // [START customize_button]
    // Set the dimensions of the sign-in button.
    SignInButton signInButton = findViewById(R.id.sign_in_button);
    signInButton.setSize(SignInButton.SIZE_WIDE);
    signInButton.setOnClickListener(this);
    // [END customize_button]
  }

  @Override
  protected void onStart() {
    super.onStart();
    loginAuth.addAuthStateListener();
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  // [START onActivityResult]
  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == RC_SIGN_IN) {
      Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
      try {
        // Google Sign In was successful, authenticate with Firebase
        GoogleSignInAccount account = task.getResult(ApiException.class);
        loginAuth.getAuth().firebaseAuthWithGoogle(account);
      } catch (ApiException e) {
        // Google Sign In failed, update UI appropriately
        Log.w(TAG, "Google sign in failed", e);
        // ...
      }
    }

  }
// [END onActivityResult]

  // [START signIn]
  private void signIn() {
    Intent signInIntent = mGoogleSignInClient.getSignInIntent();
    startActivityForResult(signInIntent, RC_SIGN_IN);
  }
// [END signIn]

  @Override
  protected void onStop() {
    super.onStop();
    loginAuth.removeAuthStateListener();
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()) {
      case R.id.sign_in_button:
        signIn();
        break;
    }
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Log.i("DATA", "onDestroy");
  }

  @Override
  public void startProgress() {

  }

  @Override
  public void dismissProgress() {

  }

  @Override
  public void result(FirebaseUser user) {
    Intent intent = new Intent(this, HomeActivity.class);
    startActivity(intent);
    finish();
  }

  @Override
  public void userCreated() {

  }

  @Override
  public void loginSuccess() {
    result(null);
  }
}