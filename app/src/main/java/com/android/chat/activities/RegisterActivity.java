package com.android.chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.chat.R;
import com.android.pal.chat.base.StaticConfig;

import java.util.regex.Matcher;


public class RegisterActivity extends AppCompatActivity {
    CardView cvAdd;
    private EditText editTextUsername, editTextPassword, editTextRepeatPassword;
    public static String STR_EXTRA_ACTION_REGISTER = "register";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        cvAdd = findViewById(R.id.cv_add);
        editTextUsername = findViewById(R.id.et_username);
        editTextPassword = findViewById(R.id.et_password);
        editTextRepeatPassword = findViewById(R.id.et_repeatpassword);
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
        Matcher matcher = Patterns.EMAIL_ADDRESS.matcher(emailStr);
        return password.length() > 0 && repeatPassword.equals(password) && matcher.find();
    }
}
