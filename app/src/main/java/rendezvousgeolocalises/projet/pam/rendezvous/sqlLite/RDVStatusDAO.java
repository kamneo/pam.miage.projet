package rendezvousgeolocalises.projet.pam.rendezvous.sqlLite;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;

import rendezvousgeolocalises.projet.pam.rendezvous.model.RendezVous;
import rendezvousgeolocalises.projet.pam.rendezvous.utils.StatusLevel;

public class RDVStatusDAO {
    private SQLiteDatabase db;
    private MySQLiteHelper mySQLiteHelper;
    private AccountDAO accountDAO;
    private String phoneOfLoggedUser;

    public static final String TABLE_RDV_STATUS = "rdvStatus";
    public static final String COLUMN_ID_RDV = "id_RDV";
    public static final String COLUMN_ID_ACCOUNT = "id_account";
    public static final String COLUMN_STATUS = "status";

    public static final String DATABASE_CREATE_RDV_STATUS = "create table if not exists "
            + TABLE_RDV_STATUS + "("
            + COLUMN_ID_RDV + " integer primary key autoincrement, "
            + COLUMN_ID_ACCOUNT + " text,"
            + COLUMN_STATUS + " text);";

    public RDVStatusDAO(Context context){
        mySQLiteHelper = MySQLiteHelper.getInstance(context);
        accountDAO = new AccountDAO(context);
        SharedPreferences sharedPreferences = context.getSharedPreferences("LOG_PREF", context.MODE_PRIVATE);
        phoneOfLoggedUser = sharedPreferences.getString("phone", null);
    }

    //on ouvre la table en lecture/écriture
    public void open() {db = mySQLiteHelper.getWritableDatabase();}

    //on ferme l'accès à la BDD
    public void close() { db.close();}

    public void addRendezVous(RendezVous rendezVous, long idRDV) {
        open();

        // On push le createur
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_ID_RDV, idRDV);
        contentValues.put(COLUMN_ID_ACCOUNT, accountDAO.getIdByPhoneNumber(phoneOfLoggedUser));
        contentValues.put(COLUMN_STATUS, StatusLevel.CREATOR);
        db.insert(TABLE_RDV_STATUS,null,contentValues);

        // On push les contacts
        for(String phoneNumber: rendezVous.getContacts()) {
            contentValues = new ContentValues();
            contentValues.put(COLUMN_ID_RDV, idRDV);
            contentValues.put(COLUMN_ID_ACCOUNT, accountDAO.getIdByPhoneNumber(phoneNumber));
            contentValues.put(COLUMN_STATUS, StatusLevel.WAITING_FOR_VALIDATION);
            db.insert(TABLE_RDV_STATUS,null,contentValues);
        }
        close();
    }
}
