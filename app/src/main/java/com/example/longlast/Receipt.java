package com.example.longlast;

public class Receipt {
    private String debtor;
    private String debtorEmail;
    private String amount;
    private String date;
    private String time;
    private String status;
    private String transactionId;


// Constructors, getters, and setters

    public Receipt() {
        // Default constructor required for Firebase
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public Receipt(String debtor, String debtorEmail, String amount, String date, String time, String status, String transactionId) {
        this.debtor = debtor;
        this.debtorEmail = debtorEmail;
        this.amount = amount;
        this.date = date;
        this.time = time;
        this.status = status;
        this.transactionId=transactionId;
    }

    public String getDebtor() {
        return debtor;
    }

    public void setDebtor(String debtor) {
        this.debtor = debtor;
    }

    public String getDebtorEmail() {
        return debtorEmail;
    }

    public void setDebtorEmail(String debtorEmail) {
        this.debtorEmail = debtorEmail;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}

