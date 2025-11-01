package com.example.rastreadordespesas;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AdicionarDespesaActivity extends AppCompatActivity {

    //BANCO DE DADOS
    private AppDatabase db;

    //despesas
    private EditText txtValorDespesa;
    private EditText txtDescricaoDespesa;
    private Spinner spinCategorias;
    private TextView txtDataDespesa;
    private Button btnSalvarDespesa;

    //lista despesa
    private ArrayAdapter<String> spinAdapter;
    private ArrayList<String> spinCategoryNames;
    private List<CategoryEntity> loadedCategories;

    //calendario
    private Calendar dataSelecionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_adicionar_despesa);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_add_expense), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db=AppDatabase.getDatabase(getApplicationContext());



        txtValorDespesa = findViewById(R.id.txtValorDespesa);
        txtDescricaoDespesa = findViewById(R.id.txtDescricaoDespesa);
        spinCategorias = findViewById(R.id.spinCategorias);
        txtDataDespesa = findViewById(R.id.txtDataDespesa);
        btnSalvarDespesa = findViewById(R.id.btnSalvarDespesa);

        spinCategoryNames = new ArrayList<>();
        loadedCategories = new ArrayList<>();
        spinAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinCategoryNames);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCategorias.setAdapter(spinAdapter);

        carregarCategoriasDoBanco();

        dataSelecionada = Calendar.getInstance();
        atualizarDataNoTextView();

        txtDataDespesa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int ano = dataSelecionada.get(Calendar.YEAR);
                int mes = dataSelecionada.get(Calendar.MONTH);
                int dia = dataSelecionada.get(Calendar.DAY_OF_MONTH);


                android.app.DatePickerDialog datePickerDialog = new android.app.DatePickerDialog(AdicionarDespesaActivity.this, new android.app.DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(android.widget.DatePicker view, int year, int month, int dayOfMonth) {
                        dataSelecionada.set(year, month, dayOfMonth);
                        atualizarDataNoTextView();
                    }
                }, ano, mes, dia);
                datePickerDialog.show();
            }
        });

        btnSalvarDespesa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarNovaDespesa();
            }
        });

    }

    private void atualizarDataNoTextView() {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        txtDataDespesa.setText(sdf.format(dataSelecionada.getTime()));
    }



    private void carregarCategoriasDoBanco() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                loadedCategories = db.categoryDao().getAllCategories();
                spinCategoryNames.clear();
                spinCategoryNames.add("Selecione uma categoria");

                for (CategoryEntity categoria : loadedCategories) {
                    spinCategoryNames.add(categoria.getName());
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        spinAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).start();

    }

    private void salvarNovaDespesa() {
        String valorStr = txtValorDespesa.getText().toString();
        String descricao = txtDescricaoDespesa.getText().toString();

        int spinPosition = spinCategorias.getSelectedItemPosition();

        final java.util.Date dataParaSalvar = dataSelecionada.getTime();

        if (valorStr.isEmpty()) {
            Toast.makeText(this, "Por favor, insira um valor", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinPosition == 0) {
            Toast.makeText(this, "Por favor, selecione uma categoria", Toast.LENGTH_SHORT).show();
            return;
        }

        double valor;
        try {
            valor = Double.parseDouble(valorStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valor inválido. ", Toast.LENGTH_SHORT).show();
            return;
        }

        CategoryEntity categoriaSelecionada = loadedCategories.get(spinPosition - 1);
        int categoriaId = categoriaSelecionada.getId();

        final ExpenseEntity novaDespesa = new ExpenseEntity();
        novaDespesa.setValor(valor);
        novaDespesa.setDescricao(descricao);
        novaDespesa.setData(dataParaSalvar);
        novaDespesa.setCategoriaId(categoriaId);

        new Thread(new Runnable() {
            @Override
            public void run() {
                db.expenseDao().insertExpense(novaDespesa);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AdicionarDespesaActivity.this, "Despesa salva !", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            }
        }).start();


    }


}