package rendezvousgeolocalises.projet.pam.rendezvous.model;

import android.content.SharedPreferences;

public class Account {
    private String name;
    private String firstName;
    private String phoneNumber;
    private String password;

    public Account(String name, String firstName, String phoneNumber, String password) {
        this.name = name;
        this.firstName = firstName;
        this.phoneNumber = phoneNumber;
        this.password = password;
    }

    public Account(String firstName, String name, String phoneNumber) {
        this(name, firstName, phoneNumber, null);
    }

    public Account(){
        this(null, null, null, null);
    }

    public String getName() {
        return (name != null)?name.toUpperCase():"";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return (firstName!=null)?firstName.substring(0,1).toUpperCase() + firstName.substring(1).toLowerCase():"";
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void load(SharedPreferences sharedPreferences){
        name = sharedPreferences.getString("name", null);
        firstName = sharedPreferences.getString("fname", null);
        phoneNumber = sharedPreferences.getString("phone", null);
    }

    public void store(SharedPreferences sharedPreferences){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", name);
        editor.putString("fname", firstName);
        editor.putString("phone", phoneNumber);
        editor.commit();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof Account))
            return false;
        Account a = (Account) obj;
        return name.equals(a.getName()) && firstName.equals(a.getFirstName()) && phoneNumber.equals(a.getPhoneNumber());
    }

    public boolean isNull() {
        return phoneNumber == null || name == null || firstName == null;
    }
}
