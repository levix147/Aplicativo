package com.example.goplan;

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    private Button btCriar;
    private TextView txtTituloPagina;

    private TarefaRepositorio tarefaRepositorio;
    private FirebaseAuth mAuth;

    private ActivityResultLauncher<String> requestCalendarPermissionLauncher;
    private ActivityResultLauncher<String> requestLocationPermissionLauncher;
    private ActivityResultLauncher<Intent> mapPickerLauncher;

    private com.google.api.services.calendar.Calendar calendarService;
    private final Calendar calendarStart = Calendar.getInstance();

    private Tarefa tarefaParaEditar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_atividade);

        tarefaRepositorio = new TarefaRepositorio();
        mAuth = FirebaseAuth.getInstance();

        iniciarComponentes();
        configurarListeners();
        configurarLaunchers();
        configurarClienteGoogleCalendar();

        if (getIntent().hasExtra("TAREFA_PARA_EDITAR")) {
            tarefaParaEditar = getIntent().getParcelableExtra("TAREFA_PARA_EDITAR");
            preencherDadosParaEdicao();
        }
    }

    private void iniciarComponentes() {
        editTitulo = findViewById(R.id.edit_titulo);
        editDescricao = findViewById(R.id.edit_descricao);
        editData = findViewById(R.id.edit_data);
        editHora = findViewById(R.id.edit_hora);
        editLocal = findViewById(R.id.edit_local);
        switchGoogleCalendar = findViewById(R.id.switch_google_calendar);
        btCriar = findViewById(R.id.bt_criar);
        txtTituloPagina = findViewById(R.id.txt_titulo_pagina);
    }

    private void configurarListeners() {
        findViewById(R.id.bt_voltar).setOnClickListener(v -> finish());
        btCriar.setOnClickListener(v -> iniciarProcessoDeSalvar());
        editData.setOnClickListener(v -> mostrarDatePicker());
        editHora.setOnClickListener(v -> mostrarTimePicker());

        editLocal.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(AdicionarAtividade.this, MapboxPickerActivity.class);
                mapPickerLauncher.launch(intent);
            } else {
                requestLocationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        });
    }

    private void preencherDadosParaEdicao() {
        if (tarefaParaEditar == null) return;

        if (txtTituloPagina != null) {
            txtTituloPagina.setText("Editar Evento");
        }
        btCriar.setText("Salvar Alterações");

        editTitulo.setText(tarefaParaEditar.getTitulo());
        editDescricao.setText(tarefaParaEditar.getDescricao());
        editData.setText(tarefaParaEditar.getData());
        editHora.setText(tarefaParaEditar.getHora());
        editLocal.setText(tarefaParaEditar.getLocal());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        try {
            Date data = sdf.parse(tarefaParaEditar.getData() + " " + tarefaParaEditar.getHora());
            if (data != null) {
                calendarStart.setTime(data);
            }
        } catch (ParseException e) {
            Log.e(TAG, "Erro ao parsear data e hora para edição", e);
        }
    }

    private void iniciarProcessoDeSalvar() {
        if (editTitulo.getText().toString().isEmpty() || editData.getText().toString().isEmpty() || editHora.getText().toString().isEmpty()) {
            Toast.makeText(this, "Preencha título, data e hora!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tarefaParaEditar != null) {
            salvarAlteracoes();
        } else {
            criarNovaTarefa();
        }
    }

    private void criarNovaTarefa() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) {
            Toast.makeText(this, "Erro: Usuário não autenticado.", Toast.LENGTH_SHORT).show();
            return;
        }

        Tarefa novaTarefa = new Tarefa(
            user.getUid(),
            editTitulo.getText().toString(),
            editDescricao.getText().toString(),
            editData.getText().toString(),
            editHora.getText().toString(),
            editLocal.getText().toString(),
            "A_FAZER"
        );
        novaTarefa.setCodigoDeConvite(gerarCodigoDeConvite());
        List<String> membros = new ArrayList<>();
        membros.add(user.getUid());
        novaTarefa.setMembros(membros);

        tarefaRepositorio.salvarTarefa(novaTarefa)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(AdicionarAtividade.this, "Evento criado com sucesso!", Toast.LENGTH_SHORT).show();
                if (switchGoogleCalendar.isChecked()) {
                    adicionarEventoAoCalendario(novaTarefa);
                }
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(AdicionarAtividade.this, "Falha ao criar evento.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Erro ao salvar tarefa", e);
            });
    }

    private void salvarAlteracoes() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user == null) return;

        Tarefa tarefaAtualizada = new Tarefa(
            tarefaParaEditar.getId(),
            user.getUid(),
            editTitulo.getText().toString(),
            editDescricao.getText().toString(),
            editData.getText().toString(),
            editHora.getText().toString(),
            editLocal.getText().toString(),
            tarefaParaEditar.getStatus()
        );
        tarefaAtualizada.setCodigoDeConvite(tarefaParaEditar.getCodigoDeConvite());
        tarefaAtualizada.setMembros(tarefaParaEditar.getMembros());

        tarefaRepositorio.atualizarTarefa(tarefaAtualizada)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(AdicionarAtividade.this, "Evento atualizado com sucesso!", Toast.LENGTH_SHORT).show();
                 if (switchGoogleCalendar.isChecked()) {
                    adicionarEventoAoCalendario(tarefaAtualizada);
                }
                finish();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(AdicionarAtividade.this, "Falha ao atualizar evento.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Erro ao atualizar tarefa", e);
            });
    }

    private void adicionarEventoAoCalendario(Tarefa tarefa) {
         if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            requestCalendarPermissionLauncher.launch(Manifest.permission.WRITE_CALENDAR);
            return;
        }

        new Thread(() -> {
            try {
                Event evento = new Event()
                    .setSummary(tarefa.getTitulo())
                    .setDescription(tarefa.getDescricao())
                    .setLocation(tarefa.getLocal());

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

                runOnUiThread(() -> Toast.makeText(AdicionarAtividade.this, "Evento adicionado à sua agenda!", Toast.LENGTH_SHORT).show());

            } catch (Exception e) {
                Log.e(TAG, "Erro ao adicionar evento no Google Calendar", e);
                runOnUiThread(() -> Toast.makeText(AdicionarAtividade.this, "Falha ao sincronizar com a agenda.", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }
    
    private String gerarCodigoDeConvite() {
        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder codigo = new StringBuilder();
        java.util.Random random = new java.util.Random();
        for (int i = 0; i < 6; i++) {
            codigo.append(caracteres.charAt(random.nextInt(caracteres.length())));
        }
        return codigo.toString();
    }

    private void configurarLaunchers() {
        requestCalendarPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                // Tenta novamente após obter permissão
                iniciarProcessoDeSalvar(); 
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

        requestLocationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Intent intent = new Intent(AdicionarAtividade.this, MapboxPickerActivity.class);
                mapPickerLauncher.launch(intent);
            } else {
                Toast.makeText(this, "Permissão de localização negada.", Toast.LENGTH_LONG).show();
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
