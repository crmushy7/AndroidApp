package com.example.longlast;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseSupport extends SQLiteOpenHelper {

    //-----------------------------RECORDS--------------------------------------//
    public static final String RECORDS_TABLE = "RECORD_TABLE";
    public static final String COLUMN_RECORD_ID = "RECORD_ID";
    public static final String COLUMN_RECORD_NAME = "RECORD_NAME";
    public static final String COLUMN_RECORD_MOBILE_NO = "RECORD_MOBILE";
    public static final String COLUMN_RECORD_AMOUNT = "RECORD_AMOUNT";
    public static final String COLUMN_RECORD_EMAIL = "RECORD_EMAIL";
    public static final String COLUMN_RECORD_TRANSACTION_TYPE = "RECORD_TYPE";
    public static final String COLUMN_RECORD_DATE = "RECORD_DATE";
    public static final String COLUMN_RECORD_TIME = "RECORD_TIME";
    public static final String COLUMN_RECORD_DESCRIPTION = "RECORD_DESCRIPTION";

    //----------------------------------USER-----------------------------------------//
    public static final String USER_TABLE = "USER_TABLE";
    public static final String COLUMN_USER_ID = "USER_ID";
    public static final String COLUMN_USER_NAME = "USER_NAME";
    public static final String COLUMN_USER_MOBILE_NO = "USER_MOBILE";
    public static final String COLUMN_USER_PIN = "USER_PIN";
    public static final String COLUMN_USER_EMAIL = "USER_EMAIL";

    public DatabaseSupport(@Nullable Context context, @Nullable String name) {
        super(context, "msomali" +
                ".db",null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String command1 = "CREATE TABLE "+USER_TABLE+" ("+COLUMN_USER_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+ COLUMN_USER_NAME+" TEXT,"+COLUMN_USER_EMAIL+" TEXT,"+COLUMN_USER_MOBILE_NO+" TEXT,"+COLUMN_USER_PIN+" TEXT)";
        String command2 = "CREATE TABLE "+RECORDS_TABLE+" ("+COLUMN_RECORD_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+ COLUMN_RECORD_NAME+" TEXT,"+COLUMN_RECORD_EMAIL+" TEXT,"+COLUMN_RECORD_MOBILE_NO+" TEXT,"+COLUMN_RECORD_AMOUNT+" REAL,"+COLUMN_RECORD_TRANSACTION_TYPE+" TEXT,"+COLUMN_RECORD_DESCRIPTION+" TEXT,"+COLUMN_RECORD_DATE+" TEXT,"+COLUMN_RECORD_TIME+" TEXT)";
        db.execSQL(command1);
        db.execSQL(command2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    //-----------------------------RECORDS OPERATIONS--------------------------------------//
    public boolean addRecord(Record record){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_RECORD_NAME,record.getFullName());
        cv.put(COLUMN_RECORD_EMAIL,record.getEmail());
        cv.put(COLUMN_RECORD_MOBILE_NO,record.getMobileNo());
        cv.put(COLUMN_RECORD_AMOUNT,record.getAmount());
        cv.put(COLUMN_RECORD_DESCRIPTION,record.getDescription());
        cv.put(COLUMN_RECORD_TRANSACTION_TYPE,record.getDescription());
        cv.put(COLUMN_RECORD_DATE,record.getDate());
        cv.put(COLUMN_RECORD_TIME,record.getTime());

        long insert = db.insert(RECORDS_TABLE,null,cv);
        if(insert==-1){return false;}else {return true;}
    }

    public boolean deleteRecord(Record record){
        SQLiteDatabase db = this.getWritableDatabase();
        String command = "DELETE FROM "+RECORDS_TABLE+" WHERE "+COLUMN_RECORD_ID+" = "+record.getId();
        Cursor cursor = db.rawQuery(command, null);
        if (cursor.moveToFirst()){return true;}else {return false;}
    }

    public List<Record> getRecords(){
        List<Record> records = new ArrayList<>();
        String command = "SELECT * FROM "+RECORDS_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(command, null);
        if (cursor.moveToFirst()){
            do {
                int recordId = cursor.getInt(0);
                String recordName = cursor.getString(1);
                String recordEmail = cursor.getString(2);
                String recordMobile = cursor.getString(3);
                double recordAmount = cursor.getDouble(4);
                String recordType = cursor.getString(5);
                String recordDescription = cursor.getString(6);
                String recordDate = cursor.getString(7);
                String recordTime = cursor.getString(8);
                Record record = new Record(recordId,recordName,recordAmount,recordMobile,recordEmail,recordDate,recordTime,recordType,recordDescription);
                records.add(record);
            }while (cursor.moveToNext());

        }
        cursor.close();
        db.close();
        return records;
    }

    //-----------------------------USER OPERATIONS--------------------------------------//
    public boolean addUser(UserRecords userRecords){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_USER_NAME,userRecords.getFullName());
        cv.put(COLUMN_USER_EMAIL,userRecords.getEmail());
        cv.put(COLUMN_USER_MOBILE_NO,userRecords.getMobileNumber());
        cv.put(COLUMN_USER_PIN,userRecords.getUserPin());

        long insert = db.insert(USER_TABLE,null,cv);
        if(insert==-1){return false;}else {return true;}
    }

    public UserRecords getUser(){
        UserRecords userRecords = new UserRecords(0,null,null,null,null);

        String command = "SELECT * FROM "+USER_TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(command, null);

        if (cursor.moveToFirst()){
                int userId = cursor.getInt(0);
                String userName = cursor.getString(1);
                String userEmail = cursor.getString(2);
                String userMobile = cursor.getString(3);
                String userPin = cursor.getString(4);

            userRecords = new UserRecords(userId,userName,userEmail,userMobile,userPin);
        }
        cursor.close();
        db.close();
        return userRecords;
    }

    public void onUserUpdate(UserRecords userRecords){
        SQLiteDatabase db = this.getWritableDatabase();

    }
}
