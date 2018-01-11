package rendezvousgeolocalises.projet.pam.rendezvous.activities;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.MotionEvent;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewTreeObserver;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import rendezvousgeolocalises.projet.pam.rendezvous.model.Account;
import rendezvousgeolocalises.projet.pam.rendezvous.model.RendezVous;
import rendezvousgeolocalises.projet.pam.rendezvous.persistance.AccountDAO;
import rendezvousgeolocalises.projet.pam.rendezvous.persistance.RendezVousDAO;
import rendezvousgeolocalises.projet.pam.rendezvous.utils.CustomExpandableListAdapter;
import rendezvousgeolocalises.projet.pam.rendezvous.R;
import rendezvousgeolocalises.projet.pam.rendezvous.utils.SmsReceiver;
import rendezvousgeolocalises.projet.pam.rendezvous.utils.StatusLevel;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback {
    private List<String> expandableListTitle;
    private HashMap<String, List<List<String>>> expandableListDetail;
    private Account logged;
    private MapView mapView;
    private GoogleMap mMap;
    List<Marker> markers = new ArrayList<Marker>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        gpsIsActivated();
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), CreateEventActivity.class));
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mapView = (MapView) findViewById(R.id.main_maps);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        MapsInitializer.initialize(this);
        final View myView = findViewById(R.id.main_maps);
        myView.setTag(myView.getVisibility());
        myView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(myView.getVisibility() == View.VISIBLE){
                    setMarkers();
                    annimateMap();
                }
            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS}, 0);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET}, 1);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 3);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECEIVE_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.RECEIVE_SMS}, 4);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS}, 5);
        }
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_SMS}, 6);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        // premiere connexion ?
        SharedPreferences sharedPreferences = getSharedPreferences("LOG_PREF", MODE_PRIVATE);
        logged = new Account();
        logged.load(sharedPreferences);
        if(logged.isNull()){
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        }else {
            NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
            navigationView.setNavigationItemSelectedListener(this);
            ((TextView) navigationView.getHeaderView(0).findViewById(R.id.loggedName)).setText(logged.getName() + " " + logged.getFirstName());

            fillExpendedList();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void fillExpendedList() {
        prepareListData();
        CustomExpandableListAdapter listAdapter = new CustomExpandableListAdapter(this, expandableListTitle, expandableListDetail);
        ExpandableListView expListView = (ExpandableListView) findViewById(R.id.eventList);
        expListView.setAdapter(listAdapter);
        expListView.expandGroup(0);
        expListView.expandGroup(1);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_map) {
            if(item.getTitle().equals(getResources().getString(R.string.nav_maps))){
                item.setTitle(getResources().getString(R.string.nav_list));
                item.setIcon(getResources().getDrawable(R.drawable.ic_menu_list, null));
                findViewById(R.id.eventList).setVisibility(View.GONE);
                findViewById(R.id.main_maps).setVisibility(View.VISIBLE);
                setMarkers();
            }else{
                item.setTitle(getResources().getString(R.string.nav_maps));
                item.setIcon(getResources().getDrawable(R.drawable.ic_menu_map, null));
                findViewById(R.id.eventList).setVisibility(View.VISIBLE);
                findViewById(R.id.main_maps).setVisibility(View.GONE);
            }
        }else if (id == R.id.nav_deconexion) {
            SharedPreferences sharedPreferences = getSharedPreferences("LOG_PREF", MODE_PRIVATE);
            Account account = new Account();
            account.store(sharedPreferences);
            startActivity(new Intent(this, LoginActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return super.onOptionsItemSelected(item);
    }

    private void prepareListData() {
        expandableListTitle = new ArrayList<>();
        expandableListDetail = new HashMap<>();

        // Adding child data
        expandableListTitle.add("Rendez vous acceptés");
        expandableListTitle.add("Rendez vous non validés");

        // Adding child data
        List<List<String>> rdvs = new ArrayList<>();
        List<List<String>> nowShowing = new ArrayList<>();
        try {
            rdvs = RendezVousDAO.getAllFormatedRendezVousAccepted(this);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        try{
            nowShowing = RendezVousDAO.getAllFormatedRendezVousNotAccepted(this);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        expandableListDetail.put(expandableListTitle.get(0), rdvs); // Header, Child data
        expandableListDetail.put(expandableListTitle.get(1), nowShowing);
    }

    private void gpsIsActivated() {
        final LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();
        }
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        finish();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void showInMap(View view) throws IOException {
        String id = ((TextView) view.findViewById(R.id.idItem)).getText().toString();
        Serializable rendezVous = getRendezVousById(id);
        Intent intent = new Intent(getBaseContext(), CreateEventActivity.class);
        intent.putExtra("newEvent", false);
        intent.putExtra("rendezVous", rendezVous);
        startActivity(intent);
    }

    private RendezVous getRendezVousById(String id) throws IOException {
        try {
            return RendezVousDAO.getRendezVousById(this, id);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
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

        setMarkers();
    }

    private void setMarkers() {
        for(Marker m : markers)
            m.remove();

        markers = new ArrayList<>();
        if(expandableListDetail != null) {
            for (List<String> data : expandableListDetail.get(expandableListTitle.get(0))) {
                try {
                    if (data.get(2) != null && !data.get(2).equals("-1")) {
                        RendezVous rdv = getRendezVousById(data.get(2));
                        addMarker(new LatLng(rdv.getLocation().getLatitude(), rdv.getLocation().getLongitude()), rdv.getName(), rdv.getDate().toString(), BitmapDescriptorFactory.HUE_RED);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            for (List<String> data : expandableListDetail.get(expandableListTitle.get(1))) {
                try {
                    if (data.get(2) != null && !data.get(2).equals("-1")) {
                        RendezVous rdv = getRendezVousById(data.get(2));
                        addMarker(new LatLng(rdv.getLocation().getLatitude(), rdv.getLocation().getLongitude()), rdv.getName(), rdv.getDate().toString(), BitmapDescriptorFactory.HUE_ORANGE);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void annimateMap(){
        //animation
        if(markers.size() == 0)
            return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Marker marker : markers) {
            builder.include(marker.getPosition());
        }
        LatLngBounds bounds = builder.build();
        int padding = 0;
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
        mMap.animateCamera(cu);
    }

    private void showAlert() {
        final android.app.AlertDialog.Builder dialog = new android.app.AlertDialog.Builder(this);
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

    private void addMarker(LatLng latLng, String title, String snippet, float color){
         markers.add(mMap.addMarker(new MarkerOptions()
                 .position(latLng)
                 .title(title)
                 .snippet(snippet)
                 .icon(BitmapDescriptorFactory.defaultMarker(color))));
    }

    public void delete(View view) {
        String id =((TextView)((View)view.getParent().getParent()).findViewById(R.id.idItem)).getText().toString();
        RendezVousDAO.deleteById(this, id);
        fillExpendedList();
    }

    public void accept(View view) {
        String id =((TextView)((View)view.getParent().getParent()).findViewById(R.id.idItem)).getText().toString();
        RendezVousDAO.acceptById(this, id);
        fillExpendedList();

    }
}
