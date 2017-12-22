package rendezvousgeolocalises.projet.pam.rendezvous.model;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import rendezvousgeolocalises.projet.pam.rendezvous.model.MyLocation;

public class RendezVous {
    private Date date;
    private String name;
    private List<String> contacts;
    private MyLocation location;
    private Context context;

    public RendezVous(Context pContext, String pName, Date pDate, List<String> pContacts, Place pPlace) throws IOException {
        this(pContext, pName, pDate, pContacts, pPlace.getLatLng());
    }

    public RendezVous(Context pContext, String pName, Date pDate, List<String> pContacts, LatLng pLatLng) throws IOException {
        date = pDate;
        name = pName;
        contacts = pContacts;
        context = pContext;

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());

        addresses = geocoder.getFromLocation(pLatLng.latitude, pLatLng.longitude , 1);
        if(addresses.size()>1)
            location = new MyLocation(addresses.get(0));
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
}
