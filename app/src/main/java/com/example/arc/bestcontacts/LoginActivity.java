package com.example.arc.bestcontacts;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.arc.bestcontacts.models.TinyDB;
import com.example.arc.bestcontacts.models.User;
import com.example.arc.bestcontacts.network.API;
import com.example.arc.bestcontacts.utils.Global;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    EditText emailInput;
    EditText passwordInput;
    CheckBox rememberMeCheck;
    Button btnLogin;
    boolean sharedPref;
    ProgressDialog progressBar;
    String eml;
    String pas;
    TinyDB tinydb;

    private static final String TAG = "LoginActivity";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        tinydb = new TinyDB(LoginActivity.this);
//        mObject = getSavedObjectFromPreference(LoginActivity.this, "FileNameGlobal", "GlobalPrefKey", Global.class);
        ArrayList<User> db  =  tinydb.getListObject("AllUsers",User.class);
        if(db.isEmpty()) {
           // Log.d(TAG, "sharedPref: NULL je Object");
            sharedPref = false;
        }
        else {
         //   Log.d(TAG, "sharedPref: Object Found");
            sharedPref = true;
            Global.setUserList(db);
        }



        emailInput = (EditText) findViewById(R.id.emailInput);
        passwordInput = (EditText) findViewById(R.id.passwordInput);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        rememberMeCheck = (CheckBox) findViewById(R.id.checkboxRememberMe);

        progressBar = new ProgressDialog(LoginActivity.this);
        progressBar.setMessage("Logging in");

        String usrInput = tinydb.getString("UserLoginEmail");
        String pwdInput = tinydb.getString("UserLoginPassword");

        if(!usrInput.isEmpty() && !pwdInput.isEmpty())
        {
            emailInput.setText(usrInput);
            passwordInput.setText(pwdInput);
            rememberMeCheck.setChecked(true);
        }

//        passwordInput.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//
//                passwordInput.setText("");
//            }
//        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isInputEmpty())
                {
                    boolean temp = false;
                    eml = emailInput.getText().toString();
                    pas = Global.md5(passwordInput.getText().toString());

                    if (!sharedPref) {
                        temp = false;
                        apiLogin(eml, pas);
                    }
                    else {
                        for (User user : Global.getUserList())
                        {
                            temp = false;
                            if(user.getEmail().equals(eml) && user.getPassword().equals(pas))
                            {
                                Global.setLoggedUser(user);
                                Toast.makeText(LoginActivity.this, "You are logged in", Toast.LENGTH_SHORT).show();
                                startHomeScreen(false);
                                temp = true;
                                break;
                            }

                        }
                    }
                    if(!temp)
                        Toast.makeText(LoginActivity.this, "Wrong credentials",Toast.LENGTH_LONG).show();

                }
            }
        });
        btnLogin.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(!isInputEmpty()) {
                    String eml = emailInput.getText().toString();
                    String pas = Global.md5(passwordInput.getText().toString());
                //    Log.d(TAG,"LONGBUTTONLOGIN");
                    apiLogin(eml, pas);
                }
                return false;
            }
        });
    }



    private void apiLogin(String eml, String pas) {
        progressBar.show();

        JsonObject jo = new JsonObject();
        jo.addProperty("email", eml);
        jo.addProperty("password", pas);
        jo.addProperty(Global.getSecurityNameKey(),Global.getSecurityKey());

        API.getServices.getAppApiServices().userLogin(jo).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response != null) {
                    if(response.code() == 200) {
                        User user = null;
                        user = response.body();
                        progressBar.dismiss();
                        if (user.getEmail() != null) {
                            Global.setLoggedUser(user);
                            Toast.makeText(LoginActivity.this, "You are logged in", Toast.LENGTH_SHORT).show();
                            startHomeScreen(true);
                        } else
                            Toast.makeText(LoginActivity.this, "Server error call IT people", Toast.LENGTH_LONG).show();
                    }
                    else if (response.code() == 401) {
                        progressBar.dismiss();
                        Toast.makeText(LoginActivity.this, "Email or password incorrect : 401 error", Toast.LENGTH_SHORT).show();
                    }
                    else if (response.code() == 402) {
                        progressBar.dismiss();
                        Toast.makeText(LoginActivity.this, "Email or password incorrect : 402 error", Toast.LENGTH_SHORT).show();
                    }
                    else if (response.code() == 404) {
                        progressBar.dismiss();
                        Toast.makeText(LoginActivity.this, "Contact administrator : 404 error", Toast.LENGTH_SHORT).show();
                    }
                }
                progressBar.dismiss();

            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                progressBar.dismiss();
                Toast.makeText(LoginActivity.this, "Network error or wrong credentials", Toast.LENGTH_SHORT).show();
               // Log.d(TAG, "onFailureLoadList: " + new Gson().toJson(t));
            }
        });
    }

    private boolean isInputEmpty() {
        return emailInput.getText().toString().isEmpty() || passwordInput.getText().toString().isEmpty();
    }

    private void startHomeScreen(boolean refresh)
    {
        if(Global.getLoggedUser().getPassword().equals(Global.md5("123123123")))
        {
            startPasswordScreen();
        }
        else {

            if (rememberMeCheck.isChecked()) {
                rememberMe(tinydb, eml, pas);
            }

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("refreshApp", refresh);
            startActivity(intent);
            finish();
        }
//        getActivity().getFragmentManager().beginTransaction().replace(R.id.fl_Main, new HomePageFragment()).addToBackStack(new LoginFragment().getClass().getName()).commit();

    }

    private void startPasswordScreen() {
        Intent intent = new Intent(this, PasswordActivity.class);
        startActivity(intent);
        finish();
    }
    private void rememberMe(final TinyDB tinyDB, String email, String password)
    {
        tinyDB.putString("UserLoginEmail",emailInput.getText().toString());
        tinyDB.putString("UserLoginPassword",passwordInput.getText().toString());
    }

}
