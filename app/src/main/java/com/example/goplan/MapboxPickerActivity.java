package com.example.goplan;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.gestures.GesturesPlugin;
import com.mapbox.maps.plugin.gestures.GesturesUtils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapboxPickerActivity extends AppCompatActivity {

    private MapView mapView;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mapbox_picker);

        mapView = findViewById(R.id.mapView);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS);

        moverCameraParaLocalizacaoAtual();

        findViewById(R.id.bt_confirmar_local).setOnClickListener(v -> {
            double latitude = mapView.getMapboxMap().getCameraState().getCenter().latitude();
            double longitude = mapView.getMapboxMap().getCameraState().getCenter().longitude();

            String endereco = getEnderecoFromCoordinates(latitude, longitude);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("endereco_selecionado", endereco);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        });
    }

    @SuppressLint("MissingPermission")
    private void moverCameraParaLocalizacaoAtual() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                CameraOptions initialCameraOptions = new CameraOptions.Builder()
                        .center(com.mapbox.geojson.Point.fromLngLat(location.getLongitude(), location.getLatitude()))
                        .zoom(14.0)
                        .build();
                mapView.getMapboxMap().setCamera(initialCameraOptions);
            }
        });
    }

    private String getEnderecoFromCoordinates(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                return addresses.get(0).getAddressLine(0);
            } else {
                return "Endereço não encontrado";
            }
        } catch (IOException e) {
            e.printStackTrace();
            return "Erro ao buscar endereço";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
