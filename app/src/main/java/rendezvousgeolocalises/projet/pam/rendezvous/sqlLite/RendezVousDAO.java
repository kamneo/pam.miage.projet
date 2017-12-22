package rendezvousgeolocalises.projet.pam.rendezvous.sqlLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import rendezvousgeolocalises.projet.pam.rendezvous.model.MyLocation;
import rendezvousgeolocalises.projet.pam.rendezvous.model.RendezVous;

public class RendezVousDAO {
    private SQLiteDatabase db;
    private MySQLiteHelper mySQLiteHelper;
    private MyLocationDAO myLocationDAO;
    private RDVStatusDAO rdvStatusDAO;

    public static final String TABLE_EVENTS = "rendezVous";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_ID_LOCATION = "id_location";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DATE = "date";

    public static final String DATABASE_CREATE_EVENT = "create table "
            + TABLE_EVENTS + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text,"
            + COLUMN_DATE + "date,"
            + COLUMN_ID_LOCATION + "integer);";


    public RendezVousDAO(Context context) {
        myLocationDAO  = new MyLocationDAO(context);
        rdvStatusDAO = new RDVStatusDAO(context);
        mySQLiteHelper = MySQLiteHelper.getInstance(context);
    }

    //on ouvre la table en lecture/écriture
    public void open() {db = mySQLiteHelper.getWritableDatabase();}

    //on ferme l'accès à la BDD
    public void close() { db.close();}

    public long add(RendezVous rendezVous){
        open();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID_LOCATION, myLocationDAO.add(rendezVous.getLocation()));
        contentValues.put(COLUMN_NAME, rendezVous.getName());
        contentValues.put(COLUMN_DATE, rendezVous.getDate().toString());
        long res = db.insert(TABLE_EVENTS,null,contentValues);
        close();

        rdvStatusDAO.addRendezVous(rendezVous, res);

        return res;
    }

}
