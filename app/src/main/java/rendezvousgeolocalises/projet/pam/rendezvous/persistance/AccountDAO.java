package rendezvousgeolocalises.projet.pam.rendezvous.persistance;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import rendezvousgeolocalises.projet.pam.rendezvous.model.Account;

public class AccountDAO {
    public static ArrayList<Account> getAllAccount(Context context) throws IOException, ClassNotFoundException {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_prefs",Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String accountsJson = sharedPreferences.getString("accounts", null);
        if(accountsJson == null || accountsJson.length() == 0)
            return new ArrayList<>();
        Account[] rendezVousArray = gson.fromJson(accountsJson, Account[].class);
        return  new ArrayList<>(Arrays.asList(rendezVousArray));
    }

    public static void storeAccounts(Context context, ArrayList<Account> accounts) throws IOException, ClassNotFoundException {
        Gson gson = new Gson();
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_prefs",Context.MODE_PRIVATE);
        String accountsJson = gson.toJson(accounts);
        sharedPreferences
                .edit()
                .putString("accounts",accountsJson)
                .apply();
    }

    public static void storeAccount(Context context, Account account) throws IOException, ClassNotFoundException {
        ArrayList<Account> accounts = getAllAccount(context);
        for (Account a : accounts) {
            if (a.equals(account))
                return;
        }
        accounts.add(account);
        storeAccounts(context, accounts);
    }

    public static Account getAccountWithPhone(Context context, String mPhone) throws IOException, ClassNotFoundException {
        ArrayList<Account> accounts = getAllAccount(context);
        for (Account a: accounts) {
            if(a.getPhoneNumber().equals(mPhone))
                return a;
        }
        return null;
    }
}
