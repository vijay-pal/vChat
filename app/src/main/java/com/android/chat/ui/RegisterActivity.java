package com.android.chat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.chat.R;
import com.android.chat.data.StaticConfig;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class RegisterActivity extends AppCompatActivity {
  CardView cvAdd;
  private final Pattern VALID_EMAIL_ADDRESS_REGEX =
    Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
  private EditText editTextUsername, editTextPassword, editTextRepeatPassword;
  public static String STR_EXTRA_ACTION_REGISTER = "register";

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_register);
    cvAdd = (CardView) findViewById(R.id.cv_add);
    editTextUsername = (EditText) findViewById(R.id.et_username);
    editTextPassword = (EditText) findViewById(R.id.et_password);
    editTextRepeatPassword = (EditText) findViewById(R.id.et_repeatpassword);
  }

  public void clickRegister(View view) {
    String username = editTextUsername.getText().toString();
    String password = editTextPassword.getText().toString();
    String repeatPassword = editTextRepeatPassword.getText().toString();
    if (validate(username, password, repeatPassword)) {
      Intent data = new Intent();
      data.putExtra(StaticConfig.STR_EXTRA_USERNAME, username);
      data.putExtra(StaticConfig.STR_EXTRA_PASSWORD, password);
      data.putExtra(StaticConfig.STR_EXTRA_ACTION, STR_EXTRA_ACTION_REGISTER);
      setResult(RESULT_OK, data);
      finish();
    } else {
      Toast.makeText(this, "Invalid email or not match password", Toast.LENGTH_SHORT).show();
    }
  }

  public void clickBackLogin(View view) {
    finish();
  }

  /**
   * Validate email, pass == re_pass
   *
   * @param emailStr
   * @param password
   * @return
   */
  private boolean validate(String emailStr, String password, String repeatPassword) {
    Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
    return password.length() > 0 && repeatPassword.equals(password) && matcher.find();
  }
}
