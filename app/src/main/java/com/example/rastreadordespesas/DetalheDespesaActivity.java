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

    // --- VARIÁVEIS DA CLASSE ---

    // Componentes Visuais (usando seus IDs)
    private EditText txtValor;
    private EditText txtDescricao;
    private Spinner spinCategorias;
    private TextView txtData;
    private Button btnSalvarAlteracoes;
    private Button btnExcluirDespesa;

    // Dados
    private AppDatabase db;
    private ExpenseEntity despesaAtual; // A despesa que estamos editando
    private int despesaId;

    // Dados para o Spinner e Data
    private ArrayAdapter<String> spinnerAdapter;
    private ArrayList<String> spinCategoryNames;
    private List<CategoryEntity> loadedCategories;
    private Calendar dataSelecionada;

    // --- CICLO DE VIDA DA ACTIVITY ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_detalhe_despesa);

        // Ajuste do layout (usando o ID 'main' do seu XML)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Pega o ID enviado pela tela de Histórico
        despesaId = getIntent().getIntExtra("DESPESA_ID", -1);
        if (despesaId == -1) {
            Toast.makeText(this, "Erro ao carregar despesa.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Inicializa o banco de dados
        db = AppDatabase.getDatabase(getApplicationContext());

        // Conecta os componentes visuais com o código
        inicializarComponentes();

        // Configura os cliques dos botões e do seletor de data
        configurarListeners();

        // Busca os dados da despesa no banco e preenche a tela
        carregarDadosIniciais();
    }

    // --- MÉTODOS DE CONFIGURAÇÃO ---

    private void inicializarComponentes() {
        // Conectando com os IDs do seu XML
        txtValor = findViewById(R.id.txtValor);
        txtDescricao = findViewById(R.id.txtDescricao);
        spinCategorias = findViewById(R.id.spinCategorias);
        txtData = findViewById(R.id.txtData);
        btnSalvarAlteracoes = findViewById(R.id.btnSalvarAlteracoes);
        btnExcluirDespesa = findViewById(R.id.btnExcluirDespesa);

        // Configura o Spinner
        spinCategoryNames = new ArrayList<>();
        loadedCategories = new ArrayList<>();
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, spinCategoryNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinCategorias.setAdapter(spinnerAdapter);

        // Inicializa a data
        dataSelecionada = Calendar.getInstance();
    }

    private void configurarListeners() {
        // Listener para o Seletor de Data
        txtData.setOnClickListener(v -> mostrarDatePicker());

        // Listener para Excluir Despesa
        btnExcluirDespesa.setOnClickListener(v -> excluirDespesa());

        // Listener para Salvar Alterações
        btnSalvarAlteracoes.setOnClickListener(v -> salvarAlteracoes());
    }

    // --- MÉTODOS DE LÓGICA (BANCO E UI) ---

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
            // 1. Carrega a despesa que estamos editando
            despesaAtual = db.expenseDao().getExpenseById(despesaId);
            if (despesaAtual == null) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Despesa não encontrada.", Toast.LENGTH_SHORT).show();
                    finish();
                });
                return;
            }

            // 2. Carrega todas as categorias para o spinner
            loadedCategories = db.categoryDao().getAllCategories();
            spinCategoryNames.clear();
            spinCategoryNames.add("Selecione uma categoria..."); // Posição 0

            int posicaoCategoriaNoSpinner = 0;

            for (int i = 0; i < loadedCategories.size(); i++) {
                CategoryEntity cat = loadedCategories.get(i);
                spinCategoryNames.add(cat.getName());

                // 3. Verifica se esta é a categoria já salva na despesa
                if (cat.getId() == despesaAtual.getCategoriaId()) {
                    posicaoCategoriaNoSpinner = i + 1; // (i + 1 por causa do "Selecione...")
                }
            }

            // 4. Define a data selecionada para a data da despesa
            dataSelecionada.setTime(despesaAtual.getData());

            // 5. Atualiza a UI na thread principal com os dados carregados
            int finalPosicaoCategoria = posicaoCategoriaNoSpinner;
            runOnUiThread(() -> {
                txtValor.setText(String.format(Locale.US, "%.2f", despesaAtual.getValor()));
                txtDescricao.setText(despesaAtual.getDescricao());
                atualizarDataNoTextView();
                spinnerAdapter.notifyDataSetChanged();
                spinCategorias.setSelection(finalPosicaoCategoria); // Pré-seleciona a categoria
            });
        }).start();
    }

    private void salvarAlteracoes() {
        // --- Pega os novos dados da tela ---
        String valorStr = txtValor.getText().toString();
        String descricao = txtDescricao.getText().toString();
        int spinPosition = spinCategorias.getSelectedItemPosition();
        final java.util.Date dataParaSalvar = dataSelecionada.getTime();

        // --- Validação ---
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
            // Usar Locale.US para garantir que o ponto decimal seja lido corretamente
            valor = Double.parseDouble(valorStr.replace(",", "."));
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valor inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pega o ID da nova categoria selecionada
        CategoryEntity categoriaSelecionada = loadedCategories.get(spinPosition - 1);
        int categoriaId = categoriaSelecionada.getId();

        // --- Atualiza o objeto 'despesaAtual' ---
        despesaAtual.setValor(valor);
        despesaAtual.setDescricao(descricao);
        despesaAtual.setData(dataParaSalvar);
        despesaAtual.setCategoriaId(categoriaId);

        // --- Salva no banco (UPDATE) ---
        new Thread(() -> {
            db.expenseDao().updateExpense(despesaAtual); // <- Método UPDATE
            runOnUiThread(() -> {
                Toast.makeText(this, "Despesa atualizada!", Toast.LENGTH_SHORT).show();
                finish(); // Fecha a tela
            });
        }).start();
    }

    private void excluirDespesa() {
        if (despesaAtual == null) {
            Toast.makeText(this, "Erro: Despesa não carregada.", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            db.expenseDao().deleteExpense(despesaAtual); // <- Método DELETE
            runOnUiThread(() -> {
                Toast.makeText(DetalheDespesaActivity.this, "Despesa excluída", Toast.LENGTH_SHORT).show();
                finish(); // Fecha esta tela
            });
        }).start();
    }
}