package com.laundromat.customer.model;

public class Merchant extends User{

    private String nicNumber;
    private String nicImageUrl;
    private Laundry laundry;

    public Merchant() {

        super();
        // Empty constructor
    }

    public String getNicNumber() {
        return nicNumber;
    }

    public void setNicNumber(String nicNumber) {
        this.nicNumber = nicNumber;
    }

    public String getNicImageUrl() {
        return nicImageUrl;
    }

    public void setNicImageUrl(String nicImageUrl) {
        this.nicImageUrl = nicImageUrl;
    }

    public Laundry getLaundry() {
        return laundry;
    }

    public void setLaundry(Laundry laundry) {
        this.laundry = laundry;
    }
}
