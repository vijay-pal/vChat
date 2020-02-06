//package com.pal.android.vChat;
//
//import android.app.ProgressDialog;
//import android.content.Intent;
//import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
//import android.util.Patterns;
//import android.view.View;
//import android.widget.EditText;
//import android.widget.Toast;
//
//import com.android.pal.chat.base.StaticConfig;
//import com.android.pal.chat.service.LoginAuth;
//import com.android.pal.chat.ui.activities.ChatRoomsActivity;
//import com.google.firebase.auth.FirebaseUser;
//
//import java.util.regex.Matcher;
//
//public class LoginActivityOld extends AppCompatActivity implements LoginAuth.TaskListener {
//  private static String TAG = "LoginActivityOld";
//  private EditText editTextUsername, editTextPassword;
//  private ProgressDialog waitingDialog;
//
//  private LoginAuth loginAuth;
//
//  @Override
//  protected void onStart() {
//    super.onStart();
//    loginAuth.addAuthStateListener();
//  }
//
//  @Override
//  protected void onCreate(Bundle savedInstanceState) {
//    super.onCreate(savedInstanceState);
//    setContentView(R.layout.activity_login_old);
//    editTextUsername = findViewById(R.id.et_username);
//    editTextPassword = findViewById(R.id.et_password);
//
//    loginAuth = new LoginAuth(this, this);
//  }
//
//  @Override
//  protected void onStop() {
//    super.onStop();
//    loginAuth.removeAuthStateListener();
//  }
//
//  public void clickRegisterLayout(View view) {
//    startActivityForResult(new Intent(this, RegisterActivity.class), StaticConfig.REQUEST_CODE_REGISTER);
//  }
//
//  @Override
//  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//    super.onActivityResult(requestCode, resultCode, data);
//    if (requestCode == StaticConfig.REQUEST_CODE_REGISTER && resultCode == RESULT_OK) {
//      loginAuth.getAuth().createUser(data.getStringExtra(StaticConfig.STR_EXTRA_USERNAME), data.getStringExtra(StaticConfig.STR_EXTRA_PASSWORD));
//    }
//  }
//
//  public void clickLogin(View view) {
//    String username = editTextUsername.getText().toString();
//    String password = editTextPassword.getText().toString();
//    if (validate(username, password)) {
//      loginAuth.getAuth().signIn(username, password);
//    } else {
//      Toast.makeText(this, "Invalid email or empty password", Toast.LENGTH_SHORT).show();
//    }
//  }
//
//  @Override
//  public void onBackPressed() {
//    super.onBackPressed();
//    setResult(RESULT_CANCELED, null);
//    finish();
//  }
//
//  private boolean validate(String emailStr, String password) {
//    Matcher matcher = Patterns.EMAIL_ADDRESS.matcher(emailStr);
//    return (password.length() > 0 || password.equals(";")) && matcher.find();
//  }
//
//  public void clickResetPassword(View view) {
//    String username = editTextUsername.getText().toString();
//    if (validate(username, ";")) {
//      loginAuth.getAuth().resetPassword(username);
//    } else {
//      Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show();
//    }
//  }
//
//  @Override
//  public void startProgress() {
//    if (waitingDialog == null) {
//      waitingDialog = new ProgressDialog(this);
//      waitingDialog.setCancelable(false);
//    }
//    waitingDialog.show();
//  }
//
//  @Override
//  public void dismissProgress() {
//    waitingDialog.dismiss();
//  }
//
//  @Override
//  public void result(FirebaseUser user) {
//    Intent intent = new Intent(this, ChatRoomsActivity.class);
//    startActivity(intent);
//    finish();
//  }
//
//  @Override
//  public void userCreated() {
//
//  }
//
//  @Override
//  public void loginSuccess() {
//    result(null);
//  }
//}
