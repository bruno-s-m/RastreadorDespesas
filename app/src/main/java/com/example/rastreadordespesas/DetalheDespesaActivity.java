package com.example.rastreadordespesas;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Imports necessários
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class DetalheDespesaActivity extends AppCompatActivity {

    private EditText txtValor;
    private EditText txtDescricao;
    private Spinner spinCategorias;
    private TextView txtData;
    private Button btnSalvarAlteracoes;
    private Button btnExcluirDespesa;
    private AppDatabase db;
    private ExpenseEntity despesaAtual;
    private int despesaId;
    private ArrayAdapter<String> spinnerAdapter;
    private ArrayList<String> spinCategoryNames;
    private List<CategoryEntity> loadedCategories;
    private Calendar dataSelecionada;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalhe_despesa);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        despesaId = getIntent().getIntExtra("DESPESA_ID", -1);
        if (despesaId == -1) {
            Toast.makeText(this, "Erro ao carregar despesa.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = AppDatabase.getDatabase(getApplicationContext());

        inicializarComponentes();

        configurarListeners();

        carregarDadosIniciais();
    }

    private void inicializarComponentes() {

        txtValor = findViewById(R.id.txtValor);
        txtDescricao = findViewById(R.id.txtDescricao);
        spinCategorias = findViewById(R.id.spinCategorias);
        txtData = findViewById(R.id.txtData);
        btnSalvarAlteracoes = findViewById(R.id.btnSalvarAlteracoes);
        btnExcluirDespesa = findViewById(R.id.btnExcluirDespesa);

        spinCategoryNames = new ArrayList<>();
        loadedCategories = new ArrayList<>();
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinCategoryNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCategorias.setAdapter(spinnerAdapter);

        dataSelecionada = Calendar.getInstance();
    }

    private void configurarListeners() {

        txtData.setOnClickListener(v -> mostrarDatePicker());

        btnExcluirDespesa.setOnClickListener(v -> excluirDespesa());

        btnSalvarAlteracoes.setOnClickListener(v -> salvarAlteracoes());
    }

    private void mostrarDatePicker() {
        int ano = dataSelecionada.get(Calendar.YEAR);
        int mes = dataSelecionada.get(Calendar.MONTH);
        int dia = dataSelecionada.get(Calendar.DAY_OF_MONTH);

        new DatePickerDialog(DetalheDespesaActivity.this, (view, year, month, dayOfMonth) -> {
            dataSelecionada.set(year, month, dayOfMonth);
            atualizarDataNoTextView();
        }, ano, mes, dia).show();
    }

    private void atualizarDataNoTextView() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        txtData.setText(sdf.format(dataSelecionada.getTime()));
    }

    private void carregarDadosIniciais() {
        new Thread(() -> {
            despesaAtual = db.expenseDao().getExpenseById(despesaId);
            if (despesaAtual == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Despesa não encontrada.", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            loadedCategories = db.categoryDao().getAllCategories();
            spinCategoryNames.clear();
            spinCategoryNames.add("Selecione uma categoria...");

            int posicaoCategoriaNoSpinner = 0;

            for (int i = 0; i < loadedCategories.size(); i++) {
                CategoryEntity cat = loadedCategories.get(i);
                spinCategoryNames.add(cat.getName());

                if (cat.getId() == despesaAtual.getCategoriaId()) {
                    posicaoCategoriaNoSpinner = i + 1;
                }
            }

            dataSelecionada.setTime(despesaAtual.getData());

            int finalPosicaoCategoria = posicaoCategoriaNoSpinner;
            runOnUiThread(() -> {
                txtValor.setText(String.format(Locale.US, "%.2f", despesaAtual.getValor()));
                txtDescricao.setText(despesaAtual.getDescricao());
                atualizarDataNoTextView();
                spinnerAdapter.notifyDataSetChanged();
                spinCategorias.setSelection(finalPosicaoCategoria);
            });
        }).start();
    }

    private void salvarAlteracoes() {
        String valorStr = txtValor.getText().toString();
        String descricao = txtDescricao.getText().toString();
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
            valor = Double.parseDouble(valorStr.replace(",", "."));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valor inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        CategoryEntity categoriaSelecionada = loadedCategories.get(spinPosition - 1);
        int categoriaId = categoriaSelecionada.getId();

        despesaAtual.setValor(valor);
        despesaAtual.setDescricao(descricao);
        despesaAtual.setData(dataParaSalvar);
        despesaAtual.setCategoriaId(categoriaId);

        new Thread(() -> {
            db.expenseDao().updateExpense(despesaAtual);
            runOnUiThread(() -> {
                Toast.makeText(this, "Despesa atualizada!", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }

    private void excluirDespesa() {
        if (despesaAtual == null) {
            Toast.makeText(this, "Erro: Despesa não carregada.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            db.expenseDao().deleteExpense(despesaAtual);
            runOnUiThread(() -> {
                Toast.makeText(DetalheDespesaActivity.this, "Despesa excluída", Toast.LENGTH_SHORT).show();
                finish();
            });
        }).start();
    }
}