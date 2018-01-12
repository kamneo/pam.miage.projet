package rendezvousgeolocalises.projet.pam.rendezvous.persistance;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import rendezvousgeolocalises.projet.pam.rendezvous.activities.MainActivity;
import rendezvousgeolocalises.projet.pam.rendezvous.model.RendezVous;
import rendezvousgeolocalises.projet.pam.rendezvous.utils.StatusLevel;

public class RendezVousDAO {
    public static ArrayList<RendezVous> getAllRendezVous(Context context) throws IOException, ClassNotFoundException {
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_prefs",Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String rendezVousesJson = sharedPreferences.getString("rendezVouses", null);
        if(rendezVousesJson == null || rendezVousesJson.length() == 0)
            return new ArrayList<>();
        RendezVous[] rendezVousArray = gson.fromJson(rendezVousesJson, RendezVous[].class);
        ArrayList<RendezVous> res = new ArrayList<>(Arrays.asList(rendezVousArray));
        Calendar lessOneDay = Calendar.getInstance();
        long timeMilli = lessOneDay.getTimeInMillis() - 3600*24*1000;
        lessOneDay.setTimeInMillis(timeMilli);
        for (int i = res.size() - 1; i >= 0; i--) {
            RendezVous rdv = res.get(i);
            if(rdv.getDate().before(lessOneDay.getTime()))
                res.remove(i--);
        }
        storeAllRendezVous(context, res);
        return res;
    }

    public static void storeAllRendezVous(Context context, ArrayList<RendezVous> rendezVous) throws IOException, ClassNotFoundException {
        Gson gson = new Gson();
        SharedPreferences sharedPreferences = context.getSharedPreferences("user_prefs",Context.MODE_PRIVATE);
        String rendezVousJson = gson.toJson(rendezVous);
        sharedPreferences
                .edit()
                .putString("rendezVouses",rendezVousJson)
                .apply();
    }

    public static void storeRendezVous(Context context, RendezVous rendezVous) throws IOException, ClassNotFoundException {
        ArrayList<RendezVous>  rendezVouses = getAllRendezVous(context);
        rendezVous.setId((long) (rendezVouses.size()));
        rendezVouses.add(rendezVous);
        storeAllRendezVous(context, rendezVouses);
    }

    public static  ArrayList<RendezVous> getAllRendezVousAccepted(Context context) throws IOException, ClassNotFoundException {
        ArrayList<RendezVous> rendezVouses = getAllRendezVous(context);
        ArrayList<RendezVous> rendezVousesAccepted = new ArrayList<>();

        for (RendezVous rdv : rendezVouses) {
            if(rdv.getStatus() == StatusLevel.CREATOR || rdv.getStatus() == StatusLevel.ACCEPTED)
                rendezVousesAccepted.add(rdv);
        }
        return rendezVousesAccepted;
    }

    public static  ArrayList<RendezVous> getAllRendezVousNotAccepted(Context context) throws IOException, ClassNotFoundException {
        ArrayList<RendezVous> rendezVouses = getAllRendezVous(context);
        ArrayList<RendezVous> rendezVousesAccepted = new ArrayList<>();

        for (RendezVous rdv : rendezVouses) {
            if(rdv.getStatus() == StatusLevel.WAITING_FOR_VALIDATION)
                rendezVousesAccepted.add(rdv);
        }
        return rendezVousesAccepted;
    }

    public static List<List<String>> getAllFormatedRendezVousAccepted(Context context) throws IOException, ClassNotFoundException {
        List<List<String>> res = new ArrayList<>();
        ArrayList<RendezVous> rendezVouses = getAllRendezVousAccepted(context);
        SimpleDateFormat formater = new SimpleDateFormat("EEEE, d MMM yyyy");

        for (RendezVous rdv: rendezVouses) {
            ArrayList<String> values = new ArrayList<>();
            values.add(rdv.getName());
            values.add(formater.format(rdv.getDate()));
            values.add(rdv.getId() + "");
            values.add(rdv.getStatus() + "");
            res.add(values);
        }

        return res;
    }

    public static List<List<String>> getAllFormatedRendezVousNotAccepted(Context context) throws IOException, ClassNotFoundException {
        List<List<String>> res = new ArrayList<>();
        ArrayList<RendezVous> rendezVouses = getAllRendezVousNotAccepted(context);
        SimpleDateFormat formater = new SimpleDateFormat("EEEE, d MMM yyyy");
        for (RendezVous rdv: rendezVouses) {
            ArrayList<String> values = new ArrayList<>();
            values.add(rdv.getName());
            values.add(formater.format(rdv.getDate()));
            values.add(rdv.getId() + "");
            values.add(rdv.getStatus() + "");
            res.add(values);
        }

        return res;
    }

    public static RendezVous getRendezVousById(Context context, String id) throws IOException, ClassNotFoundException {
        ArrayList<RendezVous> rendezVouses = getAllRendezVous(context);
        for (RendezVous rdv:rendezVouses) {
            if(rdv.getId() == Long.parseLong(id))
                return rdv;
        }
        return null;
    }

    public static void deleteById(Context context, String id) {
        try {
            RendezVous rdv = getRendezVousById(context, id);
            rdv.setStatus(StatusLevel.REFUSED);
            ArrayList<RendezVous> rendezVouses = getAllRendezVous(context);
            rendezVouses.set(Integer.parseInt(id), rdv);
            storeAllRendezVous(context, rendezVouses);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void acceptById(Context context, String id) {
        try {
            RendezVous rdv = getRendezVousById(context, id);
            rdv.setStatus(StatusLevel.ACCEPTED);
            ArrayList<RendezVous> rendezVouses = getAllRendezVous(context);
            rendezVouses.set(Integer.parseInt(id), rdv);
            storeAllRendezVous(context, rendezVouses);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
