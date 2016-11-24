/**
 * Copyright Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.firebase.codelab.friendlychat;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.StringSignature;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.codelab.friendlychat.models.FriendlyMessage_model;
import com.google.firebase.codelab.friendlychat.services.MyUploadService;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {
    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextView;
        public TextView messengerTextView;
        public CircleImageView messengerImageView;

        public MessageViewHolder(View v) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
            messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
        }
    }

    private static final String TAG = "MainActivity";
    public static final String MESSAGES_CHILD = "messages";// DP child name
    private static final int REQUEST_INVITE = 1;
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 10;
    public static final String ANONYMOUS = "anonymous";
    private static final String MESSAGE_SENT_EVENT = "message_sent";
    int PICK_PHOTO_FOR_AVATAR = 25;//X
    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    GoogleApiClient mGoogleApiClient;
    private Button mUbloodImageButton;
    private Button mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;

    // Firebase instance variables
    private BroadcastReceiver mBroadcastReceiver;
    private Uri mFileUri = null;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<FriendlyMessage_model, MessageViewHolder> mFirebaseAdapter;

    //                               datamodle            vH
    private String get_photo() {
        String email = mFirebaseAuth.getCurrentUser().getEmail();
        email += "_p_photo";
        return "https://firebasestorage.googleapis.com/v0/b/frindly-chat-app.appspot.com/o/photos%2F" + email +
                "?alt=media&token=1af70b9c-8949-4426-a1a6-8c7b732f6ac1";
    }

    void is_user_signed() {

        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        mProgressBar.setVisibility(ProgressBar.VISIBLE);
        if (mFirebaseUser == null)
        {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        }
        else
        {
            mUsername = mFirebaseUser.getDisplayName();
            if (!get_img().equals("null"))
            {
                mPhotoUrl = get_img();

            }
            else if (mFirebaseUser.getPhotoUrl() != null)
            {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
        }
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
    }

    void lizationsini() {
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mSharedPreferences
                .getInt(CodelabPreferences.FRIENDLY_MSG_LENGTH, DEFAULT_MSG_LENGTH_LIMIT))});
    }

    void Store_img(String state) {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("p_photo", state).commit();
        update_image_url(state);
    }

    String get_img() {
        return PreferenceManager.getDefaultSharedPreferences(this).getString("p_photo", "null");
    }

    void Store_seg(String state) {

        PreferenceManager.getDefaultSharedPreferences(this).edit().putString("p_photo_seg", state).commit();
        //segneture
    }

    String get_seg() {
        return PreferenceManager.getDefaultSharedPreferences(this).getString("p_photo_seg", "null");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("ChaTter");
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        // Set default username is anonymous.
        mUsername = ANONYMOUS;
//----------------------------
// Initialize Firebase Auth
        lizationsini();
        is_user_signed();
//-------------------------
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        // Initialize ProgressBar and RecyclerView.

        //----------------------------------
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
//--------------------------------------------------
// New child entries  DP
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();//ref
        mFirebaseAdapter = new FirebaseRecyclerAdapter<FriendlyMessage_model, MessageViewHolder>
                (
                        FriendlyMessage_model.class,//data typr class  ex string
                        R.layout.item_message,//layout
                        MessageViewHolder.class,//holder
                        mFirebaseDatabaseReference.child(MESSAGES_CHILD)//ref
                ) {

            @Override
            protected void populateViewHolder(MessageViewHolder viewHolder,
                                              FriendlyMessage_model friendlyMessageModel, int position)
            {
                //data returnd from recycler view adapter
                viewHolder.messageTextView.setText(friendlyMessageModel.getText());
                viewHolder.messengerTextView.setText(friendlyMessageModel.getName());
                if (friendlyMessageModel.getPhotoUrl() == null) {
                    viewHolder.messengerImageView
                            .setImageDrawable(ContextCompat
                                    .getDrawable(MainActivity.this,
                                            R.drawable.ic_account_circle_black_36dp));
                } else {
                    Glide.with(MainActivity.this)
                            .load(friendlyMessageModel.getPhotoUrl())
                            .signature(new StringSignature(get_seg()))
                            .into(viewHolder.messengerImageView);

                }
            }// view holder
        };
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
        });// registerAdapterDataObserver
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
//---------------------------------send-----------------------------------------------------------

        mSendButton = (Button) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//send massage
                Log.v(TAG, "TeRT:7:" + mPhotoUrl);
                FriendlyMessage_model friendlyMessageModel = new FriendlyMessage_model(mMessageEditText.getText().toString(), mUsername, mPhotoUrl);
                mFirebaseDatabaseReference.child(MESSAGES_CHILD).push().setValue(friendlyMessageModel);
                //ref.push().setValue(item)
                mMessageEditText.setText("");
            }
        });//on Click listener
        //--------------
        mUbloodImageButton = (Button) findViewById(R.id.ubButton);
        mUbloodImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // uplood ImaGE
                pickImage();
            }
        });
//-----------------------------------------------------------------------------
        mBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive:" + intent);
                switch (intent.getAction()) {
                case  MyUploadService.UPLOAD_COMPLETED:
                    Store_seg("" + (System.currentTimeMillis()));
                    mFirebaseAdapter.notifyDataSetChanged();
                    Store_img(get_photo());
                    break;
                    case MyUploadService.UPLOAD_ERROR:
                        break;
                }
            }

        };//broad cast receiver
        //---------------
    }// on create

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in.
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mBroadcastReceiver, MyUploadService.getIntentFilter());
        // TODO: Add code to check if user is signed in.
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mBroadcastReceiver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chat_rooms:
                //  fetchConfig();
                startActivity(new Intent(this, chat_activity.class));
                return true;
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                mUsername = ANONYMOUS;
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }

    private void sendInvitation() {
        Intent intent = new AppInviteInvitation.IntentBuilder(getString(R.string.invitation_title))
                .setMessage(getString(R.string.invitation_message))
                .setCallToActionText(getString(R.string.invitation_cta))
                .build();
        startActivityForResult(intent, REQUEST_INVITE);
    }

    public void pickImage() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PHOTO_FOR_AVATAR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode +
                ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Check how many invitations were sent.
                String[] ids = AppInviteInvitation
                        .getInvitationIds(resultCode, data);
                Log.d(TAG, "Invitations sent: " + ids.length);
            } else {
                // Sending failed or it was canceled, show failure message to
                // the user
                Log.d(TAG, "Failed to send invitation.");
            }
        } else if (requestCode == PICK_PHOTO_FOR_AVATAR && resultCode == Activity.RESULT_OK) {
            Uri selectedImageURI = data.getData();
            uploadFromUri(selectedImageURI);

        }
    }

    //---------------------
    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());
        // Save the File URI
        mFileUri = fileUri;
        // Clear the last download, if any
//        updateUI(mAuth.getCurrentUser());
        //      mDownloadUrl = null;
        // Toast message in case the user does not see the notificatio
        Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show();
        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        startService(new Intent(this, MyUploadService.class)
                .putExtra(MyUploadService.EXTRA_FILE_URI, fileUri)
                .setAction(MyUploadService.ACTION_UPLOAD));
    }// uplode from uri

    void update_image_url(final String photo_url) {
        Query query = mFirebaseDatabaseReference.child("users").orderByChild("email").equalTo(mFirebaseUser.getEmail());
        if (query == null) return;
        query.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot nodeDataSnapshot = dataSnapshot.getChildren().iterator().next();
                String key = nodeDataSnapshot.getKey(); // this key is `K1NRz9l5PU_0CFDtgXz`
                String path = "/" + dataSnapshot.getKey() + "/" + key;
                HashMap<String, Object> result = new HashMap<>();
                result.put("photo_url", photo_url);
                mFirebaseDatabaseReference.child(path).updateChildren(result);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.v("Tmp", ">>> Error:" + "find onCancelled:" + databaseError);
            }
        });
    }


}
//  m7tag 7abet ta7senat y3ni 3ayz a5ali code mtrtb we m4 3abilo we edelo