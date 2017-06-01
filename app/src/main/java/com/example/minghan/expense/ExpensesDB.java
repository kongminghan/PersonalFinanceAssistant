package com.example.minghan.expense;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by MingHan on 3/16/2017.
 */

public class ExpensesDB extends SQLiteOpenHelper {

    public static final String dbName = "dbMyExpense";
    public static final String tblName = "expenses";
    public static final String colExpName = "exp_name";
    public static final String colExpPrice = "exp_price";
    public static final String colExpDate = "exp_date";
    public static final String colExpId = "exp_id";



    public ExpensesDB(Context context){
        super(context, dbName, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS expenses (exp_id VARCHAR, exp_name VARCHAR, exp_price VARCHAR, exp_date DATE);");
        db.execSQL("CREATE TABLE IF NOT EXISTS category (cat_id INTEGER, cat_name VARCHAR);");
    }

    public void updateData(String name, String date, String id, String price){
        SQLiteDatabase db = this.getReadableDatabase();
        db.execSQL("UPDATE expenses SET exp_name = '"+name+"', exp_price = '"+price+"', exp_date = '"+date+ "' where exp_id = "+id+";");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS expenses");
        onCreate(db);
    }

    public Cursor getDataById(int id){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery("Select * from"+tblName+ " where "+colExpId+ "= "+id, null);

        return cursor;
    }

    public Cursor getDataList(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery("Select * from expenses", null);
        return cursor;
    }

    public Cursor getCats(){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor =  db.rawQuery("Select * from category", null);
        return cursor;
    }

    public void addCat(String cat){
        int row = fnTotalCatRow();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("Select * from category where cat_name='"+cat+"'", null);
        if(!cursor.moveToFirst()){
            ContentValues contentValues = new ContentValues();
            contentValues.put("cat_id", row);
            contentValues.put("cat_name", cat);
            db.insert("category", null, contentValues);
        }
    }

    public void deleteCat(Category category){
        SQLiteDatabase db = this.getReadableDatabase();
        db.delete("category", "cat_id = ?", new String [] {category.getId()});
    }

    public void updateCat(Category category){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("cat_name", category.getCat_name());
        db.update("category", values, "cat_id = ? ", new String[]{category.getId()});
    }

    public void fnExecuteSql(String strSql, Context context){
        try{
            SQLiteDatabase db = this.getReadableDatabase();
            db.execSQL(strSql);
        } catch (Exception e){
            Log.d("unable to run query", "error");
        }
    }

    public int fnTotalRow(){
        int row;
        SQLiteDatabase db = this.getReadableDatabase();
        row = (int) DatabaseUtils.queryNumEntries(db, tblName);
        return row;
    }

    public int fnTotalCatRow(){
        int row;
        SQLiteDatabase db = this.getReadableDatabase();
        row = (int) DatabaseUtils.queryNumEntries(db, "category");
        return row;
    }
}
