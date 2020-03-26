package models.common;

import java.text.DecimalFormat;

import models.UserModel;

public class ResellerRecord {
    public UserModel user;
    public double rate;
    public double profit;

    public ResellerRecord(double rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("#.##");
        String userInfo = (user == null) ? "no user" : user.toString();
        return userInfo + "-" + df.format(rate);
    }
}
