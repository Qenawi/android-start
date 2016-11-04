package com.google.firebase.codelab.friendlychat;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.codelab.friendlychat.models.emailTo_id_modle;
import com.google.firebase.codelab.friendlychat.models.users_data_modle;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class user_chat_rooms extends android.support.v4.app.Fragment {
    private OnFragmentInteractionListener mListener;

    private DatabaseReference mFirebaseDatabaseReference, mFirebaseDatabaseReference2;
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private_room_users PRU;
    ListView chat_list;
    private String other_person_chat_email, other_person_chat_name, chat_email;
     users_data_modle udm;
    ArrayList<String> rooms_list;
    ArrayList<String> key_oF;
    emailTo_id_modle data_snap_shot;
    ArrayAdapter<String> arrayAdapter;

    public user_chat_rooms() {
        // Required empty public constructor
    }
    //----------------
    private String title;
    private int page;
    public static user_chat_rooms newInstance(int page, String title) {
        user_chat_rooms fragmentFirst = new user_chat_rooms();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }
   //-----------------
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");
        //
        rooms_list = new ArrayList<>();
        key_oF=new ArrayList<>();
        PRU=new private_room_users();
        arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, rooms_list);
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        chat_email = mFirebaseUser.getEmail();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference().child("email_to_id");
        mFirebaseDatabaseReference2 = FirebaseDatabase.getInstance().getReference();
        ChildEventListener query = mFirebaseDatabaseReference.orderByChild("email").equalTo(mFirebaseUser.getEmail())
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                       _numper_of_user_rooms(dataSnapshot);
                    }
                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                       _numper_of_user_rooms(dataSnapshot);
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
//-----------------------------
    }//on create
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_user_chat_rooms, container, false);
        chat_list = (ListView) root.findViewById(R.id.chat_room_list);
        chat_list.setAdapter(arrayAdapter);
        chat_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Toast.makeText(getActivity(), key_oF.get(position), Toast.LENGTH_LONG).show();
            }
        });
        return root;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction_user_chat_rooms(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction_user_chat_rooms(Uri uri);
    }

    //
    void _numper_of_user_rooms(DataSnapshot dataSnapshot) {
        data_snap_shot = new emailTo_id_modle();
        data_snap_shot = dataSnapshot.getValue(emailTo_id_modle.class);
        key_oF.add(data_snap_shot.getUid());
        get_room_info(data_snap_shot.getUid());
    }

    void get_room_info(final String child_name)
    {
        // get email of chat person
        mFirebaseDatabaseReference2.child("private_rooms").child(child_name)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                     Log.v("user_chat_rooms","geet rooom info"+child_name);
                    PRU=dataSnapshot.getValue(private_room_users.class);
                    if (chat_email.equals(PRU.getUser1()))
                    {
                        other_person_chat_email=PRU.getUser2();
                    }
                    else
                    {

                        other_person_chat_email=PRU.getUser1();
                    }
                        get_name_of_user(other_person_chat_email);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

// get

    }
    // class to read user data from private messeges
    private static class private_room_users {
        public private_room_users() {
        }

        String user1, user2;

        public private_room_users(String user1, String user2) {
            this.user1 = user1;
            this.user2 = user2;
        }

        public String getUser1() {
            return user1;
        }

        public void setUser1(String user1) {
            this.user1 = user1;
        }

        public String getUser2() {
            return user2;
        }

        public void setUser2(String user2) {
            this.user2 = user2;
        }
    }
    private void get_name_of_user(String chat_email)
    {

        DatabaseReference dp_ref;
        dp_ref=FirebaseDatabase.getInstance().getReference().child("users");
        ChildEventListener query =  dp_ref.orderByChild("email").equalTo(chat_email).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s)
            {

                udm=dataSnapshot.getValue(users_data_modle.class);
                other_person_chat_name=udm.getName();
                rooms_list.add(other_person_chat_name);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s)
            {
                udm=dataSnapshot.getValue(users_data_modle.class);
                other_person_chat_name=udm.getName();
                rooms_list.add(other_person_chat_name);
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot)
            {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
// Close connection DatabaseReference.goOffline();