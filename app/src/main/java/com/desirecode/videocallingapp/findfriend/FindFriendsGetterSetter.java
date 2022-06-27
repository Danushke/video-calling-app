package com.desirecode.videocallingapp.findfriend;

public class FindFriendsGetterSetter {
    /***in here this variable name are should equal with the name of KEY in firebase */
    String username,status,userimage;

    public FindFriendsGetterSetter(String userName, String status, String userimage) {
        this.username = userName;
        this.status = status;
        this.userimage = userimage;
    }
    public FindFriendsGetterSetter(){

    }

    public String getUserName() {
        return username;
    }

    public void setUserName(String userName) {
        this.username = userName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImage() {
        return userimage;
    }

    public void setImage(String image) {
        this.userimage = userimage;
    }
}
