package com.google.firebase.codelab.friendlychat;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.firebase.codelab.friendlychat.models.users_data_modle;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class users_list_fragment extends android.support.v4.app.Fragment {

    private OnFragmentInteractionListener mListener;
    ListView user_list;
    Map  <Integer, String> photo_url_list = new HashMap<>();
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseListAdapter<users_data_modle> mlistadapter;
    public static final String users = "users";
    TextView messageTextView;
    TextView messengerTextView;
    CircleImageView messengerImageView;

    public users_list_fragment() {
        // Required empty public constructor
    }
//
private String title;
    private int page;
    public static users_list_fragment newInstance(int page, String title) {
        users_list_fragment fragmentFirst = new users_list_fragment();
        Bundle args = new Bundle();
        args.putInt("someInt", page);
        args.putString("someTitle", title);
        fragmentFirst.setArguments(args);
        return fragmentFirst;
    }
 //
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        //
        page = getArguments().getInt("someInt", 0);
        title = getArguments().getString("someTitle");
        }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_users_list_fragment, container, false);
        user_list = (ListView) root.findViewById(R.id.user_list);
        mlistadapter = new FirebaseListAdapter<users_data_modle>(getActivity(), users_data_modle.class, R.layout.item_message, mFirebaseDatabaseReference.child(users)) {
            @Override
            protected void populateView(View itemView, users_data_modle s, int position)
            {
                messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
                messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
                messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
                messageTextView.setText(s.getName());
                messengerTextView.setText(s.getEmail());
                photo_url_list.put(position,s.getPhoto_url());
                Glide.with(getActivity())
                        .load(s.getPhoto_url())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .into(messengerImageView);
            }
        };
        user_list.setAdapter(mlistadapter);
        user_list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {

           TextView tex= (TextView)view.findViewById(R.id.messengerTextView);
                TextView tex2= (TextView)view.findViewById(R.id.messageTextView);
           notify_chat_activity(tex.getText().toString(),tex2.getText().toString(),photo_url_list.get(position));
            }
        });
        return root;
    }


    public void notify_chat_activity(String chosen_email,String name,String photo_link)
    {
        if (mListener != null)
        {
            mListener.onFragmentInteraction_user_list(chosen_email,name,photo_link);
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
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction_user_list(String uri,String name,String photo_link);
    }

}
