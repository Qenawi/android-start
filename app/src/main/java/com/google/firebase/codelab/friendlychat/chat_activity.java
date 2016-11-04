package com.google.firebase.codelab.friendlychat;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.codelab.friendlychat.models.emailTo_id_modle;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Iterator;

public class chat_activity extends AppCompatActivity implements users_list_fragment.OnFragmentInteractionListener, user_chat_rooms.OnFragmentInteractionListener {
    final private String TAG = "chat_activity";
    private String _email;
    private String push_key;
    private DatabaseReference test_curnt_user_list_ref;
    private DatabaseReference mFirebaseDatabaseReference;
    private ViewPager viewPager;
    MyPagerAdapter adapterViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_activity);
        setTitle("Chatter");
        _email = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        viewPager=(ViewPager)findViewById(R.id.pager);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);
        //  replace();
    }

    @Override
    public void onFragmentInteraction_user_list(String uri, String name) {
        //check if room with that user was exist
        check_if_chat_room_exist(uri, name);
        //f exist open directly
        // else  creat room with welcome msG and add it To
        // email To id


    }

    @Override
    public void onFragmentInteraction_user_chat_rooms(Uri uri) {
// Directly start app
    }

    private void check_if_chat_room_exist(final String email2, final String name) {

        final DatabaseReference dprf = mFirebaseDatabaseReference.child("test");
        Query query1 = dprf.orderByChild("email").equalTo(_email);
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterator i = dataSnapshot.getChildren().iterator();
                if (dataSnapshot.getChildrenCount() > 0 && i.hasNext()) {
                    DataSnapshot d = (DataSnapshot) i.next();
                    String room_key = d.getKey();// key of the room
                    check_if_chat_room_exist_helper(email2, room_key, name);
                } else {
                    Toast.makeText(getApplication(), "user not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }//check_if_chat_room_exist

    private void check_if_chat_room_exist_helper(final String email2, String key, final String name) {
        final DatabaseReference dprf = mFirebaseDatabaseReference.child("test");
        test_curnt_user_list_ref = dprf.child(key).child("email_list");//
        Query query = dprf.child(key).child("email_list").orderByChild("email").equalTo(email2);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getChildrenCount() == 0) {
                    // add data to private room
                    creat_new_room(email2, name);
                    // add ke to my email - tmp add it to other person email
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
//-------------------------------------------------------------------
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                bring_data(dataSnapshot, name);

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                bring_data(dataSnapshot, name);
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
        });// child event listner
    }//chek if that room exist helper

    private void bring_data(DataSnapshot dataSnapshot, String name) {//room exist
        emailTo_id_modle c = dataSnapshot.getValue(emailTo_id_modle.class);
        Intent i = new Intent(getApplicationContext(), private_chatRoom_activity.class);
        i.putExtra("room_link", c.getUid());
        i.putExtra("other_person_name", name);
        startActivity(i);

    }

    private void creat_new_room(final String email, final String name)
    {
        final DatabaseReference dprf = mFirebaseDatabaseReference.child("private_rooms");

        private_room_users pvu = new private_room_users(email, _email);
        push_key = dprf.push().getKey();
        dprf.child(push_key).setValue(pvu).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                // push complete add room
                Toast.makeText(getApplication(), "push key" + push_key, Toast.LENGTH_SHORT).show();
                // add to my acc
                store_new_room_data(email, push_key);
                //  check_if_chat_room_exist(email, name);

            }
        });
    }// create new room

    private void add_to_my_acc(String email, String key) {
        // refranc to curnt user list;
        emailTo_id_modle em = new emailTo_id_modle(email, key);
        test_curnt_user_list_ref.push().setValue(em);
    }

    void store_new_room_data(final String email2, final String key)
    {
        final DatabaseReference dprf = mFirebaseDatabaseReference.child("email_to_id");
        emailTo_id_modle em = new emailTo_id_modle(email2, key);
        dprf.push().setValue(em);
        if(!email2.equals(_email))
        {
            em = new emailTo_id_modle(_email, key);
            dprf.push().setValue(em);
        }
        add_to_my_acc(email2, key);
        add_me_to_his_friend_list(email2);
    }

    private static class private_room_users {
        String user1, user2;

        public private_room_users(String user1, String user2) {
            this.user1 = user1;
            this.user2 = user2;
        }

        public private_room_users() {

        }

        public String getUser2() {
            return user2;
        }

        public void setUser2(String user2) {
            this.user2 = user2;
        }

        public String getUser1() {
            return user1;
        }

        public void setUser1(String user1) {
            this.user1 = user1;
        }


    }
   private  void add_me_to_his_friend_list(final  String email2)
   {
       final DatabaseReference dprf = mFirebaseDatabaseReference.child("test");
       Query query1 = dprf.orderByChild("email").equalTo(email2);
       query1.addListenerForSingleValueEvent(new ValueEventListener() {
           @Override
           public void onDataChange(DataSnapshot dataSnapshot) {
               Iterator i = dataSnapshot.getChildren().iterator();
               if (dataSnapshot.getChildrenCount() > 0 && i.hasNext()) {
                   DataSnapshot d = (DataSnapshot) i.next();
                   String room_key = d.getKey();// key of the room
                   emailTo_id_modle em = new emailTo_id_modle(_email, push_key);
                   dprf.child(room_key).child("email_list").push().setValue(em);
               }
               else
               {
               //
               }
           }

           @Override
           public void onCancelled(DatabaseError databaseError) {

           }
       });
   }
    //----------------pager adapter
    public static class MyPagerAdapter extends FragmentPagerAdapter {
        private static int NUM_ITEMS = 2;

        public MyPagerAdapter(FragmentManager fragmentManager)
        {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount()
        {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return users_list_fragment.newInstance(0, "Fiends");
                case 1: // Fragment # 0 - This will show FirstFragment different title
                    return user_chat_rooms.newInstance(1, "Chats");
                default:
                    return null;
            }
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }

    }//pager adapter
}// chat ctivity class
