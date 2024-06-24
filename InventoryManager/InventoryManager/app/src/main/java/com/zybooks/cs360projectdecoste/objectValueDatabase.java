package com.zybooks.cs360projectdecoste;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

//Generic database object made of 1 table with 2 columns
public class objectValueDatabase extends SQLiteOpenHelper {

    private static final int VERSION = 1;

    //Generic object/value database
    public objectValueDatabase(Context context, String DATABASE_NAME) {//userDatabase
        super(context, DATABASE_NAME, null, VERSION);
    }

    //Generic table for holding objects with values
    public final class ObjectTable {
        public static final String TABLE = "objectValueTable";
        private static final String COL_OBJECT = "object";
        private static final String COL_VALUE = "value";
    }

    //Creates generic 2 column table on creation
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + ObjectTable.TABLE + " (" + ObjectTable.COL_OBJECT + " text, " +
                ObjectTable.COL_VALUE + " text)");
    }

    //Recreates table
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Recreates table
        db.execSQL("drop table if exists " + ObjectTable.TABLE);
        onCreate(db);
    }

    //Add a two column object to a table
    //C in CRUD
    public void createObject(String object, String value) {
        //Grabs database
        SQLiteDatabase db = getWritableDatabase();

        //Creates a store for the values
        ContentValues values = new ContentValues();

        //Adds object and value to database
        values.put(ObjectTable.COL_OBJECT, object);
        values.put(ObjectTable.COL_VALUE, value);
        db.insert(ObjectTable.TABLE, null, values);
    }

    //Safely reads object into database table
    //R is CRUD
    public Cursor readObject(String object){
        //Grabs database
        SQLiteDatabase db = getReadableDatabase();
        String sql = "select * from " + ObjectTable.TABLE + " where object = ?";
        //rawQuery used to prevent SQL injection
        Cursor cursor = db.rawQuery(sql, new String[] {object});
        return cursor;
    }

    //Updates object in database table
    //U in CRUD
    public void updateObject(String object, String value) {
        //Grabs database
        SQLiteDatabase db = getWritableDatabase();

        //Creates a store for the values
        ContentValues values = new ContentValues();

        values.put(ObjectTable.COL_VALUE, value);

        //Updates item in table
        db.update(ObjectTable.TABLE, values, "object = ?", new String[] {object});
    }

    //Safely deletes object from database table
    //D in CRUD
    public void deleteObject(String object, String value){
        //Grabs database
        SQLiteDatabase db = getReadableDatabase();
        String sql = "delete from " + ObjectTable.TABLE + " where object = ?";
        //rawQuery used to prevent SQL injection
        db.rawQuery(sql, new String[] {object});
    }

    //WARNING: DANGEROUS
    //Deletes contents of entire table in database
    public void deleteAll(){
        //Grabs database
        SQLiteDatabase db = getReadableDatabase();

        //Deletes table contents
        String sql = "delete from " + ObjectTable.TABLE;
        db.execSQL(sql);
    }

    //Returns count of objects in database
    public int getDBTableCount() {
        String sqlQuery = "SELECT  * FROM " + ObjectTable.TABLE;

        //Grabs database
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(sqlQuery, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    //Returns a cursor object that may be used to navigate the database
    public Cursor getDB(){
        //SQL query for grabbing every item in table
        String sqlQuery = "SELECT  * FROM " + ObjectTable.TABLE;

        //Grabs database
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery(sqlQuery, null);
        return cursor;
    }

    //CheckValueByObject
    public boolean checkValueByObject(String object, String value) {
        //Grabs database
        SQLiteDatabase db = getReadableDatabase();
        //SQL command
        String sql = "select * from " + ObjectTable.TABLE + " where object = ?";
        //rawQuery used to prevent SQL injection. Best practice for security
        Cursor cursor = db.rawQuery(sql, new String[]{object});
        //Checks if value is linked to the given object
        if (cursor.moveToFirst()) {
            do {
                //Gets value from database
                String valueCheck = cursor.getString(1);
                if(valueCheck.equals(value)) {
                    //Returns true if object/value pair are found in database
                    return true;
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        //Returns false if object/value pair are not found in database
        return false;
    }
}