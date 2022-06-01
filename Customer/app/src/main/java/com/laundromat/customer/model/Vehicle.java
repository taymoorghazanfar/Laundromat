package com.laundromat.customer.model;

import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Vehicle {

    public String id;
    public String name;
    public String plateNumber;
    public HashMap<String, String> images;
    public String color;
    public int model;

    public Vehicle(String name, String plateNumber, String color, int model, HashMap<String, String> images) {
        this.name = name;
        this.plateNumber = plateNumber;
        this.color = color;
        this.model = model;
        this.images = images;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public HashMap<String, String> getImages() {
        return images;
    }

    public void setImages(HashMap<String, String> images) {
        this.images = images;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getModel() {
        return model;
    }

    public void setModel(int model) {
        this.model = model;
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
