package com.example.aplicativo;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar;
import java.util.Locale;

public class AdicionarAtividade extends AppCompatActivity {

    private EditText editTitulo, editDescricao, editData, editHora, editLocal;
    private TarefaRepositorio tarefaRepositorio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adicionar_atividade);

        tarefaRepositorio = new TarefaRepositorio();

        editTitulo = findViewById(R.id.edit_titulo);
        editDescricao = findViewById(R.id.edit_descricao);
        editData = findViewById(R.id.edit_data);
        editHora = findViewById(R.id.edit_hora);
        editLocal = findViewById(R.id.edit_local);

        ImageView btVoltar = findViewById(R.id.bt_voltar);
        AppCompatButton btCriar = findViewById(R.id.bt_criar);

        editData.setOnClickListener(v -> {
            final Calendar calendario = Calendar.getInstance();
            int ano = calendario.get(Calendar.YEAR);
            int mes = calendario.get(Calendar.MONTH);
            int dia = calendario.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(AdicionarAtividade.this,
                    (view, year, month, dayOfMonth) -> {
                        String dataFormatada = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
                        editData.setText(dataFormatada);
                    }, ano, mes, dia);
            datePickerDialog.show();
        });

        editHora.setOnClickListener(v -> {
            final Calendar calendario = Calendar.getInstance();
            int hora = calendario.get(Calendar.HOUR_OF_DAY);
            int minuto = calendario.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(AdicionarAtividade.this,
                    (view, hourOfDay, minute) -> {
                        String horaFormatada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        editHora.setText(horaFormatada);
                    }, hora, minuto, true);
            timePickerDialog.show();
        });

        btVoltar.setOnClickListener(v -> finish());

        btCriar.setOnClickListener(v -> {
            String titulo = editTitulo.getText().toString();
            String descricao = editDescricao.getText().toString();
            String data = editData.getText().toString();
            String hora = editHora.getText().toString();
            String local = editLocal.getText().toString();

            if (titulo.isEmpty() || data.isEmpty()) {
                Toast.makeText(AdicionarAtividade.this, "Preencha pelo menos o título e a data!", Toast.LENGTH_SHORT).show();
                return;
            }

            Tarefa novaTarefa = new Tarefa(titulo, descricao, data, hora, local, "A_FAZER");
            tarefaRepositorio.salvarTarefa(novaTarefa);

            Toast.makeText(AdicionarAtividade.this, "Evento criado com sucesso!", Toast.LENGTH_SHORT).show();
            finish();
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            v.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(), insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            return insets;
        });
    }
}