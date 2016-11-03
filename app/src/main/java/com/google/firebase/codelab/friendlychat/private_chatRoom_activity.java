package com.google.firebase.codelab.friendlychat;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.codelab.friendlychat.models.private_room_msg_room_model;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class private_chatRoom_activity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView messengerTextView;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(android.R.id.text1);
            messengerTextView = (TextView) itemView.findViewById(android.R.id.text2);
        }
    }

    private static final String TAG = "private_chatRoom_";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 10;
    private String mUsername;
    private SharedPreferences mSharedPreferences;
    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private EditText mMessageEditText;
    private String room_link;
    private String other_person_name;
    // Firebase instance variables
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<private_room_msg_room_model, MessageViewHolder> mFirebaseAdapter;

    void lizationsini() {
        mUsername = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.private_chat_room_messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);

        mMessageEditText = (EditText) findViewById(R.id.private_chat_room_messageEditText);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mSharedPreferences
                .getInt(CodelabPreferences.FRIENDLY_MSG_LENGTH, DEFAULT_MSG_LENGTH_LIMIT))});
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //get room name from bundle

        room_link = getIntent().getStringExtra("room_link");
        other_person_name = getIntent().getStringExtra("other_person_name");

        setContentView(R.layout.activity_private_chat_room_activity);
        setTitle(other_person_name);
        lizationsini();
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        mFirebaseAdapter = new FirebaseRecyclerAdapter<private_room_msg_room_model, MessageViewHolder>
                (
                        private_room_msg_room_model.class,
                        android.R.layout.simple_list_item_2,
                        MessageViewHolder.class,
                        mFirebaseDatabaseReference.child("private_rooms/" + room_link + "/user_messages")
                )

        {
            @Override
protected void populateViewHolder(MessageViewHolder viewHolder, private_room_msg_room_model model, int position) {

                viewHolder.messageTextView.setText(model.getMessage());
                viewHolder.messengerTextView.setText(model.getName());
            }
        };
        //--------------
        //Scrool to the last new income messege-->
        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }//onItemRangeInserted
        });
        // registerAdapterDataObserver
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
//---------------------------------send-----------------------------------------------------------
        mSendButton = (Button) findViewById(R.id.private_chat_room_sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//send massage
                private_room_msg_room_model friendlyMessageModel = new private_room_msg_room_model(mMessageEditText.getText().toString(), mUsername);
                mFirebaseDatabaseReference.child("private_rooms/" + room_link + "/user_messages").push().setValue(friendlyMessageModel);
                //ref.push().setValue(item)
                mMessageEditText.setText("");
            }
        });//on Click listener
    }//on create


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}