package rendezvousgeolocalises.projet.pam.rendezvous.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import rendezvousgeolocalises.projet.pam.rendezvous.R;
import rendezvousgeolocalises.projet.pam.rendezvous.model.Account;
import rendezvousgeolocalises.projet.pam.rendezvous.sqlLite.AccountDAO;

public class RegisterActivity extends Activity {
    private AccountDAO accountDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        accountDAO = new AccountDAO(this);
    }

    public void baseIt(View view){
        EditText phoneNumber = (EditText)findViewById(R.id.phoneNumber);
        EditText password = (EditText)findViewById(R.id.password);
        EditText name = (EditText)findViewById(R.id.name);
        EditText firstName = (EditText)findViewById(R.id.firstname);
        long flag;

        Account account = new Account(name.getText().toString(), firstName.getText().toString(), phoneNumber.getText().toString(), password.getText().toString());

        accountDAO.open();
        flag = accountDAO.add(account);

        if(flag != -1){
            SharedPreferences sharedPreferences = getSharedPreferences("LOG_PREF", MODE_PRIVATE);
            account.store(sharedPreferences);
            startActivity(new Intent(this, MainActivity.class));
        }else{
            Toast toast = Toast.makeText(this, "Unable to save you account\nYou probably alreday have an account", Toast.LENGTH_LONG);
            toast.show();
        }
        accountDAO.close();
    }

}

