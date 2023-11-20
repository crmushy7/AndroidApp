package com.example.longlast;

public class Record {
    private int id;
    private String fullName;
    private double amount;
    private String mobileNo;
    private String email;
    private String date;
    private String time;
    private String transactionType;
    private String description;

    public Record(int id,String fullName, double amount, String mobileNo, String email, String date, String time, String transactionType, String description) {
        this.id = id;
        this.fullName = fullName;
        this.amount = amount;
        this.mobileNo = mobileNo;
        this.email = email;
        this.date = date;
        this.time = time;
        this.transactionType = transactionType;
        this.description = description;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", amount=" + amount +
                ", mobileNo='" + mobileNo + '\'' +
                ", email='" + email + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", description='" + description;
    }
}
