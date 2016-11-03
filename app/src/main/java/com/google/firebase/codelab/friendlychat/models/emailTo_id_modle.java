package com.google.firebase.codelab.friendlychat.models;

import java.io.Serializable;

/**
 * Created by QEnawi on 10/23/2016.
 */
public class emailTo_id_modle implements Serializable {
    private String email;
    private String uid;
    public emailTo_id_modle(){}
    public emailTo_id_modle(String email, String uid)
    {
        this.email = email;
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
