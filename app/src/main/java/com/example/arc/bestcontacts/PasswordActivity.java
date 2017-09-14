package com.example.arc.bestcontacts;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.arc.bestcontacts.models.TinyDB;
import com.example.arc.bestcontacts.models.User;
import com.example.arc.bestcontacts.network.API;
import com.example.arc.bestcontacts.utils.Global;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PasswordActivity extends AppCompatActivity {

    private static final String TAG = "PasswordActivityTag";
    EditText oldPassword;
    EditText newPassword;
    EditText newRepeatPassword;
    Button btnChange;
    Button btnCancel;
    TinyDB tinydb;
    List<User> userList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password);

        oldPassword = (EditText) findViewById(R.id.oldPasswordInput);
        newPassword = (EditText) findViewById(R.id.newPasswordInput);
        newRepeatPassword = (EditText) findViewById(R.id.newPasswordRepeatInput);
        btnChange = (Button) findViewById(R.id.btnChangePassword);
        btnCancel = (Button) findViewById(R.id.btnCancleChangePassword);
        final TinyDB tinydb = new TinyDB(PasswordActivity.this);
        final ProgressDialog progressBar;
        progressBar = new ProgressDialog(PasswordActivity.this);
        progressBar.setMessage("Changing password and updating in progress");

        if(Global.getLoggedUser().getPassword().equals(Global.md5("123123123")))
        {
            btnCancel.setVisibility(View.GONE);
        }


        btnChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nPass = newPassword.getText().toString();
                String nrPass = newRepeatPassword.getText().toString();
                String oldPass = oldPassword.getText().toString();

                if(nPass.length() > 5) {
                    if (!oldPass.equals(nPass)) {
                        if (nPass.equals(nrPass)) {
                            progressBar.show();
                            final JsonObject jo = new JsonObject();
                            String email = Global.getLoggedUser().getEmail();
                            oldPass = Global.md5(oldPassword.getText().toString());
                            nPass = Global.md5(nPass);

                            jo.addProperty("email", email);
                            jo.addProperty("password", oldPass);
                            jo.addProperty("newpassword", nPass);
                            jo.addProperty(Global.getSecurityNameKey(), Global.getSecurityKey());
                            final String finalNPass = nPass;
                            API.getServices.getAppApiServices().userChangePass(jo).enqueue(new Callback() {
                                @Override
                                public void onResponse(Call call, Response response) {
                                    if (response.code() == 200) {
                                        API.getServices.getAppApiServices().getUserList(jo).enqueue(new Callback<List<User>>() {

                                            @Override
                                            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                                             //   Log.d(TAG, "onResponsePassword: " + new Gson().toJson(response));

                                                if (response.body() != null) {

                                                 //   Log.d(TAG, "onResponseBody: " + new Gson().toJson(response.body()));
                                                    userList = new ArrayList<User>();
                                                    userList = response.body();

                                                    Collections.sort(userList, new Comparator<User>() {
                                                        public int compare(User obj1, User obj2) {
                                                            // ## Ascending order
                                                            return obj1.getFirstname().compareToIgnoreCase(obj2.getFirstname()); // To compare string values
                                                            // return Integer.valueOf(obj1.empId).compareTo(obj2.empId); // To compare integer values

                                                            // ## Descending order
                                                            // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                                                            // return Integer.valueOf(obj2.empId).compareTo(obj1.empId); // To compare integer values
                                                        }
                                                    });

                                                    tinydb.remove("AllUsers");
                                                    tinydb.putListObject("AllUsers", (ArrayList<User>) userList);
                                                    Global.setLoggedUserPassword(finalNPass);

                                                }

                                            }

                                            @Override
                                            public void onFailure(Call<List<User>> call, Throwable t) {
                                                Toast.makeText(PasswordActivity.this, "Can't get new list Users", Toast.LENGTH_SHORT).show();
                                              //  Log.d(TAG, "onFailureChangePassGetUser: " + new Gson().toJson(t));
                                            }
                                        });
                                        Toast.makeText(PasswordActivity.this, "Password changed and all data is updated", Toast.LENGTH_LONG).show();

                                        startActivity(new Intent(PasswordActivity.this, MainActivity.class));
                                    } else if (response.code() == 401) {
                                        Toast.makeText(PasswordActivity.this, "Contact administrator : 401 error", Toast.LENGTH_SHORT).show();
                                    } else if (response.code() == 402) {
                                        Toast.makeText(PasswordActivity.this, "Old password is incorrect : 402 error", Toast.LENGTH_SHORT).show();
                                    } else if (response.code() == 404) {
                                        Toast.makeText(PasswordActivity.this, "Contact administrator : 404 error", Toast.LENGTH_SHORT).show();
                                    }
                                    progressBar.dismiss();

                                }

                                @Override
                                public void onFailure(Call call, Throwable t) {
                                    progressBar.dismiss();
                                    Toast.makeText(PasswordActivity.this, "Can't change Pass", Toast.LENGTH_SHORT).show();
                                  //  Log.d(TAG, "onFailureChangePass: " + new Gson().toJson(t));
                                }
                            });
                        } else
                            Toast.makeText(PasswordActivity.this, "New password don't match", Toast.LENGTH_SHORT).show();
                    } else
                        Toast.makeText(PasswordActivity.this, "New password can't be same as old one", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(PasswordActivity.this, "Lenght of new password is lower than 6", Toast.LENGTH_LONG);
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PasswordActivity.this, MainActivity.class);

                startActivity(intent);
                finish();
            }
        });
    }


}
