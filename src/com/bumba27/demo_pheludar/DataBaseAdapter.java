package com.bumba27.demo_pheludar;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DataBaseAdapter {
	public static final String KEY_ROWID = "id";
    public static final String KEY_G_LAT = "glat";
    public static final String KEY_G_LNG = "glng";
    public static final String KEY_N_LAT = "nlat";
    public static final String KEY_N_LNG = "nlng";
    public static final String KEY_MB_CODE = "mb_code_perf";
    public static final String KEY_FORMATTED_DATE = "formattedDate";
    public static final String KEY_G_ADDRESS = "gAddress";
    public static final String KEY_N_ADDRESS = "nAddress";
    private static final String TAG = "DBAdapter";
    
    private static final String DATABASE_NAME = "market_boy_DB";
    private static final String DATABASE_TABLE = "market_boy_loc_tbl";
    private static final int DATABASE_VERSION = 1;
//	new MyAsyncTask().execute(""+glat, ""+glng, ""+nlat, ""+nlng, getFromPreference("mb_code_perf"), formattedDate, gAddress, nAddress);	

    private static final String DATABASE_CREATE =
        "create table if not exists "+DATABASE_TABLE+" (id integer primary key autoincrement, "
        + "glat VARCHAR, glng VARCHAR, nlat VARCHAR, nlng VARCHAR, mb_code_perf VARCHAR, formattedDate VARCHAR, gAddress VARCHAR, nAddress VARCHAR );";
        
    private final Context context;    

    private DatabaseHelper DBHelper;
    private SQLiteDatabase db;

    public DataBaseAdapter(Context ctx) 
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }
        
    private static class DatabaseHelper extends SQLiteOpenHelper 
    {
        DatabaseHelper(Context context) 
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) 
        {
        	try {
        		db.execSQL(DATABASE_CREATE);	
        	} catch (SQLException e) {
        		e.printStackTrace();
        	}
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS contacts");
            onCreate(db);
        }
    }    

    //---opens the database---
    public DataBaseAdapter open() throws SQLException 
    {
        db = DBHelper.getWritableDatabase();
        return this;
    }

    //---closes the database---    
    public void close() 
    {
        DBHelper.close();
    }
    
    //---insert a record into the database---
    public long insertRecord(String glat, String glng, String nlat, String nlng, String mb_code_perf, String formattedDate, String gAddress, String nAddress) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_G_LAT, glat);
        initialValues.put(KEY_G_LNG, glng);
        initialValues.put(KEY_N_LAT, nlat);
        initialValues.put(KEY_N_LNG, nlng);
        initialValues.put(KEY_MB_CODE, mb_code_perf);
        initialValues.put(KEY_FORMATTED_DATE, formattedDate);
        initialValues.put(KEY_G_ADDRESS, gAddress);
        initialValues.put(KEY_N_ADDRESS, nAddress);
        return db.insert(DATABASE_TABLE, null, initialValues);
    }

    //---deletes a particular record---
    public boolean deleteRecord(long rowId) 
    {
        return db.delete(DATABASE_TABLE, KEY_ROWID + ">" + rowId, null) > 0;
    }

    //---retrieves all the records---
    public Cursor getAllRecords() 
    {
        return db.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_G_LAT,
                KEY_G_LNG, KEY_N_LAT, KEY_N_LNG, KEY_MB_CODE, KEY_FORMATTED_DATE, KEY_G_ADDRESS, KEY_N_ADDRESS}, null, null, null, null, null);
    }

    //---retrieves a particular record---
    public Cursor getRecord(long rowId) throws SQLException 
    {
        Cursor mCursor =
                db.query(true, DATABASE_TABLE, new String[] {KEY_ROWID, KEY_G_LAT,
                		KEY_G_LNG, KEY_N_LAT, KEY_N_LNG, KEY_MB_CODE, KEY_FORMATTED_DATE, KEY_G_ADDRESS, KEY_N_ADDRESS}, 
                KEY_ROWID + "=" + rowId, null, null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

//    //---updates a record---
//    public boolean updateRecord(long rowId, String firstName, String lastName, String address) 
//    {
//        ContentValues args = new ContentValues();
//        args.put(KEY_G_LAT, firstName);
//        args.put(KEY_G_LNG, lastName);
//        args.put(KEY_N_LAT, address);
//        return db.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
//    }
}
