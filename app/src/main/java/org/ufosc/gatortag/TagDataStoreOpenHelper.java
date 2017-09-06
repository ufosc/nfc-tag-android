package org.ufosc.gatortag;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

// Not yet fully implemented.

public class TagDataStoreOpenHelper extends SQLiteOpenHelper{
    private static final String createCommand = "CREATE DATABASE "
            + SQLTagDataStore.DATABASE_NAME + " ( "
            + SQLTagDataStore.KEY_UID + " NUMBER, "
            + SQLTagDataStore.KEY_SERIAL_NUMBER + " TEXT, "
            + SQLTagDataStore.KEY_SHORT_NAME + " TEXT, "
            + SQLTagDataStore.KEY_CREATE_DATE + " NUMBER, "
            + SQLTagDataStore.KEY_SCAN_DATE + " NUMBER, "
            + SQLTagDataStore.KEY_HASH + " TEXT );";

    TagDataStoreOpenHelper(Context context){
        super(context, SQLTagDataStore.DATABASE_NAME, null, SQLTagDataStore.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createCommand);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
