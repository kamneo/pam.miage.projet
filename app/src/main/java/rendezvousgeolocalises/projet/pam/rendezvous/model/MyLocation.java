package rendezvousgeolocalises.projet.pam.rendezvous.model;

import android.location.Address;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;

public class MyLocation implements Serializable{
    private double latitude;
    private double longitude;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;

    public MyLocation(Address pAddress){
        latitude = pAddress.getLatitude();
        longitude = pAddress.getLongitude();
        address = pAddress.getAddressLine(0);
        city = pAddress.getLocality();
        state = pAddress.getAdminArea();
        country = pAddress.getCountryName();
        postalCode = pAddress.getPostalCode();
    }


    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }



    @Override
    public String toString() {
        return "MyLocation{" +
                "latLng=" + latitude + " : " + longitude +
                ", address='" + address + '\'' +
                ", city='" + city + '\'' +
                ", state='" + state + '\'' +
                ", country='" + country + '\'' +
                ", postalCode='" + postalCode + '\'' +
                '}';
    }
}
