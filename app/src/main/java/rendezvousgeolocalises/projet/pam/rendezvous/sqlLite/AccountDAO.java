package rendezvousgeolocalises.projet.pam.rendezvous.sqlLite;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import rendezvousgeolocalises.projet.pam.rendezvous.model.Account;

public class AccountDAO {
    private SQLiteDatabase db;
    private MySQLiteHelper mySQLiteHelper;

    public static final String TABLE_ACCOUNT = "account";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_FIRST_NAME = "firstName";
    public static final String COLUMN_PHONE_NUMBER = "phoneNumber";
    public static final String COLUMN_PASSWORD = "password";

    public static final String DATABASE_CREATE_ACCOUNT = "create table "
            + TABLE_ACCOUNT + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_NAME + " text,"
            + COLUMN_FIRST_NAME + " text,"
            + COLUMN_PHONE_NUMBER + " text not null,"
            + COLUMN_PASSWORD + " text);";

    public AccountDAO(Context context) {
        mySQLiteHelper = MySQLiteHelper.getInstance(context);
    }

    //on ouvre la table en lecture/écriture
    public void open() {db = mySQLiteHelper.getWritableDatabase();}

    //on ferme l'accès à la BDD
    public void close() { db.close();}

    public long add(Account account) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_NAME, account.getName());
        contentValues.put(COLUMN_FIRST_NAME, account.getFirstName());
        contentValues.put(COLUMN_PHONE_NUMBER, account.getPhoneNumber());
        contentValues.put(COLUMN_PASSWORD, account.getPassword());

        return db.insert(TABLE_ACCOUNT,null,contentValues);
    }

    public long remove(String phoneNumber){
        return db.delete(TABLE_ACCOUNT, COLUMN_PHONE_NUMBER + "=?", new String[]{phoneNumber});
    }

    public long remove(Account account){
        return remove(account.getPhoneNumber());
    }

    public Account getAccountWithPhone(String phone){
        open();
        Cursor c = db.query(TABLE_ACCOUNT, new String[] {COLUMN_PHONE_NUMBER, COLUMN_PASSWORD, COLUMN_NAME, COLUMN_FIRST_NAME}, COLUMN_PHONE_NUMBER + " = \"" + phone +"\"", null, null, null, null);
        Account a = cursorToAccount(c);
        close();
        return a;
    }

    private Account cursorToAccount(Cursor c) {
        if (c.getCount() == 0)
            return null;
        c.moveToFirst();

        String name = c.getString(c.getColumnIndex(COLUMN_NAME));
        String phone = c.getString(c.getColumnIndex(COLUMN_PHONE_NUMBER));
        String first_name = c.getString(c.getColumnIndex(COLUMN_FIRST_NAME));
        String password = c.getString(c.getColumnIndex(COLUMN_PASSWORD));
        return new Account(name, first_name, phone, password);
    }

    public long getIdByPhoneNumber(String phoneNumber) {
        open();
        Cursor c = db.query(TABLE_ACCOUNT, new String[] {COLUMN_ID}, COLUMN_PHONE_NUMBER + " = \"" + phoneNumber +"\"", null, null, null, null);
        if (c.getCount() == 0)
            return -1;
        c.moveToFirst();
        long id = c.getLong(c.getColumnIndex(COLUMN_ID));
        close();
        return id;
    }
}
