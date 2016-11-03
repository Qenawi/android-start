package com.google.firebase.codelab.friendlychat.services;

import android.app.Service;
import android.util.Log;

/**
 * Created by QEnawi on 10/20/2016.
 */
public abstract class MyBaseTaskService extends Service {



    private static final String TAG = "MyBaseTaskService";

    private int mNumTasks = 0;



    public void taskStarted() {

        changeNumberOfTasks(1);

    }



    public void taskCompleted() {

        changeNumberOfTasks(-1);

    }



    private synchronized void changeNumberOfTasks(int delta) {

        Log.d(TAG, "changeNumberOfTasks:" + mNumTasks + ":" + delta);

        mNumTasks += delta;



        // If there are no tasks left, stop the service

        if (mNumTasks <= 0) {

            Log.d(TAG, "stopping");

            stopSelf();

        }

    }



}