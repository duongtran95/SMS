package com.example.trantrungduong95.truesms.Model;


/**
 * Created by ngomi_000 on 7/1/2017.
 */

public class Search {
    private String num;
    private String content;

    public Search() {
    }

    public Search(String num, String content) {
        this.num = num;
        this.content = content;
    }

    public String getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = num;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
