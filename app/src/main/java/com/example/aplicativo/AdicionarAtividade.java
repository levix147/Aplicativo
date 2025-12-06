package com.example.aplicativo;

import android.app.DatePickerDialog; // Import necessário
import android.app.TimePickerDialog; // Import necessário
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Calendar; // Import para pegar a data atual
import java.util.Locale;

public class AdicionarAtividade extends AppCompatActivity {

    private EditText editTitulo, editDescricao, editData, editHora, editLocal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_adicionar_atividade);


        editTitulo = findViewById(R.id.edit_titulo);
        editDescricao = findViewById(R.id.edit_descricao);
        editData = findViewById(R.id.edit_data);
        editHora = findViewById(R.id.edit_hora);
        editLocal = findViewById(R.id.edit_local);

        ImageView btVoltar = findViewById(R.id.bt_voltar);
        AppCompatButton btCriar = findViewById(R.id.bt_criar);


        editData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final Calendar calendar = Calendar.getInstance();
                int ano = calendar.get(Calendar.YEAR);
                int mes = calendar.get(Calendar.MONTH);
                int dia = calendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog datePickerDialog = new DatePickerDialog(AdicionarAtividade.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                                String dataFormatada = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month + 1, year);
                                editData.setText(dataFormatada);
                            }
                        }, ano, mes, dia);
                datePickerDialog.show();
            }
        });


        editHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int hora = calendar.get(Calendar.HOUR_OF_DAY);
                int minuto = calendar.get(Calendar.MINUTE);

                TimePickerDialog timePickerDialog = new TimePickerDialog(AdicionarAtividade.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                String horaFormatada = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                                editHora.setText(horaFormatada);
                            }
                        }, hora, minuto, true); // true = formato 24h
                timePickerDialog.show();
            }
        });


        btVoltar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        btCriar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titulo = editTitulo.getText().toString();
                String data = editData.getText().toString();
                String hora = editHora.getText().toString();
                String local = editLocal.getText().toString();

                if (titulo.isEmpty() || data.isEmpty()) {
                    Toast.makeText(AdicionarAtividade.this, "Preencha pelo menos o título e a data!", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(AdicionarAtividade.this, TeladeVisualizacao.class);
                intent.putExtra("NOVO_TITULO", titulo);
                intent.putExtra("NOVA_DATA", data);
                intent.putExtra("NOVA_HORA", hora);
                intent.putExtra("NOVO_LOCAL", local);

                startActivity(intent);
                finish();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}