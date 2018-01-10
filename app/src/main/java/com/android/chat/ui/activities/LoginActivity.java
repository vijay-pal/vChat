package com.android.chat.ui.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.android.chat.R;
import com.android.chat.data.SharedPreferenceHelper;
import com.android.chat.data.StaticConfig;
import com.android.chat.model.User;

import java.util.HashMap;
import java.util.regex.Matcher;


public class LoginActivity extends AppCompatActivity {
  private static String TAG = "LoginActivity";
  private EditText editTextUsername, editTextPassword;
  private ProgressDialog waitingDialog;

  private AuthUtils authUtils;
  private FirebaseAuth mAuth;
  private FirebaseAuth.AuthStateListener mAuthListener;
  private FirebaseUser user;
  private boolean firstTimeAccess;

  @Override
  protected void onStart() {
    super.onStart();
    mAuth.addAuthStateListener(mAuthListener);
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);
    editTextUsername = (EditText) findViewById(R.id.et_username);
    editTextPassword = (EditText) findViewById(R.id.et_password);
    firstTimeAccess = true;
    initFirebase();
  }


  private void initFirebase() {
    //Khoi tao thanh phan de dang nhap, dang ky
    mAuth = FirebaseAuth.getInstance();
    authUtils = new AuthUtils();
    mAuthListener = new FirebaseAuth.AuthStateListener() {
      @Override
      public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        user = firebaseAuth.getCurrentUser();
        if (user != null) {
          // User is signed in
          StaticConfig.UID = user.getUid();
          if (firstTimeAccess) {
            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
            LoginActivity.this.finish();
          }
        } else {
          Log.d(TAG, "onAuthStateChanged:signed_out");
        }
        firstTimeAccess = false;
      }
    };

    //Khoi tao dialog waiting khi dang nhap
    waitingDialog = new ProgressDialog(this);
    waitingDialog.setCancelable(false);
  }

  @Override
  protected void onStop() {
    super.onStop();
    if (mAuthListener != null) {
      mAuth.removeAuthStateListener(mAuthListener);
    }
  }

  public void clickRegisterLayout(View view) {
    startActivityForResult(new Intent(this, RegisterActivity.class), StaticConfig.REQUEST_CODE_REGISTER);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == StaticConfig.REQUEST_CODE_REGISTER && resultCode == RESULT_OK) {
      authUtils.createUser(data.getStringExtra(StaticConfig.STR_EXTRA_USERNAME), data.getStringExtra(StaticConfig.STR_EXTRA_PASSWORD));
    }
  }

  public void clickLogin(View view) {
    String username = editTextUsername.getText().toString();
    String password = editTextPassword.getText().toString();
    if (validate(username, password)) {
      authUtils.signIn(username, password);
    } else {
      Toast.makeText(this, "Invalid email or empty password", Toast.LENGTH_SHORT).show();
    }
  }

  @Override
  public void onBackPressed() {
    super.onBackPressed();
    setResult(RESULT_CANCELED, null);
    finish();
  }

  private boolean validate(String emailStr, String password) {
    Matcher matcher = Patterns.EMAIL_ADDRESS.matcher(emailStr);
    return (password.length() > 0 || password.equals(";")) && matcher.find();
  }

  public void clickResetPassword(View view) {
    String username = editTextUsername.getText().toString();
    if (validate(username, ";")) {
      authUtils.resetPassword(username);
    } else {
      Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
    }
  }

  class AuthUtils {
    /**
     * Action register
     *
     * @param email
     * @param password
     */
    void createUser(String email, String password) {
      waitingDialog.setIcon(R.drawable.ic_add_friend);
      waitingDialog.setTitle("Registering....");
      waitingDialog.show();
      mAuth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {
            Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());
            waitingDialog.dismiss();
            // If sign in fails, display a message to the user. If sign in succeeds
            // the auth state listener will be notified and logic to handle the
            // signed in user can be handled in the listener.
            if (!task.isSuccessful()) {
              Toast.makeText(LoginActivity.this, "Email exist or weak password!", Toast.LENGTH_LONG).show();
            } else {
              initNewUserInfo();
              Toast.makeText(LoginActivity.this, "Register and Login success", Toast.LENGTH_SHORT).show();
              startActivity(new Intent(LoginActivity.this, HomeActivity.class));
              LoginActivity.this.finish();
            }
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            waitingDialog.dismiss();
          }
        })
      ;
    }


    /**
     * Action Login
     *
     * @param email
     * @param password
     */
    void signIn(String email, String password) {
      waitingDialog.setIcon(R.drawable.ic_person_low);
      waitingDialog.setTitle("Login....");
      waitingDialog.show();
      mAuth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {
            Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
            // If sign in fails, display a message to the user. If sign in succeeds
            // the auth state listener will be notified and logic to handle the
            // signed in user can be handled in the listener.
            waitingDialog.dismiss();
            if (!task.isSuccessful()) {
              Toast.makeText(LoginActivity.this, "Email not exist or wrong password!", Toast.LENGTH_LONG).show();
            } else {
              saveUserInfo();
              startActivity(new Intent(LoginActivity.this, HomeActivity.class));
              LoginActivity.this.finish();
            }
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            waitingDialog.dismiss();
          }
        });
    }

    /**
     * Action reset password
     *
     * @param email
     */
    void resetPassword(final String email) {
      mAuth.sendPasswordResetEmail(email)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {
            Toast.makeText(LoginActivity.this, "Sent email to " + email, Toast.LENGTH_LONG).show();
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            Toast.makeText(LoginActivity.this, "We can't sent recovery email on  " + email, Toast.LENGTH_LONG).show();
          }
        });
    }

    /**
     * Luu thong tin user info cho nguoi dung dang nhap
     */
    void saveUserInfo() {
      FirebaseDatabase.getInstance().getReference().child("user/" + StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          waitingDialog.dismiss();
          HashMap hashUser = (HashMap) dataSnapshot.getValue();
          User userInfo = new User();
          userInfo.name = (String) hashUser.get("name");
          userInfo.email = (String) hashUser.get("email");
          userInfo.avatar = (String) hashUser.get("avatar");
          SharedPreferenceHelper.getInstance(LoginActivity.this).saveUserInfo(userInfo);
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
      });
    }

    void initNewUserInfo() {
      User newUser = new User();
      newUser.email = user.getEmail();
      newUser.name = user.getEmail().substring(0, user.getEmail().indexOf("@"));
      newUser.avatar = StaticConfig.STR_DEFAULT_BASE64;
      FirebaseDatabase.getInstance().getReference().child("user/" + user.getUid()).setValue(newUser);
    }
  }
}
