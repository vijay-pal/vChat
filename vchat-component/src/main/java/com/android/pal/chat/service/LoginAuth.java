package com.android.pal.chat.service;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.android.pal.chat.base.StaticConfig;
import com.android.pal.chat.base.data.SharedPreferenceHelper;
import com.android.pal.chat.base.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.ProviderQueryResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

/**
 * Created by admirar on 1/14/18.
 */

public class LoginAuth {
  private final static String TAG = LoginAuth.class.getSimpleName();
  private TaskListener mTaskListener;
  private UserSessionListener mSessionListener;
  private AuthUtils authUtils;
  private FirebaseAuth mAuth;
  private FirebaseUser user;
  private FirebaseAuth.AuthStateListener mAuthListener;
  private Context mContext;

  public LoginAuth(Context mContext, TaskListener mTaskListener) {
    this.mContext = mContext;
    this.mTaskListener = mTaskListener;
    initFirebase();
  }

  public LoginAuth(Context mContext, UserSessionListener mTaskListener) {
    this.mContext = mContext;
    this.mSessionListener = mTaskListener;
    initFirebase();
  }

  public AuthUtils getAuth() {
    return authUtils;
  }

  public void addAuthStateListener() {
    mAuth.addAuthStateListener(mAuthListener);
  }

  public void removeAuthStateListener() {
    if (mAuthListener != null) {
      mAuth.removeAuthStateListener(mAuthListener);
    }
  }

  private void initFirebase() {
    mAuth = FirebaseAuth.getInstance();
    authUtils = new AuthUtils();
    mAuthListener = new FirebaseAuth.AuthStateListener() {
      @Override
      public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        user = firebaseAuth.getCurrentUser();
        if (user != null) {
          // User is signed in
          StaticConfig.UID = user.getUid();
          if (mTaskListener != null) {
            mTaskListener.result(user);
          }
          if (mSessionListener != null) {
            mSessionListener.result(user);
          }
        } else {
          if (mSessionListener != null) {
            mSessionListener.sessionExpired();
          }
        }

      }
    };
  }

  public class AuthUtils {
    public void createUser(String email, String password) {
      if (mTaskListener != null) {
        mTaskListener.startProgress();
      }
      mAuth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {
            if (mTaskListener != null) {
              mTaskListener.dismissProgress();
            }
            // signed in user can be handled in the listener.
            if (!task.isSuccessful()) {
              Toast.makeText(mContext, "Email exist or weak password!", Toast.LENGTH_LONG).show();
            } else {
              initNewUserInfo();
              mTaskListener.userCreated();
            }
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            if (mTaskListener != null) {
              mTaskListener.dismissProgress();
            }
          }
        })
      ;
    }

    public void isUseExist(final String email, final String password) {
      mAuth.fetchProvidersForEmail(email).addOnCompleteListener(new OnCompleteListener<ProviderQueryResult>() {
        @Override
        public void onComplete(@NonNull Task<ProviderQueryResult> task) {
          if (task.getResult().getProviders().isEmpty()) {
            Log.i(TAG, "isEmpty");
            if (mSessionListener != null) {
              mSessionListener.userNotExits();
            }
          } else {
            signIn(email, password);
          }
        }
      });
    }

    public void signIn(String email, String password) {
      if (mTaskListener != null) {
        mTaskListener.startProgress();
      }
      mAuth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {
            if (mTaskListener != null) {
              mTaskListener.dismissProgress();
            }
            if (!task.isSuccessful()) {
              Toast.makeText(mContext, "Email not exist or wrong password!", Toast.LENGTH_LONG).show();
            } else {
              saveUserInfo();
              if (mTaskListener != null) {
                mTaskListener.loginSuccess();
              }
            }
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            if (mTaskListener != null) {
              mTaskListener.dismissProgress();
            }
          }
        });
    }

    public void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
      Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

      AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
      mAuth.signInWithCredential(credential)
        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task) {
            if (task.isSuccessful()) {
              // Sign in success, update UI with the signed-in user's information
              Log.d(TAG, "signInWithCredential:success");
              user = mAuth.getCurrentUser();
              initNewUserInfo();
              if (mTaskListener != null) {
                mTaskListener.loginSuccess();
              }
            } else {
              // If sign in fails, display a message to the user.
              Log.w(TAG, "signInWithCredential:failure", task.getException());
              Toast.makeText(mContext, "Authentication Failed.", Toast.LENGTH_LONG).show();
            }

            // ...
          }
        });
    }

    public void resetPassword(final String email) {
      mAuth.sendPasswordResetEmail(email)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
          @Override
          public void onComplete(@NonNull Task<Void> task) {
            Toast.makeText(mContext, "Sent email to " + email, Toast.LENGTH_LONG).show();
          }
        })
        .addOnFailureListener(new OnFailureListener() {
          @Override
          public void onFailure(@NonNull Exception e) {
            Toast.makeText(mContext, "We can't sent recovery email on  " + email, Toast.LENGTH_LONG).show();
          }
        });
    }

    public void saveUserInfo() {
      FirebaseDatabase.getInstance().getReference().child("user/" + StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          if (mTaskListener != null) {
            mTaskListener.dismissProgress();
          }
          try {
            HashMap hashUser = (HashMap) dataSnapshot.getValue();
            User userInfo = new User();
            userInfo.name = (String) hashUser.get("name");
            userInfo.email = (String) hashUser.get("email");
            userInfo.avatar = (String) hashUser.get("avatar");
            SharedPreferenceHelper.getInstance(mContext).saveUserInfo(userInfo);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
      });
    }

    void initNewUserInfo() {
      FirebaseDatabase.getInstance().getReference().child("user/" + user.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
        @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
          if (!dataSnapshot.exists()) {
            User newUser = new User();
            newUser.email = user.getEmail();
            newUser.name = user.getEmail().substring(0, user.getEmail().indexOf("@"));
            newUser.avatar = StaticConfig.STR_DEFAULT_BASE64;
            FirebaseDatabase.getInstance().getReference().child("user/" + user.getUid()).setValue(newUser);
          }
        }

        @Override
        public void onCancelled(DatabaseError databaseError) {

        }
      });
    }

    public void loggedOut() {
      mAuth.signOut();
    }
  }

  public interface TaskListener {
    void startProgress();

    void dismissProgress();

    void result(FirebaseUser user);

    void userCreated();

    void loginSuccess();
  }

  public interface UserSessionListener {

    void result(FirebaseUser user);

    void sessionExpired();

    void userNotExits();
  }
}
