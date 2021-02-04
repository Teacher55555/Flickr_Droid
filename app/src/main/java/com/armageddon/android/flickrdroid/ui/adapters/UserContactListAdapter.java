package com.armageddon.android.flickrdroid.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.armageddon.android.flickrdroid.R;
import com.armageddon.android.flickrdroid.common.LogoIcon;
import com.armageddon.android.flickrdroid.model.Person;
import com.bumptech.glide.Glide;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Shows user's contact list in 3 columns.
 * Click on user's logo replaces UsersContactsFragment to PersonsFragment in current activity.
 */

public class UserContactListAdapter
        extends RecyclerView.Adapter<UserContactListAdapter.ContactsViewHolder> {
    private final List<Person.Contact> mContacts;
    private final CallBacks mCallBacks;

    public interface CallBacks {
        void onPersonItemClick (String userId, String userName);
    }

    public UserContactListAdapter(List<Person.Contact> contacts, Context context) {
        mContacts = contacts;
        mCallBacks = (CallBacks) context;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_list_item, parent, false);
        return new ContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactsViewHolder holder, int position) {
        holder.onBind(mContacts.get(position));
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

      class ContactsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
       private final ImageView mLogo;
       private final TextView mName;
       private Person.Contact mContact;

       public ContactsViewHolder(@NonNull View itemView) {
           super(itemView);
           mLogo = itemView.findViewById(R.id.user_icon);
           mName = itemView.findViewById(R.id.contact_name);
           itemView.setOnClickListener(this);
       }

       public void onBind (Person.Contact contact) {
           Glide.with(itemView)
                   .load(contact.getUrl(LogoIcon.normal_100px))
                   .error(R.drawable.icon_person_filled)
                   .into(mLogo);
           mName.setText(contact.getUsername());
           mContact = contact;
       }

         @Override
         public void onClick(View v) {
             mCallBacks.onPersonItemClick(mContact.getNsid(), mContact.getUsername());
         }
     }
}















