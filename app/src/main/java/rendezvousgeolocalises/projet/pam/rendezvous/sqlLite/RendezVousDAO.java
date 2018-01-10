package rendezvousgeolocalises.projet.pam.rendezvous.sqlLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.DateFormat;
import android.icu.text.SimpleDateFormat;
import android.icu.util.TimeZone;
import android.os.Build;
import android.support.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rendezvousgeolocalises.projet.pam.rendezvous.model.MyLocation;
import rendezvousgeolocalises.projet.pam.rendezvous.model.RendezVous;
import rendezvousgeolocalises.projet.pam.rendezvous.utils.StatusLevel;

public class RendezVousDAO {
    private SQLiteDatabase db;
    private Context context;
    private MySQLiteHelper mySQLiteHelper;
    private MyLocationDAO myLocationDAO;
    private RDVStatusDAO rdvStatusDAO;

    public static final String TABLE_EVENTS = "rendezVous";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ID_LOCATION = "id_location";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DATE = "date";
    private static final String[] ALL_COLUMNS = new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_DATE, COLUMN_ID_LOCATION};

    public static final String DATABASE_CREATE_EVENT = "create table if not exists "
            + TABLE_EVENTS + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text,"
            + COLUMN_DATE + " date,"
            + COLUMN_ID_LOCATION + " integer);";


    public RendezVousDAO(Context _context) {
        context = _context;
        mySQLiteHelper = MySQLiteHelper.getInstance(context);
        myLocationDAO  = new MyLocationDAO(context);
        rdvStatusDAO = new RDVStatusDAO(context);
    }

    //on ouvre la table en lecture/écriture
    public void open() {db = mySQLiteHelper.getWritableDatabase();}

    //on ferme l'accès à la BDD
    public void close() { db.close();}

    public long add(RendezVous rendezVous){
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID_LOCATION, myLocationDAO.add(rendezVous.getLocation()));
        contentValues.put(COLUMN_NAME, rendezVous.getName());
        contentValues.put(COLUMN_DATE, rendezVous.getDate().toString());
            open();
        long res = db.insert(TABLE_EVENTS,null,contentValues);
        close();

        rdvStatusDAO.addRendezVous(rendezVous, res);

        return res;
    }

    public ArrayList<String> getAllRendezVous(){
        ArrayList<String> allRendezVous = new ArrayList<>();
        open();
        Cursor cursor = db.query(TABLE_EVENTS, ALL_COLUMNS, null, null, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    allRendezVous.add(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                } while (cursor.moveToNext());

            }
            cursor.close();
        }
        close();
        return allRendezVous;
    }

    public List<List<String>> getAllFormatedRendezVous(){
        List<List<String>> res = new ArrayList<>();
        open();
        Cursor cursor = db.query(TABLE_EVENTS, ALL_COLUMNS, null, null, null, null, COLUMN_DATE);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                    String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                    String id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID)) + "";
                    res.add(new ArrayList<>(Arrays.asList(name, date, id)));
                } while (cursor.moveToNext());

            }
            cursor.close();
        }
        close();
        return res;
    }

    public List<List<String>> getAllFormatedRendezVousAccepted(){
        open();
        Cursor cursor = db.query(TABLE_EVENTS, ALL_COLUMNS, COLUMN_ID +" IN (SELECT " + RDVStatusDAO.COLUMN_ID_RDV +
                        " FROM " + RDVStatusDAO.TABLE_RDV_STATUS +
                        " WHERE " + RDVStatusDAO.COLUMN_STATUS + " = " + StatusLevel.ACCEPTED + " OR " + RDVStatusDAO.COLUMN_STATUS + " = " + StatusLevel.CREATOR +")"
                , null, null, null, COLUMN_DATE);
        List<List<String>> res = formatCursor(cursor);

        close();
        return res;
    }

    public List<List<String>> getAllFormatedRendezVousNotAccepted(){
        open();
        Cursor cursor = db.query(TABLE_EVENTS, ALL_COLUMNS, COLUMN_ID +" IN (SELECT " + RDVStatusDAO.COLUMN_ID_RDV +
                        " FROM " + RDVStatusDAO.TABLE_RDV_STATUS +
                        " WHERE " + RDVStatusDAO.COLUMN_STATUS + " <> " + StatusLevel.ACCEPTED + " AND " + RDVStatusDAO.COLUMN_STATUS + " <> " + StatusLevel.CREATOR +")"
                , null, null, null, COLUMN_DATE);
        List<List<String>> res = formatCursor(cursor);

        close();
        return res;
    }

    public List<List<String>> formatCursor(Cursor cursor){
        List<List<String>> res = new ArrayList<>();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                    String date = cursor.getString(cursor.getColumnIndex(COLUMN_DATE));
                    String id = cursor.getInt(cursor.getColumnIndex(COLUMN_ID)) + "";
                    res.add(new ArrayList<>(Arrays.asList(name, date, id, StatusLevel.ACCEPTED+"")));
                } while (cursor.moveToNext());

            }
            cursor.close();
        }
        return res;
    }

    public RendezVous getRendezVousById(int id) throws IOException {
        return getRendezVousById(id + "");
    }

    public RendezVous getRendezVousById(String id) throws IOException {
        open();
        Cursor cursor = db.query(TABLE_EVENTS, ALL_COLUMNS, COLUMN_ID + " = '" + id + "'", null, null, null, COLUMN_DATE);
        return cursorToRendezVous(cursor);
    }

    private RendezVous cursorToRendezVous(Cursor c) throws IOException {
        RendezVous rendezVous = null;
        if (!c.moveToFirst())
            return null;

        //Sinon on se place sur le premier élément
        LatLng latLng = myLocationDAO.getLatLngFromId(c.getInt(c.getColumnIndex(COLUMN_ID_LOCATION)));
        String name = c.getString(c.getColumnIndex(COLUMN_NAME));
        String dateTime = c.getString(c.getColumnIndex(COLUMN_DATE));
        Date date = parseDate(dateTime);
        rendezVous = new RendezVous(context, name, date , null, latLng);
        //On ferme le cursor
        c.close();

        close();
        //On retourne le livre
        return rendezVous;
    }

    private Date parseDate(String dateTime){
        Date date = null;
        SimpleDateFormat sdf = new SimpleDateFormat("EE MMM dd HH:mm:ss z yyyy", Locale.US);
        sdf.setTimeZone(TimeZone.GMT_ZONE);
        try {
            date = sdf.parse(dateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static int getIdLocationFromIdRDV(SQLiteDatabase db, int idRDV){
        int id_location = 0;
        Cursor c = db.rawQuery("SELECT * FROM " + RendezVousDAO.TABLE_EVENTS + "  WHERE " + RendezVousDAO.COLUMN_ID + "='" + idRDV + "'", null);
        if (c.moveToFirst()) {
            id_location = c.getInt(c.getColumnIndex(RendezVousDAO.COLUMN_ID_LOCATION));
        }
        c.close();
        return id_location;
    }

    public static void remove(SQLiteDatabase db, int id){
        int id_location = getIdLocationFromIdRDV(db, id);
        db.execSQL("DELETE FROM " + MyLocationDAO.TABLE_LOCATION + " WHERE " + MyLocationDAO.COLUMN_ID + " = '" + id_location + "'");
        db.execSQL("DELETE FROM " + RDVStatusDAO.TABLE_RDV_STATUS + " WHERE " + RDVStatusDAO.COLUMN_ID_RDV + " = '" + id + "'");
        db.execSQL("DELETE FROM " + RendezVousDAO.TABLE_EVENTS + " WHERE " + RendezVousDAO.COLUMN_ID + " = '" + id + "'");
    }
}
