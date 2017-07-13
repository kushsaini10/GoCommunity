package com.example.kush.gocommunity;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


import java.util.ArrayList;

/**
 * Created by saini on 13-Aug-16.
 */
public class GODBOpenHelper extends SQLiteOpenHelper {
    public static final String GO_TABLE = "GoLikesTable";
    public static final String POKESTOP = "pokestop";
    public static final String GYM = "gym";
    public static final String GO_TABLE_ID = "_ID";
    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "longitude";
    public static final String LIKED = "liked";
    public static final String DISLIKED = "disliked";

    public static final String GO_P_TABLE = "GoParentTable";
    public static final String PARENT = "parent";

    public GODBOpenHelper(Context context) {
        super(context, "GODB", null, 1);

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + GO_TABLE + " ( " + GO_TABLE_ID +" TEXT PRIMARY KEY ,"+ POKESTOP + " INTEGER ,"+ GYM + " INTEGER ," + LATITUDE +
                " DOUBLE ,"+ LONGITUDE + " DOUBLE ,"+ LIKED + " INTEGER ,"+ DISLIKED + " INTEGER);");
        db.execSQL("create table " + GO_P_TABLE + " ( "+ LATITUDE + " DOUBLE ,"+ LONGITUDE +
                " DOUBLE ," + PARENT + " INTEGER); ");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
    public ArrayList<Cursor> getData(String Query){
        //get writable database
        SQLiteDatabase sqlDB = this.getWritableDatabase();
        String[] columns = new String[] { "mesage" };
        //an array list of cursor to save two cursors one has results from the query
        //other cursor stores error message if any errors are triggered
        ArrayList<Cursor> alc = new ArrayList<Cursor>(2);
        MatrixCursor Cursor2= new MatrixCursor(columns);
        alc.add(null);
        alc.add(null);


        try{
            String maxQuery = Query ;
            //execute the query results will be save in Cursor c
            Cursor c = sqlDB.rawQuery(maxQuery, null);


            //add value to cursor2
            Cursor2.addRow(new Object[] { "Success" });

            alc.set(1,Cursor2);
            if (null != c && c.getCount() > 0) {


                alc.set(0,c);
                c.moveToFirst();

                return alc ;
            }
            return alc;
        } catch(SQLException sqlEx){
            Log.d("printing exception", sqlEx.getMessage());
            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+sqlEx.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        } catch(Exception ex){

            Log.d("printing exception", ex.getMessage());

            //if any exceptions are triggered save the error message to cursor an return the arraylist
            Cursor2.addRow(new Object[] { ""+ex.getMessage() });
            alc.set(1,Cursor2);
            return alc;
        }


    }
}
