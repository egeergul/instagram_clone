package com.example.instaclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

import com.example.instaclone.Adapter.ChatUserAdapter;
import com.example.instaclone.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hendraanggrian.appcompat.widget.SocialAutoCompleteTextView;

import java.util.ArrayList;
import java.util.List;

public class DirectMessageActivity extends AppCompatActivity {

    private RecyclerView recyclerViewChats;
    private ChatUserAdapter chatUserAdapter;
    private List<User> usersForChat, userForGood;
    private TextView inboxHeading;
    private List<String> userKeys;

    private SocialAutoCompleteTextView searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_direct_message);

        searchBar = findViewById(R.id.search_bar);
        inboxHeading = findViewById(R.id.inbox_heading);
        recyclerViewChats = findViewById(R.id.recycler_view_chats);
        recyclerViewChats.setHasFixedSize(true);
        recyclerViewChats.setLayoutManager(new LinearLayoutManager(this));
        usersForChat = new ArrayList<>();
        userForGood = new ArrayList<>();
        userKeys = new ArrayList<>();
        chatUserAdapter = new ChatUserAdapter(this, usersForChat);
        recyclerViewChats.setAdapter(chatUserAdapter);

        getUsersForChat();
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchUser(charSequence.toString());

            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("");

        FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        inboxHeading.setText("");
                        inboxHeading.setText(user.getName()+"'s Inbox");
                    }

                    @Override
                    public void onCancelled(@NonNull  DatabaseError error) {

                    }
                });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            }
        });

    }

    private void searchUser(String st) {
        usersForChat.clear();
        for(User user: userForGood){
            if( user.getUsername().length() >= st.length() &&
                    user.getUsername().substring(0,st.length()).equals(st)){
                usersForChat.add(user);
            }
        }
        chatUserAdapter.notifyDataSetChanged();
    }

    private void getUsersForChat() {
        userKeys.clear();
        FirebaseDatabase.getInstance().getReference().child("Messages").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                            userKeys.add(dataSnapshot.getKey());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        FirebaseDatabase.getInstance().getReference().child("Follow").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    boolean add = true;
                    for(String key: userKeys){
                        if(key.equals(dataSnapshot.getKey()))
                            add = false;
                    }
                    if(add)
                        userKeys.add( dataSnapshot.getKey());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseDatabase.getInstance().getReference().child("Users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                usersForChat.clear();
                userForGood.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    for(String key: userKeys){
                        if(dataSnapshot.getKey().equals(key)){
                            User user = dataSnapshot.getValue(User.class);
                            usersForChat.add(user);
                            userForGood.add(user);
                        }
                    }
                }
                chatUserAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        finish();
    }
}