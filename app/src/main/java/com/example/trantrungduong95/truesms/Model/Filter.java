package com.example.trantrungduong95.truesms.Model;

/**
 * Created by ngomi_000 on 7/9/2017.
 */

public class Filter {
    private int id_;
    private String char_;
    private String word_;
    private String pharse_;

    public Filter(int id_, String char_, String word_, String pharse_) {
        this.id_ = id_;
        this.char_ = char_;
        this.word_ = word_;
        this.pharse_ = pharse_;
    }
    public Filter() {

    }

    public int getId_() {
        return id_;
    }

    public void setId_(int id_) {
        this.id_ = id_;
    }

    public String getChar_() {
        return char_;
    }

    public void setChar_(String char_) {
        this.char_ = char_;
    }

    public String getWord_() {
        return word_;
    }

    public void setWord_(String word_) {
        this.word_ = word_;
    }

    public String getPharse_() {
        return pharse_;
    }

    public void setPharse_(String pharse_) {
        this.pharse_ = pharse_;
    }
}
