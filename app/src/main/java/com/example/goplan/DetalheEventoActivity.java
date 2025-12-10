package com.example.goplan;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;
import com.mapbox.maps.CameraOptions;
import com.mapbox.maps.MapView;
import com.mapbox.maps.Style;
import com.mapbox.maps.plugin.annotation.AnnotationConfig;
import com.mapbox.maps.plugin.annotation.AnnotationPlugin;
import com.mapbox.maps.plugin.annotation.AnnotationsUtils;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManagerKt;
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DetalheEventoActivity extends AppCompatActivity {

    private MapView mapView;
    private Tarefa tarefa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalhe_evento);

        tarefa = getIntent().getParcelableExtra("TAREFA_EXTRA");
        if (tarefa == null) {
            finish();
            return;
        }

        mapView = findViewById(R.id.mapViewDetalhe);

        configurarToolbar();
        preencherDetalhes();
        iniciarMapa();
    }

    private void configurarToolbar() {
        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void preencherDetalhes() {
        TextView txtTitulo = findViewById(R.id.detalhe_titulo);
        TextView txtDescricao = findViewById(R.id.detalhe_descricao);
        TextView txtDataHora = findViewById(R.id.detalhe_data_hora);
        TextView txtLocal = findViewById(R.id.detalhe_local_texto);

        txtTitulo.setText(tarefa.getTitulo());
        txtDescricao.setText(tarefa.getDescricao());
        txtDataHora.setText(String.format("%s às %s", tarefa.getData(), tarefa.getHora()));
        txtLocal.setText(tarefa.getLocal());
    }

    private void iniciarMapa() {
        if (TextUtils.isEmpty(tarefa.getLocal())) {
            return;
        }

        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS, style -> {
            new Thread(() -> {
                try {
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    List<Address> addresses = geocoder.getFromLocationName(tarefa.getLocal(), 1);

                    if (addresses != null && !addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        double latitude = address.getLatitude();
                        double longitude = address.getLongitude();

                        runOnUiThread(() -> {
                            configurarCameraDoMapa(latitude, longitude);
                            adicionarMarcador(latitude, longitude);
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        });
    }

    private void configurarCameraDoMapa(double latitude, double longitude) {
        CameraOptions cameraOptions = new CameraOptions.Builder()
                .center(com.mapbox.geojson.Point.fromLngLat(longitude, latitude))
                .zoom(14.0)
                .build();
        mapView.getMapboxMap().setCamera(cameraOptions);
    }

    private void adicionarMarcador(double latitude, double longitude) {
        AnnotationPlugin annotationApi = AnnotationsUtils.getAnnotations(mapView);
        // A linha abaixo foi corrigida para passar a configuracao correta
        PointAnnotationManager pointAnnotationManager = PointAnnotationManagerKt.createPointAnnotationManager(annotationApi, new AnnotationConfig());

        PointAnnotationOptions pointAnnotationOptions = new PointAnnotationOptions()
                .withPoint(com.mapbox.geojson.Point.fromLngLat(longitude, latitude));
        
        pointAnnotationManager.create(pointAnnotationOptions);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
