package com.example.cllobet.ndklogin.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "db";

    // Login table nombre
    private static final String TABLE_LOGIN = "users";

    // Login Table Columns nombres
    private static final String KEY_USERNAME = "userName";
    private static final String KEY_PASSWORD = "password";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
//        String CREATE_LOGIN_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_LOGIN + "("
//                + KEY_USERNAME + " TEXT UNIQUE PRIMARY KEY,"
//                + KEY_PASSWORD + " TEXT NOT NULL" + ")";
//        db.execSQL(CREATE_LOGIN_TABLE);
        createDB();
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        // Drop older table if existed
//        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGIN);
//
//        // Create tables again
//        onCreate(db);
        dropDB();
    }

    /**
     * Storing user details in database
     * */
    public boolean addUser(String userName, String password) {
//        SQLiteDatabase db = this.getWritableDatabase();
//
//        //Si existeix, retorna fals, i no es pot afegir
        if (SignIn(userName) != null) return false;

        Log.d("addUser", "Afegint usuari ha tornat null");
//
//        if ((userName.equals("")) || (password.toString().equals(""))) return false;
//
//
//        ContentValues values = new ContentValues();
//        values.put(KEY_USERNAME, userName); // userName
//        values.put(KEY_PASSWORD, password); // Password
//
//        // Inserting Row
//        db.insert(TABLE_LOGIN, null, values);
//        db.close(); // Closing database connection
//        return true;
        return addUserDB(userName,password);
    }

    public String SignIn(String user) {
        //if(!CheckExist("admin")) addUser("admin","4dm1n");
//        SQLiteDatabase db = this.getReadableDatabase();
//        String[] columns = {KEY_PASSWORD};
//        String[] where = {user};
//        Cursor c = db.query(
//                TABLE_LOGIN,
//                columns,
//                "userName = ?",
//                where,
//                null,
//                null,
//                null
//        );
//        if (c.moveToFirst()) {
//            db.close();
//            return c.getString(0);
//        }
//        db.close();
//        return "";
        return signInDB(user);
    }

    /**
     * Re crate database
     * Delete all tables and create them again
     * */
    public void resetTables() {
//        SQLiteDatabase db = this.getWritableDatabase();
//        // Delete All Rows
//        db.delete(TABLE_LOGIN, null, null);
//        db.close();
        dropDB();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native boolean createDB();
    public native boolean dropDB();
    public native boolean addUserDB(String userName, String password);
    public native String signInDB(String user);
}
