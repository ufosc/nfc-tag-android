package org.ufosc.gatortag;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

// Not yet fully implemented.

public class SQLTagDataStore {
    public static final String DATABASE_NAME = "gatortagTagStore";
    public static final String KEY_SERIAL_NUMBER = "tagSerialNumber";
    public static final String KEY_SHORT_NAME = "tagName";
    public static final String KEY_UID = "tagUid";
    public static final String KEY_HASH = "tagHash";
    public static final String KEY_SCAN_DATE = "tagScanDate";
    public static final String KEY_CREATE_DATE = "tagCreateDate";
    public static final int DATABASE_VERSION = 2;

    private SQLiteDatabase tagBase;

    public SQLTagDataStore(Context appContext){
        TagDataStoreOpenHelper openHelper = new TagDataStoreOpenHelper(appContext);

        tagBase = openHelper.getWritableDatabase();
    }

    // public boolean putTag()

}
