package com.desirecode.videocallingapp.messages;



public class Messages {
    private String from;
    private String to;
    private String message;
    private String type;
    private String date;
    private String time;
    private String messageID;
    private String name;

    public Messages() {
    }

    public Messages(String from, String to, String message, String type, String date, String time, String messageID, String name) {
        this.from = from;
        this.to = to;
        this.message = message;
        this.type = type;
        this.date = date;
        this.time = time;
        this.messageID = messageID;
        this.name = name;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageID) {
        this.messageID = messageID;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
