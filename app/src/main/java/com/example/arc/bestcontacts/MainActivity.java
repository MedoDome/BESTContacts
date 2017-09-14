package com.example.arc.bestcontacts;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.example.arc.bestcontacts.adapter.AdapterListContacts;
import com.example.arc.bestcontacts.models.TinyDB;
import com.example.arc.bestcontacts.models.User;
import com.example.arc.bestcontacts.network.API;
import com.example.arc.bestcontacts.utils.Global;
import com.example.arc.bestcontacts.utils.OnSwipeTouchListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    ListView userListView;
    AdapterListContacts adapterListContacts;
    final OnSwipeTouchListener swipeDetector = new OnSwipeTouchListener();
    List<User> userList;
    EditText searchInput;
    ProgressDialog progressBar;
    TinyDB tinydb;
    boolean refreshApp;

    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;
    private static final int PERMISSIONS_REQUEST_CALL_PHONE = 200;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted, try now", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == PERMISSIONS_REQUEST_CALL_PHONE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted, try now", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(this, "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
        loadActivity();
    }

    @RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menus, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.activitySearch)
                .getActionView();
        if (null != searchView) {
            searchView.setSearchableInfo(searchManager
                    .getSearchableInfo(getComponentName()));
            searchView.setIconifiedByDefault(true);
        }

        SearchView.OnQueryTextListener queryTextListener = new SearchView.OnQueryTextListener() {
            public boolean onQueryTextChange(String newText) {
                adapterListContacts.getFilter().filter(newText);
                // this is your adapter that will be filtered
                return true;
            }

            public boolean onQueryTextSubmit(String query) {
                //Here u can get the value "query" which is entered in the search box.

                return true;
            }
        };
        searchView.setOnQueryTextListener(queryTextListener);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

       if (id == R.id.activityPassword) {
           Intent intent = new Intent(this, PasswordActivity.class);

           startActivity(intent);
           return true;
        } else if (id == R.id.activitySync) {
            progressBar = new ProgressDialog(MainActivity.this);
            progressBar.setMessage("Sync in progress");
            progressBar.show();
            apiGetUserList(tinydb);

            return true;
        } else if (id == R.id.activityLogout) {
            Global.deleteLoggedUser();
            Intent intent = new Intent(this,LoginActivity.class);
            startActivity(intent);
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTinyDb();
        if(getIntent().getExtras() == null)
            this.refreshApp = false;
        else
            this.refreshApp = getIntent().getExtras().getBoolean("refreshApp");



        if (checkAndRequestPermissions() )
        {
            loadActivity();
        }


    }

    private void setTinyDb() {
        this.tinydb  = new TinyDB(MainActivity.this);
    }

    private void loadActivity() {

        setTitle("BEST Mostar Contacts");

        userListView = (ListView) findViewById(R.id.lv_contacts);
        progressBar = new ProgressDialog(MainActivity.this);

        if (Global.getUserList() == null || refreshApp) {
           // Log.d(TAG, "sharedPref: Loading list from API");
            progressBar.setMessage("Loading list from API");
            progressBar.show();
            apiGetUserList(tinydb);
        } else {
           // Log.d(TAG, "sharedPref: Loading list locally");
            progressBar.setMessage("Loading list locally");
            progressBar.show();
            userList = new ArrayList<User>();
            userList = Global.getUserList();
            adapterListContacts = new AdapterListContacts();
            adapterListContacts = new AdapterListContacts(MainActivity.this, userList);
            adapterListContacts.notifyDataSetChanged();
            userListView.setAdapter(adapterListContacts);
            progressBar.dismiss();

        }
    }

    public void apiGetUserList(final TinyDB tinydb) {

        JsonObject jo = new JsonObject();
        jo.addProperty(Global.getSecurityNameKey(),Global.getSecurityKey());

        API.getServices.getAppApiServices().getUserList(jo).enqueue(new Callback<List<User>>() {

            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
               // Log.d(TAG, "onResponse: " + new Gson().toJson(response));

                if (response.body() != null) {

                 //   Log.d(TAG, "onResponseBody: " + new Gson().toJson(response.body()));
                    userList = new ArrayList<User>();
                    userList = response.body();

                    Collections.sort(userList, new Comparator<User>(){
                        public int compare(User obj1, User obj2) {
                            // ## Ascending order
                            return obj1.getFirstname().compareToIgnoreCase(obj2.getFirstname()); // To compare string values
                            // return Integer.valueOf(obj1.empId).compareTo(obj2.empId); // To compare integer values

                            // ## Descending order
                            // return obj2.firstName.compareToIgnoreCase(obj1.firstName); // To compare string values
                            // return Integer.valueOf(obj2.empId).compareTo(obj1.empId); // To compare integer values
                        }});

                    adapterListContacts = new AdapterListContacts();
                    adapterListContacts = new AdapterListContacts(MainActivity.this, userList);
                    adapterListContacts.notifyDataSetChanged();
                    userListView.setAdapter(adapterListContacts);
                    if(refreshApp)
                        tinydb.remove("AllUsers");
                    tinydb.putListObject("AllUsers", (ArrayList<User>) userList);
                    Global.setUserList(userList);
                    progressBar.dismiss();

                }

            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                progressBar.dismiss();
                Toast.makeText(MainActivity.this, "Can't load members", Toast.LENGTH_SHORT).show();
             //   Log.d(TAG, "onFailureLoadList: " + new Gson().toJson(t));
            }
        });
    }

    private boolean checkAndRequestPermissions()
    {
        int permissionReadContact = ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS);
        int permissionCallPhone = ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE);

        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionCallPhone != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CALL_PHONE);
        }
        if (permissionReadContact != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Closing LBG Mostar contacts")
                .setMessage("Are you sure you want to close application?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
    }


}
