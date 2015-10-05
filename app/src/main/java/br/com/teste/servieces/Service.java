package br.com.teste.servieces;

import android.content.Context;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.List;

import br.com.teste.entities.Driver;
import br.com.teste.utils.Constants;

/**
 * Created by aoliveira on 29/09/15.
 */
public class Service {

 public static Future<List<Driver>> getlistDrivers (FutureCallback<List<Driver>> futureCallback,
                                                    LatLng sw,
                                                    LatLng ne,
                                                    Context context) {
        String url= Constants.SERVICE_URI + "sw=" + sw.latitude + "," + sw.longitude + "&ne=" + ne.latitude + "," + ne.longitude;

        return Ion.with(context)
                .load(url)
                .setTimeout(17000)
                .as(new TypeToken<List<Driver>>() {
                })
                .setCallback(futureCallback);
    }
}
