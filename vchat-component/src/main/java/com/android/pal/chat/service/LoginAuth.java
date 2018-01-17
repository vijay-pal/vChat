package com.android.pal.chat.service;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.android.pal.chat.base.StaticConfig;
import com.android.pal.chat.base.data.SharedPreferenceHelper;
import com.android.pal.chat.base.model.User;
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

import java.util.HashMap;

/**
 * Created by admirar on 1/14/18.
 */

public class LoginAuth {
    private Listener mListener;
    private AuthUtils authUtils;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private boolean firstTimeAccess;
    private Context mContext;

    public LoginAuth(Context mContext, Listener mListener) {
        this.mContext = mContext;
        this.mListener = mListener;
        firstTimeAccess = true;
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
                }
                if (mListener != null) {
                    mListener.result(user);
                }
            }
        };
    }

    public class AuthUtils {
        public void createUser(String email, String password) {
            if (mListener != null) {
                mListener.startProgress();
            }
            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (mListener != null) {
                                mListener.dismissProgress();
                            }
                            // signed in user can be handled in the listener.
                            if (!task.isSuccessful()) {
                                Toast.makeText(mContext, "Email exist or weak password!", Toast.LENGTH_LONG).show();
                            } else {
                                initNewUserInfo();
                                mListener.userCreated();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (mListener != null) {
                                mListener.dismissProgress();
                            }
                        }
                    })
            ;
        }

        public void signIn(String email, String password) {
            if (mListener != null) {
                mListener.startProgress();
            }
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (mListener != null) {
                                mListener.dismissProgress();
                            }
                            if (!task.isSuccessful()) {
                                Toast.makeText(mContext, "Email not exist or wrong password!", Toast.LENGTH_LONG).show();
                            } else {
                                saveUserInfo();
                                if (mListener != null) {
                                    mListener.loginSuccess();
                                }
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if (mListener != null) {
                                mListener.dismissProgress();
                            }
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
                    if (mListener != null) {
                        mListener.dismissProgress();
                    }
                    HashMap hashUser = (HashMap) dataSnapshot.getValue();
                    User userInfo = new User();
                    userInfo.name = (String) hashUser.get("name");
                    userInfo.email = (String) hashUser.get("email");
                    userInfo.avatar = (String) hashUser.get("avatar");
                    SharedPreferenceHelper.getInstance(mContext).saveUserInfo(userInfo);
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

    public interface Listener {
        void startProgress();

        void dismissProgress();

        void result(FirebaseUser user);

        void userCreated();

        void loginSuccess();
    }
}
