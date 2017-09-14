package com.example.arc.bestcontacts.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.example.arc.bestcontacts.R;
import com.example.arc.bestcontacts.models.User;
import com.example.arc.bestcontacts.utils.Global;

import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by arc on 12/06/17.
 */

public class AdapterListContacts extends BaseAdapter  implements Filterable {

    Context context;
    List<User> userList;
    LayoutInflater layoutInflater;


    public AdapterListContacts() {
    }

    public AdapterListContacts(Context ctx, List<User> userList) {
        this.context = ctx;
        this.layoutInflater = (LayoutInflater) ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.userList = userList;

    }

    static class ListHolder {

        TextView lbl_uid;
        TextView lbl_firstName;
        TextView lbl_nickname;
        TextView lbl_number;
        TextView lbl_email;
        Button btn_add;
        Button btn_call;
    }

    @Override
    public int getCount() {
        return userList.size();
    }

    @Override
    public Object getItem(int position) {
        return userList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ListHolder listHolder;
        if (convertView == null) {
            listHolder = new ListHolder();
            convertView = layoutInflater.inflate(R.layout.list_item, parent, false);
            //listHolder.lbl_uid = (TextView) convertView.findViewById(R.id.uid);
            listHolder.lbl_firstName = (TextView) convertView.findViewById(R.id.first_name);
            listHolder.lbl_email = (TextView) convertView.findViewById(R.id.email);
            listHolder.lbl_number = (TextView) convertView.findViewById(R.id.number);
            listHolder.lbl_nickname = (TextView) convertView.findViewById(R.id.nick_name);
            listHolder.btn_call = (Button) convertView.findViewById(R.id.btnCall);
            listHolder.btn_add = (Button) convertView.findViewById(R.id.btnAddContact);

            convertView.setTag(listHolder);
        } else {
            listHolder = (ListHolder) convertView.getTag();
        }

        //listHolder.lbl_uid.setText(userList.get(position).id);
        listHolder.lbl_firstName.setText(String.valueOf(userList.get(position).getFirstname() + " " + String.valueOf(userList.get(position).getLastname() )));
        listHolder.lbl_email.setText(String.valueOf(userList.get(position).getEmail()));
        listHolder.lbl_nickname.setText(String.valueOf(userList.get(position).getNickname()));
        listHolder.lbl_number.setText(String.valueOf(userList.get(position).getNumber()));

        listHolder.btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_INSERT);
                intent.setType(ContactsContract.Contacts.CONTENT_TYPE);

                intent.putExtra(ContactsContract.Intents.Insert.NAME,  listHolder.lbl_firstName.getText());
                intent.putExtra(ContactsContract.Intents.Insert.PHONE, listHolder.lbl_number.getText());
                intent.putExtra(ContactsContract.Intents.Insert.EMAIL, listHolder.lbl_email.getText());

                context.startActivity(intent);
            }
        });

        listHolder.btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + listHolder.lbl_number.getText()));

                context.startActivity(intent);
            }
        });
        return convertView;
    }

    @Override
    public Filter getFilter() {

        Filter filter = new Filter() {

            public static final String TAG = "FILTER";

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, Filter.FilterResults results) {

                userList = (List<User>) results.values;
                notifyDataSetChanged();
            }

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                ArrayList<User> FilteredArrayNames = new ArrayList<User>();

                // perform your search here using the searchConstraint String.

                String userInput = constraint.toString().toLowerCase();
                for (int i = 0; i < Global.getUserList().size(); i++) {
                    User user = Global.getUserList().get(i);
                    String dataNames = user.getFirstname() + " " + user.getLastname() + " " + user.getNickname();
                    dataNames = dataNames.toLowerCase();

                    /*Replace special characters because some users don't have BHS keyboard*/
                    dataNames = dataNames.replace('č','c');
                    dataNames = dataNames.replace('ć','c');
                    dataNames = dataNames.replace('ž','z');
                    dataNames = dataNames.replace('đ','d');
                    dataNames = dataNames.replace('š','s');

                    /* Remove all from user input */

                    userInput = userInput.replace('ć','c');
                    userInput = userInput.replace('ž','z');
                    userInput = userInput.replace('đ','d');
                    userInput = userInput.replace('š','s');

                    if (dataNames.contains(userInput.toString()))  {
                        FilteredArrayNames.add(user);
                    }
                }
                results.count = FilteredArrayNames.size();
                results.values = FilteredArrayNames;

                return results;
            }
        };

        return filter;
    }


}
