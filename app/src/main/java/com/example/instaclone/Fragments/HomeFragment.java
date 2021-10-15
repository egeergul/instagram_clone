package com.example.instaclone.Fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.instaclone.Adapter.PostAdapter;
import com.example.instaclone.DirectMessageActivity;
import com.example.instaclone.MainActivity;
import com.example.instaclone.Model.Message;
import com.example.instaclone.Model.Post;
import com.example.instaclone.R;
import com.example.instaclone.StartActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerViewPosts;
    private PostAdapter postAdapter;
    private List<Post> mPosts;
    private ImageView inbox;
    private View unreadMessages;
    private List<String> followingLists;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        inbox = view.findViewById(R.id.inbox);
        unreadMessages = view.findViewById(R.id.unread_messages);
        recyclerViewPosts = view.findViewById(R.id.recyclerview_posts);
        recyclerViewPosts.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        linearLayoutManager.setReverseLayout(true);
        recyclerViewPosts.setLayoutManager(linearLayoutManager);
        mPosts = new ArrayList<>();
        postAdapter = new PostAdapter(getContext(), mPosts);
        recyclerViewPosts.setAdapter(postAdapter);

        followingLists = new ArrayList<>();
        
        checkFollowingUsers();
        //checkUnreadChats();

        inbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), DirectMessageActivity.class);
                getContext().startActivity(intent);
            }
        });

        return  view;
    }

    @Override
    public void onStart() {
        super.onStart();
        checkUnreadChats();
    }

    private void checkUnreadChats() {

        FirebaseDatabase.getInstance().getReference().child("Messages").
                child(FirebaseAuth.getInstance().getCurrentUser().getUid()).
                addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull  DataSnapshot snapshot) {
                        boolean unreadMessagesExist = false;
                        for(DataSnapshot dataSnapshot: snapshot.getChildren()){
                            Message message = dataSnapshot.child("lastMessage").getValue(Message.class);
                            if(message!=null && !message.getAuthor().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()) &&
                                    message.getSeen()==false){
                                unreadMessagesExist =true;

                            };
                        }
                        if(unreadMessagesExist)
                            unreadMessages.setVisibility(View.VISIBLE);
                        else
                            unreadMessages.setVisibility(View.GONE);

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


    }

    private void checkFollowingUsers() {
        FirebaseDatabase.getInstance().getReference().child("Follow").child(FirebaseAuth.getInstance().
                getCurrentUser().getUid()).child("following").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull  DataSnapshot dataSnapshot) {
                followingLists.clear();
                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    followingLists.add(snapshot.getKey());
                }
                followingLists.add(FirebaseAuth.getInstance().getCurrentUser().getUid());
                readPosts();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void readPosts() {
        FirebaseDatabase.getInstance().getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mPosts.clear();

                for(DataSnapshot snapshot: dataSnapshot.getChildren()){
                    Post post = snapshot.getValue(Post.class);
                    for(String id: followingLists){
                        if(post.getPublisher().equals(id))
                            mPosts.add(post);
                    }
                }

                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}