package com.laundromat.admin.model;

import com.google.gson.Gson;
import com.laundromat.admin.model.util.PaymentMethod;
import com.laundromat.admin.model.util.TransactionStatus;
import com.laundromat.admin.model.util.TransactionType;
import com.laundromat.admin.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

public class Transaction {

    private String id;
    private double amount;
    private PaymentMethod paymentMethod;
    private TransactionType type;
    private String dateCreated;

    public Transaction() {

        this.dateCreated = StringUtils.getCurrentDateTime();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public String getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(String dateCreated) {
        this.dateCreated = dateCreated;
    }

    public JSONObject toJson() {

        Gson gson = new Gson();
        String jsonString = gson.toJson(this);

        try {
            return new JSONObject(jsonString);
        } catch (JSONException e) {
            return null;
        }
    }
}
