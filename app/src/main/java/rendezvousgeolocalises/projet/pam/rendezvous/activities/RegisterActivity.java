package rendezvousgeolocalises.projet.pam.rendezvous.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import rendezvousgeolocalises.projet.pam.rendezvous.R;
import rendezvousgeolocalises.projet.pam.rendezvous.model.Account;
import rendezvousgeolocalises.projet.pam.rendezvous.persistance.AccountDAO;

public class RegisterActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    public void baseIt(View view){
        EditText phoneNumber = (EditText)findViewById(R.id.phoneNumber);
        EditText password = (EditText)findViewById(R.id.password);
        EditText name = (EditText)findViewById(R.id.name);
        EditText firstName = (EditText)findViewById(R.id.firstname);

        Account account = new Account(name.getText().toString(), firstName.getText().toString(), phoneNumber.getText().toString(), password.getText().toString());

        try {
            AccountDAO.storeAccount(this, account);
        } catch (IOException | ClassNotFoundException e) {
            Toast toast = Toast.makeText(this, "Unable to save you account\nYou probably alreday have an account", Toast.LENGTH_LONG);
            toast.show();
        }

            SharedPreferences sharedPreferences = getSharedPreferences("LOG_PREF", MODE_PRIVATE);
            account.store(sharedPreferences);
            startActivity(new Intent(this, MainActivity.class));
    }

}

