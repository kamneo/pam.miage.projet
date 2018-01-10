package rendezvousgeolocalises.projet.pam.rendezvous.sqlLite;


import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.RequiresApi;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {


    private static final String DATABASE_NAME = "rendezVousGeolocalises.db";
    private static final int DATABASE_VERSION = 3;
    private static MySQLiteHelper sInstance;

    public static synchronized MySQLiteHelper getInstance(Context context) {
        if (sInstance == null) { sInstance = new MySQLiteHelper(context); }
        return sInstance;
    }

    private MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(MyLocationDAO.DATABASE_CREATE_LOCATION);
        database.execSQL(RendezVousDAO.DATABASE_CREATE_EVENT);
        database.execSQL(AccountDAO.DATABASE_CREATE_ACCOUNT);
        database.execSQL(RDVStatusDAO.DATABASE_CREATE_RDV_STATUS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + RDVStatusDAO.TABLE_RDV_STATUS);
        db.execSQL("DROP TABLE IF EXISTS " + MyLocationDAO.TABLE_LOCATION);
        db.execSQL("DROP TABLE IF EXISTS " + AccountDAO.TABLE_ACCOUNT);
        db.execSQL("DROP TABLE IF EXISTS " + RendezVousDAO.TABLE_EVENTS);
        onCreate(db);
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        SQLiteDatabase db = super.getWritableDatabase();
        cleanTables(db);
        return db;
    }

    public static void cleanTables(SQLiteDatabase db) {
        Cursor c = db.rawQuery("SELECT * FROM " + RendezVousDAO.TABLE_EVENTS + "  WHERE " + RendezVousDAO.COLUMN_DATE + "< strftime('%Y-%m-%d','now');" , null);
        if (c.moveToFirst()){
            do {
                int id = c.getInt(c.getColumnIndex(RendezVousDAO.COLUMN_ID));
                int id_location = c.getInt(c.getColumnIndex(RendezVousDAO.COLUMN_ID_LOCATION));
                RendezVousDAO.remove(db, id);
            } while(c.moveToNext());
        }
        c.close();
    }
}