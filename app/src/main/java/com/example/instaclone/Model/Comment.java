package com.example.instaclone.Model;

public class Comment {

    private String id;
    private String comment;
    private String publisher;

    //IMPORTANT TO ADD A DEFAULT CONSTRUCTOR (EMPTY)
    public Comment(){}

    public Comment(String id, String comment, String publisher) {
        this.id = id;
        this.comment = comment;
        this.publisher = publisher;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
}
