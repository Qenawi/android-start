package com.google.firebase.codelab.friendlychat.models;

/**
 * Created by QEnawi on 10/27/2016.
 */
public class private_room_msg_room_model
{
    String name;
    String message;
public  private_room_msg_room_model(){}
    public private_room_msg_room_model(String message, String name)
    {
        this.message = message;
        this.name = name;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
