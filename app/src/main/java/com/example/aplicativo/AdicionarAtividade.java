package com.example.aplicativo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class AdicionarAtividade extends AppCompatActivity {

    private static final String TAG = "AdicionarAtividade";

    private EditText editTitulo, editDescricao, editData, editHora, editLocal;
    private SwitchMaterial switchGoogleCalendar;
    private TarefaRepositorio tarefaRepositorio;

    private FusedLocationProviderClient fusedLocationClient;
    private ActivityResultLauncher<String> requestLocationPermissionLauncher;
    private ActivityResultLauncher<String> requestCalendarPermissionLauncher;

    private com.google.api.services.calendar.Calendar calendarService;
    private final Calendar calendarStart = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_atividade);

        tarefaRepositorio = new TarefaRepositorio();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        iniciarComponentes();
        configurarListeners();
        configurarLaunchersDePermissao();
        configurarClienteGoogleCalendar();
    }

    private void iniciarComponentes() {
        editTitulo = findViewById(R.id.edit_titulo);
        editDescricao = findViewById(R.id.edit_descricao);
        editData = findViewById(R.id.edit_data);
        editHora = findViewById(R.id.edit_hora);
        editLocal = findViewById(R.id.edit_local);
        switchGoogleCalendar = findViewById(R.id.switch_google_calendar);
    }

    private void configurarListeners() {
        findViewById(R.id.bt_voltar).setOnClickListener(v -> finish());
        findViewById(R.id.bt_criar).setOnClickListener(v -> salvarNovaTarefa());
        editData.setOnClickListener(v -> mostrarDatePicker());
        editHora.setOnClickListener(v -> mostrarTimePicker());
        editLocal.setOnClickListener(v -> buscarLocalizacaoAtual());
    }

    private void configurarLaunchersDePermissao() {
        requestLocationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                obterLocalizacao();
            } else {
                Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_SHORT).show();
            }
        });

        requestCalendarPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                adicionarEventoAoGoogleCalendar();
            } else {
                Toast.makeText(this, "Permissão de calendário negada.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void configurarClienteGoogleCalendar() {
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                this, Collections.singleton(CalendarScopes.CALENDAR_EVENTS));
        credential.setSelectedAccount(GoogleSignIn.getLastSignedInAccount(this).getAccount());

        calendarService = new com.google.api.services.calendar.Calendar.Builder(
                com.google.api.client.http.javanet.NetHttpTransport.INSTANCE,
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName(getString(R.string.app_name))
                .build();
    }

    private void salvarNovaTarefa() {
        String titulo = editTitulo.getText().toString();
        String descricao = editDescricao.getText().toString();
        String data = editData.getText().toString();
        String hora = editHora.getText().toString();
        String local = editLocal.getText().toString();

        if (titulo.isEmpty() || data.isEmpty() || hora.isEmpty()) {
            Toast.makeText(this, "Preencha título, data e hora!", Toast.LENGTH_SHORT).show();
            return;
        }

        Tarefa novaTarefa = new Tarefa(titulo, descricao, data, hora, local, "A_FAZER");
        tarefaRepositorio.salvarTarefa(novaTarefa);

        Toast.makeText(this, "Evento criado com sucesso!", Toast.LENGTH_SHORT).show();

        if (switchGoogleCalendar.isChecked()) {
            iniciarProcessoGoogleCalendar();
        }

        finish();
    }

    private void iniciarProcessoGoogleCalendar() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            adicionarEventoAoGoogleCalendar();
        } else {
            requestCalendarPermissionLauncher.launch(Manifest.permission.WRITE_CALENDAR);
        }
    }

    private void adicionarEventoAoGoogleCalendar() {
        new Thread(() -> {
            try {
                Event evento = new Event()
                        .setSummary(editTitulo.getText().toString())
                        .setDescription(editDescricao.getText().toString())
                        .setLocation(editLocal.getText().toString());

                // Define a hora de inicio
                Date startDate = calendarStart.getTime();
                DateTime startDateTime = new DateTime(startDate);
                EventDateTime start = new EventDateTime().setDateTime(startDateTime).setTimeZone(TimeZone.getDefault().getID());
                evento.setStart(start);

                // Define a hora de fim (assumindo 1 hora de duracao)
                Calendar endCalendar = (Calendar) calendarStart.clone();
                endCalendar.add(Calendar.HOUR_OF_DAY, 1);
                Date endDate = endCalendar.getTime();
                DateTime endDateTime = new DateTime(endDate);
                EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone(TimeZone.getDefault().getID());
                evento.setEnd(end);

                calendarService.events().insert("primary", evento).execute();

                runOnUiThread(() -> Toast.makeText(AdicionarAtividade.this, "Evento adicionado ao Google Agenda!", Toast.LENGTH_LONG).show());

            } catch (Exception e) {
                Log.e(TAG, "Erro ao adicionar evento no Google Calendar", e);
                runOnUiThread(() -> Toast.makeText(AdicionarAtividade.this, "Falha ao adicionar no Google Agenda.", Toast.LENGTH_LONG).show());
            }
        }).start();
    }
    
    // --- Metodos de Localizacao ---
    private void buscarLocalizacaoAtual() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            obterLocalizacao();
        } else {
            requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    @SuppressLint("MissingPermission")
    private void obterLocalizacao() {
        fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
            if (location != null) {
                converterCoordenadasParaEndereco(location.getLatitude(), location.getLongitude());
            } else {
                Toast.makeText(this, "Não foi possível obter a localização.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void converterCoordenadasParaEndereco(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                editLocal.setText(addresses.get(0).getAddressLine(0));
            }
        } catch (IOException e) {
            Log.e(TAG, "Servico de Geocoder indisponivel", e);
        }
    }

    // --- Metodos de Data e Hora ---
    private void mostrarDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendarStart.set(Calendar.YEAR, year);
            calendarStart.set(Calendar.MONTH, month);
            calendarStart.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            String dataFormatada = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
            editData.setText(dataFormatada);
        }, calendarStart.get(Calendar.YEAR), calendarStart.get(Calendar.MONTH), calendarStart.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void mostrarTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendarStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendarStart.set(Calendar.MINUTE, minute);
            String horaFormatada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
            editHora.setText(horaFormatada);
        }, calendarStart.get(Calendar.HOUR_OF_DAY), calendarStart.get(Calendar.MINUTE), true).show();
    }
}
