package rendezvousgeolocalises.projet.pam.rendezvous.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rendezvousgeolocalises.projet.pam.rendezvous.R;
import rendezvousgeolocalises.projet.pam.rendezvous.model.Account;
import rendezvousgeolocalises.projet.pam.rendezvous.model.MyLocation;
import rendezvousgeolocalises.projet.pam.rendezvous.model.RendezVous;
import rendezvousgeolocalises.projet.pam.rendezvous.persistance.RendezVousDAO;
import rendezvousgeolocalises.projet.pam.rendezvous.utils.StatusLevel;

public class CreateEventActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Calendar myCalendar;
    private DatePickerDialog.OnDateSetListener date;
    private GoogleMap mMap;
    LocationManager locationManager;
    private int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private int CONTACT_PICK_CODE = 2;
    private String TAG = "bonjour";
    private MapView mapView;
    private LatLng lastPostion;
    private Marker marker;
    private boolean newEvent;
    private RendezVous oldRendezVous = null;
    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int selectedYear,
                              int selectedMonth, int selectedDay) {
            myCalendar.set(selectedYear, selectedMonth, selectedDay);
            updateLabel();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);

        newEvent = getIntent().getBooleanExtra("newEvent", true);

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        MapsInitializer.initialize(this);
        myCalendar = Calendar.getInstance();

        if(newEvent){
            locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
            lastPostion = getCurrentPosition();
            date = new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    myCalendar.set(Calendar.YEAR, year);
                    myCalendar.set(Calendar.MONTH, monthOfYear);
                    myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                }
            };
        }else{
            oldRendezVous = (RendezVous)getIntent().getSerializableExtra("rendezVous");
            findViewById(R.id.radioButtons).setVisibility(View.GONE); //on cache les radioBoutons
            findViewById(R.id.layout_contact).setVisibility(View.GONE); //on cache l'ajout de contact
            findViewById(R.id.create_eventName).setEnabled(false); //desactivation de l'input pour le titre
            findViewById(R.id.date_value).setClickable(false); //on cache l'ajout de contact

            ((EditText)findViewById(R.id.create_eventName)).setText(oldRendezVous.getName());
            myCalendar.setTime(oldRendezVous.getDate());
        }
        updateLabel();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if(newEvent) {
            getMenuInflater().inflate(R.menu.create_event, menu);
            return true;
        }else
            return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_create_event) {
            try {
                createEvent();
            } catch (ParseException | IOException e) {
                e.printStackTrace();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createEvent() throws ParseException, IOException {
        String myFormat = "dd/MM/yyyy";
        ArrayList<String> contacts = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.FRENCH);
        Date date = sdf.parse(((TextView) findViewById(R.id.date_value)).getText().toString());
        String eventName = ((TextView) findViewById(R.id.create_eventName)).getText().toString();
        String[] tab_contacts = ((TextView) findViewById(R.id.create_contacts)).getText().toString().split(";");

        for(String s : tab_contacts)
            contacts.add(s.trim());

        SharedPreferences sharedPreferences = getSharedPreferences("LOG_PREF", MODE_PRIVATE);
        Account logged = new Account();
        logged.load(sharedPreferences);
        if(logged.isNull()){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }

        RendezVous rendezVous = new RendezVous(this, eventName, date, contacts, marker.getPosition(), StatusLevel.CREATOR);
        for (String phoneNumber: contacts){
            sendInvitation(phoneNumber, rendezVous);
        }

        try {
            RendezVousDAO.storeRendezVous(this, rendezVous);
        } catch (ClassNotFoundException e) {
            Toast.makeText(this, "Une erreur s'est produite lors de la création de l'event", Toast.LENGTH_SHORT).show();
        }
        finish();
    }

    private void sendInvitation(String phoneNumber, RendezVous rendezVous) {
        if(phoneNumber.length() > 10 && phoneNumber.length()<4){
            Toast.makeText(this, "Numero incorect : " + phoneNumber, Toast.LENGTH_LONG).show();
            return;
        }
        SmsManager smsManager = SmsManager.getDefault();
        String message = getResources().getString(R.string.message_header) + rendezVous.sendInformations();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }

    public void setDate(View view) {
        new DatePickerDialog(CreateEventActivity.this,
                datePickerListener,
                myCalendar.get(Calendar.YEAR),
                myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yyyy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.FRENCH);

        ((TextView) findViewById(R.id.date_value)).setText(sdf.format(myCalendar.getTime()));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showAlert();
            return;
        }
        mMap.setMyLocationEnabled(true);
        // Updates the location and zoom of the MapView
        if (lastPostion != null && newEvent) {
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(lastPostion, 10);
            addMarker(lastPostion);
            mMap.animateCamera(cameraUpdate);
        }
        if(!newEvent){
            addMarker(new LatLng(oldRendezVous.getLocation().getLatitude(), oldRendezVous.getLocation().getLongitude()));
        }
    }

    @Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }


    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    public LatLng getCurrentPosition() {
        LatLng latLng = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            showAlert();
            return null;
        }

        Criteria criteria = new Criteria();
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        String provider = locationManager.getBestProvider(criteria, false);
        if(provider != null) {
            Location location = locationManager.getLastKnownLocation(provider);
            if(location != null){
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                latLng = new LatLng(lat, lng);
            }
        }
        if(latLng == null)
            Toast.makeText(this, "Error during your localisation", Toast.LENGTH_SHORT).show();

        return latLng;
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    }
                });
        dialog.show();
    }

    public void updateRadio(View view){
        RadioButton rb = (RadioButton) findViewById(R.id.radioButton);
        if(rb.isChecked()){
            ((TextView)findViewById(R.id.adresse)).setText(R.string.address_value_default);
            findViewById(R.id.adresse).setVisibility(View.GONE);
            lastPostion = getCurrentPosition();
            if(lastPostion != null)
                addMarker(lastPostion);
        }else{
            findViewById(R.id.adresse).setVisibility(View.VISIBLE);
            marker.remove();
        }
    }

    public void openIntent(View view){
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
            //PLACE_AUTOCOMPLETE_REQUEST_CODE is integer for request code
        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
            // TODO: Handle the error.
        }
    }

    private void addMarker(LatLng latLng){
        if(marker != null)
            marker.remove();
        marker = mMap.addMarker(new MarkerOptions()
                .position(latLng));

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 10);
        mMap.animateCamera(cameraUpdate);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //autocompleteFragment.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                addMarker(place.getLatLng());
                ((TextView)findViewById(R.id.adresse)).setText(place.getName());
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.i(TAG, status.getStatusMessage());
            } else if (resultCode == RESULT_CANCELED) {

            }
        } else if(requestCode == CONTACT_PICK_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                Uri contactURI = data.getData();
                String phone = retrieveContactNumber(contactURI);
                if(phone.length() > 0) {
                    EditText et = (EditText) findViewById(R.id.create_contacts);
                    et.setText((et.getText().length()>0)? et.getText() + "; " + phone : phone);
                    et.setSelection(et.getText().length());
                }
            }
        }
    }

    public void readContact(View view){
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, Uri.parse("content://contacts/people"));
            startActivityForResult(intent, CONTACT_PICK_CODE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String retrieveContactNumber(Uri uriContact) {
        String contactNumber = null;
        String contactID = "";

        // getting contacts ID
        Cursor cursorID = getContentResolver().query(uriContact,
                new String[]{ContactsContract.Contacts._ID},
                null, null, null);

        if (cursorID.moveToFirst()) {

            contactID = cursorID.getString(cursorID.getColumnIndex(ContactsContract.Contacts._ID));
        }

        cursorID.close();

        Log.d(TAG, "Contact ID: " + contactID);

        // Using the contact ID now we will get contact phone number
        Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
                        ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
                        ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

                new String[]{contactID},
                null);

        if (cursorPhone.moveToFirst()) {
            contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
        }

        cursorPhone.close();

       return contactNumber;
    }
}
