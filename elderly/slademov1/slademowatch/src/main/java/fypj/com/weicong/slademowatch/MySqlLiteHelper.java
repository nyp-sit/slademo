package fypj.com.weicong.slademowatch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

/**
 * Created by L30911 on 12/17/2015.
 */
public class MySqlLiteHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "offline_action_store";

    private static final String TABLE_NAME = "actions";
    private static final String COL_ID = "id";
    private static final String COL_PATH = "path";
    private static final String COL_ARGS = "arguments";
    private static final String COL_STORED = "stored";


    public MySqlLiteHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DB = "CREATE TABLE "+TABLE_NAME+" (" +
                COL_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_PATH+" TEXT, " +
                COL_ARGS+" TEXT, " +
                COL_STORED+" INTEGER DEFAULT 0" +
                ")";
        db.execSQL(CREATE_DB);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS actions");
        this.onCreate(db);
    }

    public void putEntry(String path, String message){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(COL_ARGS, message);
        cv.put(COL_PATH, path);
        db.insert(TABLE_NAME, null, cv);
        db.close();
    }
    public ArrayList<UpdateDbRequestEntity> fetchEntry(){
        SQLiteDatabase db = this.getWritableDatabase();
        ArrayList<UpdateDbRequestEntity> list = new ArrayList<UpdateDbRequestEntity>();

        Cursor cs = db.query("actions", new String[]{COL_ID, COL_PATH, COL_ARGS,COL_STORED}, COL_STORED+" = ?", new String[] {"0"}, null , null, null, null);
        if(cs.moveToFirst()){

            do{
                UpdateDbRequestEntity entity = new UpdateDbRequestEntity();
                entity.setId(cs.getInt(cs.getColumnIndex(COL_ID)));
                entity.setPath(cs.getString(cs.getColumnIndex(COL_PATH)));
                entity.setStored(cs.getInt(cs.getColumnIndex(COL_STORED)) == 0 ? false : true);
                entity.setArgs(cs.getString(cs.getColumnIndex(COL_ARGS)));
                list.add(entity);
            }while(cs.moveToNext());
        }
        /*
        ContentValues cv = new ContentValues();
        cv.put(COL_STORED, 1);
        db.update(TABLE_NAME, cv, COL_STORED + " = ? ", new String[]{"0"});

        db.close();*/
        db.delete(TABLE_NAME, //table name
                COL_STORED+" = ?",  // selections
                new String[] { "0" }); //selections args

        db.close();
        return list;
    }



}
