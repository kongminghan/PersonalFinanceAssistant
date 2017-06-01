package com.example.minghan.expense;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by MingHan on 5/31/2017.
 */

public class Expenses {
    private long timestamp;
    private double amount;
    private String category;
    private String id;

    public Expenses(double amount, String category) {
        this.amount = amount;
        this.category = category;
    }

    public Expenses(){}

    public void setTimestamp(long timestamp){
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
