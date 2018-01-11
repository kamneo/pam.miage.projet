package rendezvousgeolocalises.projet.pam.rendezvous.model;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rendezvousgeolocalises.projet.pam.rendezvous.model.MyLocation;
import rendezvousgeolocalises.projet.pam.rendezvous.persistance.RendezVousDAO;
import rendezvousgeolocalises.projet.pam.rendezvous.utils.StatusLevel;

public class RendezVous implements Serializable {
    private Date date;
    private String name;
    private List<String> contacts;
    private MyLocation location;
    private int status;
    private Long id;

    public RendezVous(Context pContext, String pName, Date pDate, List<String> pContacts, Place pPlace, int pStatus) throws IOException {
        this(pContext, pName, pDate, pContacts, pPlace.getLatLng(), pStatus);
    }

    public RendezVous(Context pContext, String pName, Date pDate, List<String> pContacts, LatLng pLatLng, int pStatus) throws IOException {
        date = pDate;
        name = pName;
        contacts = pContacts;

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(pContext, Locale.getDefault());

        addresses = geocoder.getFromLocation(pLatLng.latitude, pLatLng.longitude , 1);
        if(addresses.size()>=1)
            location = new MyLocation(addresses.get(0));

        status = pStatus;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getContacts() {
        return contacts;
    }

    public void setContacts(List<String> contacts) {
        this.contacts = contacts;
    }

    public MyLocation getLocation() {
        return location;
    }

    public void setLocation(MyLocation location) {
        this.location = location;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String sendInformations() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
        String date = formatter.format(getDate());
        return "name='"+getName()+"', date='" + date + "', latitude='" + getLocation().getLatitude() + "', longitude='"+getLocation().getLongitude()+"'";
    }

    public static RendezVous deserialize(Context c, String str) throws IOException {
        String[] strs = str.split("'");
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy");
        Date d = new Date();
        try {
            d = formatter.parse(strs[3]);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        RendezVous rendezVous =  new RendezVous(c, strs[1], d, new ArrayList<String>(), new LatLng(Double.parseDouble(strs[5]), Double.parseDouble(strs[7])), StatusLevel.WAITING_FOR_VALIDATION);
        try {
            RendezVousDAO.storeRendezVous(c, rendezVous);
        } catch (ClassNotFoundException e) {
            Toast.makeText(c, "Erreur de l'import", Toast.LENGTH_SHORT).show();
        }
        return rendezVous;
    }

}
