package com.example.goplan;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class AdicionarAtividade extends AppCompatActivity {

    private static final String TAG = "AdicionarAtividade";

    private EditText editTitulo, editDescricao, editData, editHora, editLocal;
    private SwitchMaterial switchGoogleCalendar;
    private TarefaRepositorio tarefaRepositorio;
    private FirebaseAuth mAuth; // <<< Firebase Auth adicionado

    private ActivityResultLauncher<String> requestCalendarPermissionLauncher;
    private ActivityResultLauncher<Intent> mapPickerLauncher;

    private com.google.api.services.calendar.Calendar calendarService;
    private final Calendar calendarStart = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_atividade);

        tarefaRepositorio = new TarefaRepositorio();
        mAuth = FirebaseAuth.getInstance(); // <<< Firebase Auth inicializado

        iniciarComponentes();
        configurarListeners();
        configurarLaunchers();
        configurarClienteGoogleCalendar();
    }

    // ... (metodos existentes sem alteracao) ...

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
        findViewById(R.id.bt_criar).setOnClickListener(v -> iniciarProcessoDeSalvar());
        editData.setOnClickListener(v -> mostrarDatePicker());
        editHora.setOnClickListener(v -> mostrarTimePicker());

        editLocal.setOnClickListener(v -> {
            Intent intent = new Intent(AdicionarAtividade.this, MapboxPickerActivity.class);
            mapPickerLauncher.launch(intent);
        });
    }

    private void configurarLaunchers() {
        requestCalendarPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                salvarTarefaEEventoNoCalendario();
            } else {
                Toast.makeText(this, "Permissão de calendário negada.", Toast.LENGTH_LONG).show();
            }
        });

        mapPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Intent data = result.getData();
                        if (data != null && data.hasExtra("endereco_selecionado")) {
                            String endereco = data.getStringExtra("endereco_selecionado");
                            editLocal.setText(endereco);
                        }
                    }
                });
    }

    private void configurarClienteGoogleCalendar() {
        GoogleSignInAccount googleAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (googleAccount == null) {
            switchGoogleCalendar.setEnabled(false);
            return;
        }

        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                this, Collections.singleton(CalendarScopes.CALENDAR_EVENTS));
        credential.setSelectedAccount(googleAccount.getAccount());

        calendarService = new com.google.api.services.calendar.Calendar.Builder(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName(getString(R.string.app_name))
                .build();
    }

    private void iniciarProcessoDeSalvar() {
        if (editTitulo.getText().toString().isEmpty() || editData.getText().toString().isEmpty() || editHora.getText().toString().isEmpty()) {
            Toast.makeText(this, "Preencha título, data e hora!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!switchGoogleCalendar.isChecked()) {
            salvarApenasTarefaLocal();
            finish();
            return;
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
            salvarTarefaEEventoNoCalendario();
        } else {
            requestCalendarPermissionLauncher.launch(Manifest.permission.WRITE_CALENDAR);
        }
    }

    private void salvarApenasTarefaLocal() {
        Tarefa novaTarefa = criarTarefaAPartirDoForm();
        if(novaTarefa != null) {
            tarefaRepositorio.salvarTarefa(novaTarefa);
            Toast.makeText(this, "Evento criado com sucesso!", Toast.LENGTH_SHORT).show();
        }
    }

    private void salvarTarefaEEventoNoCalendario() {
        Tarefa novaTarefa = criarTarefaAPartirDoForm();
        if(novaTarefa == null) return;

        tarefaRepositorio.salvarTarefa(novaTarefa);

        new Thread(() -> {
            try {
                Event evento = new Event()
                        .setSummary(novaTarefa.getTitulo())
                        .setDescription(novaTarefa.getDescricao())
                        .setLocation(novaTarefa.getLocal());

                Date startDate = calendarStart.getTime();
                DateTime startDateTime = new DateTime(startDate);
                EventDateTime start = new EventDateTime().setDateTime(startDateTime).setTimeZone(TimeZone.getDefault().getID());
                evento.setStart(start);

                Calendar endCalendar = (Calendar) calendarStart.clone();
                endCalendar.add(Calendar.HOUR_OF_DAY, 1);
                Date endDate = endCalendar.getTime();
                DateTime endDateTime = new DateTime(endDate);
                EventDateTime end = new EventDateTime().setDateTime(endDateTime).setTimeZone(TimeZone.getDefault().getID());
                evento.setEnd(end);

                calendarService.events().insert("primary", evento).execute();

                runOnUiThread(() -> Toast.makeText(AdicionarAtividade.this, "Evento adicionado ao app e à agenda!", Toast.LENGTH_LONG).show());

            } catch (Exception e) {
                Log.e(TAG, "Erro ao adicionar evento no Google Calendar", e);
                runOnUiThread(() -> Toast.makeText(AdicionarAtividade.this, "Falha ao adicionar na agenda.", Toast.LENGTH_LONG).show());
            }
            finish();
        }).start();
    }

    // Metodo ATUALIZADO para incluir o userId
    private Tarefa criarTarefaAPartirDoForm() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            // Isso nao deveria acontecer se o usuario chegou aqui, mas e uma boa verificacao de seguranca
            Toast.makeText(this, "Erro: Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return null;
        }

        String userId = user.getUid();
        String titulo = editTitulo.getText().toString();
        String descricao = editDescricao.getText().toString();
        String data = editData.getText().toString();
        String hora = editHora.getText().toString();
        String local = editLocal.getText().toString();
        return new Tarefa(userId, titulo, descricao, data, hora, local, "A_FAZER");
    }

    private void mostrarDatePicker() {
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendarStart.set(Calendar.YEAR, year);
            calendarStart.set(Calendar.MONTH, month);
            calendarStart.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            editData.setText(sdf.format(calendarStart.getTime()));
        }, calendarStart.get(Calendar.YEAR), calendarStart.get(Calendar.MONTH), calendarStart.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void mostrarTimePicker() {
        new TimePickerDialog(this, (view, hourOfDay, minute) -> {
            calendarStart.set(Calendar.HOUR_OF_DAY, hourOfDay);
            calendarStart.set(Calendar.MINUTE, minute);
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
            editHora.setText(sdf.format(calendarStart.getTime()));
        }, calendarStart.get(Calendar.HOUR_OF_DAY), calendarStart.get(Calendar.MINUTE), true).show();
    }
}
