package rendezvousgeolocalises.projet.pam.rendezvous.sqlLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import rendezvousgeolocalises.projet.pam.rendezvous.model.Account;
import rendezvousgeolocalises.projet.pam.rendezvous.model.MyLocation;

/**
 * Created by kamneo on 08/12/2017.
 */

public class MyLocationDAO {
    private SQLiteDatabase db;
    private MySQLiteHelper mySQLiteHelper;

    public static final String TABLE_LOCATION = "location";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";
    public static final String COLUMN_ADDRESS = "address";
    public static final String COLUMN_CITY = "city";
    public static final String COLUMN_COUNTRY = "country";
    public static final String COLUMN_STATE = "state";
    public static final String COLUMN_POSTAL_CODE = "postalCode";

    public static final String DATABASE_CREATE_LOCATION = "create table "
            + TABLE_LOCATION + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_LATITUDE + " text,"
            + COLUMN_LONGITUDE + " text,"
            + COLUMN_ADDRESS + " text,"
            + COLUMN_CITY + " text,"
            + COLUMN_COUNTRY + " text,"
            + COLUMN_STATE + " text,"
            + COLUMN_POSTAL_CODE + " text);";

    public MyLocationDAO(Context context) {
        mySQLiteHelper = MySQLiteHelper.getInstance(context);
    }

    //on ouvre la table en lecture/écriture
    public void open() {db = mySQLiteHelper.getWritableDatabase();}

    //on ferme l'accès à la BDD
    public void close() { db.close();}

    /**
     * return the id of the row, -1 if something wrong happens
     */
    public long add(MyLocation myLocation){
        open();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_LATITUDE, myLocation.getLatitude());
        contentValues.put(COLUMN_LONGITUDE, myLocation.getLongitude());
        contentValues.put(COLUMN_ADDRESS, myLocation.getAddress());
        contentValues.put(COLUMN_CITY, myLocation.getCity());
        contentValues.put(COLUMN_COUNTRY, myLocation.getCountry());
        contentValues.put(COLUMN_STATE, myLocation.getState());
        contentValues.put(COLUMN_POSTAL_CODE, myLocation.getPostalCode());
        long res = db.insert(TABLE_LOCATION,null,contentValues);
        close();
        return res;
    }
}
