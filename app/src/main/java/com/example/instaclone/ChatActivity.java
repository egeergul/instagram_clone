package com.example.instaclone;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.instaclone.Adapter.ChatMessageAdapter;
import com.example.instaclone.Model.Message;
import com.example.instaclone.Model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {

    private TextView name;
    private CircleImageView imageProfile;
    private RecyclerView recyclerViewMessages;
    private Button send;
    private EditText editText;

    private ChatMessageAdapter chatMessageAdapter;
    private List<Message> messageList;

    DatabaseReference ref1;
    DatabaseReference ref2;

    FirebaseUser fUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent intent = getIntent();
        String otherPersonId = intent.getStringExtra("userId");

        fUser = FirebaseAuth.getInstance().getCurrentUser();

        ref1 = FirebaseDatabase.getInstance().getReference().child("Messages").child(fUser.getUid()).
                child(otherPersonId);
        ref2 = FirebaseDatabase.getInstance().getReference().child("Messages").child(otherPersonId)
                .child(fUser.getUid());

        name = findViewById(R.id.name);
        imageProfile = findViewById(R.id.image_profile);
        send = findViewById(R.id.send);
        editText = findViewById(R.id.edit_text);
        recyclerViewMessages = findViewById(R.id.recycler_view_messages);
        messageList = new ArrayList<>();
        chatMessageAdapter = new ChatMessageAdapter(this, messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerViewMessages.setLayoutManager(layoutManager);
        recyclerViewMessages.setHasFixedSize(true);
        recyclerViewMessages.setAdapter(chatMessageAdapter);

        name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra("publisherId", otherPersonId);
                getApplicationContext().startActivity(intent);
            }
        });

        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                ////TODO
                intent.putExtra("publisherId", otherPersonId);
                getApplicationContext().startActivity(intent);
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                String messageText;
                if (editText.getText().toString().isEmpty()==false) {
                    messageText = editText.getText().toString();
                    sendMessage(messageText);
                }
            }
        });

        getChatInfo(otherPersonId);
        getChatHistory(otherPersonId);

    }

    private void getChatHistory(String otherPersonsId) {
        FirebaseDatabase.getInstance().getReference().child("Messages").child(fUser.getUid()).
                child(otherPersonsId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                    Message message = dataSnapshot.getValue(Message.class);

                    if(message.getAuthor().equals(otherPersonsId) && dataSnapshot.getKey().equals("lastMessage")){
                        ref1.child("lastMessage").child("seen").setValue(true);
                        ref2.child(("lastMessage")).child("seen").setValue(true);
                    } else if (message.getAuthor().equals(otherPersonsId)){
                        ref1.child(message.getId()).child("seen").setValue(true);
                        ref2.child(message.getId()).child("seen").setValue(true);
                    }

                    if(!dataSnapshot.getKey().equals("lastMessage")){
                        messageList.add(message);
                    }

                }
                chatMessageAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull  DatabaseError error) {

            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendMessage(String messageText) {

        String messageId = ref1.push().getKey();

        long millis = Instant.now().toEpochMilli();
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", messageId);
        map.put("text", messageText);
        map.put("seen", false);
        map.put("author", fUser.getUid());
        map.put("timestamp", millis);

        ref1.child(messageId).setValue(map);
        ref2.child(messageId).setValue(map);

        ref1.child("lastMessage").setValue(map);
        ref2.child("lastMessage").setValue(map);

        editText.setText("");

    }

    private void getChatInfo(String otherPersonId) {
        FirebaseDatabase.getInstance().getReference().child("Users").child(otherPersonId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot snapshot) {
                User otherPerson = snapshot.getValue(User.class);
                Picasso.get().load(otherPerson.getImageUrl()).placeholder(R.mipmap.ic_launcher).into(imageProfile);
                name.setText(otherPerson.getName());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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