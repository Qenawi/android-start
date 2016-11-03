package com.google.firebase.codelab.friendlychat.models;

/**
 * Created by QEnawi on 10/22/2016.
 */
public class users_data_modle
{
    private String name;
    private String email;
    private String photo_url;


  public users_data_modle(){}

    public users_data_modle(String email, String name, String photo_url)
    {
        this.email = email;
        this.name = name;
        this.photo_url = photo_url;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoto_url() {
        return photo_url;
    }

    public void setPhoto_url(String photo_url) {
        this.photo_url = photo_url;
    }
}
