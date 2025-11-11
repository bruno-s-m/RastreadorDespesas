package com.example.rastreadordespesas;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // --- Variáveis para Categorias ---
    private EditText txtCategoriaNome;
    private EditText txtCategoriaLimite;
    private Button btnSalvarCategoria;
    private ListView listviewCategorias;
    private ArrayAdapter<String> categoryAdapter;
    private ArrayList<String> categoryList;

    // Lista para guardar as entidades de Categoria (para o Spinner e Exclusão)
    private List<CategoryEntity> listaDeCategorias;

    // --- Variáveis para Despesas ---
    private EditText txtValorDespesa;
    private EditText txtDescricaoDespesa;
    private Spinner spinnerCategorias;
    private TextView txtDataDespesa;
    private Button btnSalvarDespesa;
    private ListView listViewDespesas;
    private ArrayAdapter<String> expenseAdapter;
    private ArrayList<String> expenseList;
    private Date dataSelecionada; // Armazena a data da despesa

    // --- Resumo do Orçamento ---
    private TextView txtResumoOrcamento;


    private AppDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // --- Inicialização do Banco de Dados ---
        db = AppDatabase.getDatabase(getApplicationContext());

        // --- Inicialização Categoria (Como já estava) ---
        txtCategoriaNome = findViewById(R.id.txtCategoriaNome);
        txtCategoriaLimite = findViewById(R.id.txtCategoriaLimite);
        btnSalvarCategoria = findViewById(R.id.btnSalvarCategoria);
        listviewCategorias = findViewById(R.id.listViewCategorias);
        categoryList = new ArrayList<>();
        listaDeCategorias = new ArrayList<>(); // Inicializa a nova lista
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categoryList);
        listviewCategorias.setAdapter(categoryAdapter);

        // --- Inicialização Despesa (Novo) ---
        txtValorDespesa = findViewById(R.id.txtValorDespesa);
        txtDescricaoDespesa = findViewById(R.id.txtDescricaoDespesa);
        spinnerCategorias = findViewById(R.id.spinnerCategorias);
        txtDataDespesa = findViewById(R.id.txtDataDespesa);
        btnSalvarDespesa = findViewById(R.id.btnSalvarDespesa);
        listViewDespesas = findViewById(R.id.listViewDespesas);
        expenseList = new ArrayList<>();
        expenseAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, expenseList);
        listViewDespesas.setAdapter(expenseAdapter);

        // --- Inicialização Resumo (Novo) ---
        txtResumoOrcamento = findViewById(R.id.txtResumoOrcamento);

        // --- Carregar Dados Iniciais ---
        carregarCategoriasDoBanco();
        carregarDespesasDoBanco();
        atualizarResumoOrcamento();

        // --- Listeners de Categoria ---
        btnSalvarCategoria.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarNovaCategoria();
            }
        });

        // --- Listener para Excluir/Editar Categoria (Novo - Requisito 3.2 CRUD) ---
        listviewCategorias.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                // Pega a categoria correta da nossa lista de entidades
                CategoryEntity categoriaSelecionada = listaDeCategorias.get(position);
                mostrarDialogoOpcoesCategoria(categoriaSelecionada);
                return true; // Indica que o clique longo foi consumido
            }
        });

        // --- Listeners de Despesa (Novo) ---
        btnSalvarDespesa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarNovaDespesa();
            }
        });

        // Listener para abrir o DatePicker (Requisito 3.1)
        txtDataDespesa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                abrirSeletorData();
            }
        });
    }

    // =================================================================================
    // --- MÉTODOS DE CATEGORIA (CRUD) ---
    // =================================================================================

    private void salvarNovaCategoria() {
        String nomeCategoria = txtCategoriaNome.getText().toString();
        String limiteCategoriaStr = txtCategoriaLimite.getText().toString();

        if (nomeCategoria.isEmpty()) {
            Toast.makeText(this, "Preencha o nome da categoria", Toast.LENGTH_SHORT).show();
            return;
        }

        double limiteCategoria = 0.0;
        if (!limiteCategoriaStr.isEmpty()) {
            try {
                limiteCategoria = Double.parseDouble(limiteCategoriaStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Limite Inválido. Use apenas números", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        final CategoryEntity novaCategoria = new CategoryEntity();
        novaCategoria.setName(nomeCategoria);
        novaCategoria.setLimiteMensal(limiteCategoria);

        new Thread(new Runnable() {
            @Override
            public void run() {
                db.categoryDao().insertCategory(novaCategoria);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, "Categoria salva com sucesso!", Toast.LENGTH_SHORT).show();
                        txtCategoriaNome.setText("");
                        txtCategoriaLimite.setText("");

                        // Recarrega as categorias na ListView E no Spinner
                        carregarCategoriasDoBanco();
                    }
                });
            }
        }).start();
    }

    private void carregarCategoriasDoBanco() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // Busca as entidades do banco
                listaDeCategorias = db.categoryDao().getAllCategories();

                // Limpa as listas antigas
                categoryList.clear();
                ArrayList<String> nomesSpinner = new ArrayList<>();

                // Preenche as listas com os novos dados
                for (CategoryEntity categoria : listaDeCategorias) {
                    String limiteFormatado = String.format("%.2f", categoria.getLimiteMensal());

                    // 1. Lista para a ListView (com limite)
                    categoryList.add(categoria.getName() + " (Limite: R$ " + limiteFormatado + " )");

                    // 2. Lista para o Spinner (apenas o nome)
                    nomesSpinner.add(categoria.getName());
                }

                // Adaptador do Spinner (precisa ser criado aqui)
                ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(MainActivity.this,
                        android.R.layout.simple_spinner_item, nomesSpinner);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Atualiza a ListView
                        categoryAdapter.notifyDataSetChanged();
                        // Atualiza o Spinner
                        spinnerCategorias.setAdapter(spinnerAdapter);
                    }
                });
            }
        }).start();

    }

    /**
     * NOVO: Mostra opções para Excluir ou Editar (Requisito 3.2 - U e D do CRUD)
     */
    private void mostrarDialogoOpcoesCategoria(CategoryEntity categoria) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opções para: " + categoria.getName());
        builder.setItems(new CharSequence[]{"Excluir", "Editar (Em breve...)"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: // Excluir
                        // Confirmação de exclusão
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Confirmar Exclusão")
                                .setMessage("Tem certeza que deseja excluir a categoria '" + categoria.getName() + "'? Todas as despesas associadas também serão excluídas.")
                                .setPositiveButton("Sim, Excluir", (d, w) -> excluirCategoria(categoria))
                                .setNegativeButton("Cancelar", null)
                                .show();
                        break;
                    case 1: // Editar
                        Toast.makeText(MainActivity.this, "Função de editar será implementada.", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        builder.show();
    }

    /**
     * NOVO: Lógica para excluir a categoria (Requisito 3.2 - Delete)
     */
    private void excluirCategoria(CategoryEntity categoria) {
        new Thread(() -> {
            db.categoryDao().deleteCategory(categoria);
            // Recarrega tudo na UI thread
            runOnUiThread(() -> {
                Toast.makeText(this, "Categoria excluída", Toast.LENGTH_SHORT).show();
                carregarCategoriasDoBanco(); // Recarrega categorias (lista e spinner)
                carregarDespesasDoBanco(); // Recarrega despesas (pois podem ter sido excluídas em cascata)
                atualizarResumoOrcamento(); // Atualiza o resumo
            });
        }).start();
    }


    // =================================================================================
    // --- MÉTODOS DE DESPESA (CRUD) --- (Requisito 3.1)
    // =================================================================================

    /**
     * NOVO: Abre o seletor de data
     */
    private void abrirSeletorData() {
        Calendar cal = Calendar.getInstance();
        int ano = cal.get(Calendar.YEAR);
        int mes = cal.get(Calendar.MONTH);
        int dia = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    // Seta a data selecionada
                    Calendar calSelecionado = Calendar.getInstance();
                    calSelecionado.set(year, month, dayOfMonth);
                    dataSelecionada = calSelecionado.getTime();

                    // Formata a data para exibir no TextView
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    txtDataDespesa.setText(sdf.format(dataSelecionada));

                }, ano, mes, dia);

        datePickerDialog.show();
    }

    /**
     * NOVO: Salva a nova despesa no banco (Requisito 3.1 - Create)
     */
    private void salvarNovaDespesa() {
        String valorStr = txtValorDespesa.getText().toString();
        String descricao = txtDescricaoDespesa.getText().toString();

        // Validação de Valor
        if (valorStr.isEmpty()) {
            Toast.makeText(this, "Preencha o valor da despesa", Toast.LENGTH_SHORT).show();
            return;
        }
        double valor = 0.0;
        try {
            valor = Double.parseDouble(valorStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validação de Data
        if (dataSelecionada == null) {
            Toast.makeText(this, "Selecione a data da despesa", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validação de Categoria
        if (spinnerCategorias.getSelectedItem() == null) {
            Toast.makeText(this, "Nenhuma categoria selecionada. Crie uma primeiro.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Pega o ID da categoria selecionada
        int posicaoSpinner = spinnerCategorias.getSelectedItemPosition();
        CategoryEntity categoriaSelecionada = listaDeCategorias.get(posicaoSpinner);
        int categoriaId = categoriaSelecionada.getId();

        // Cria a entidade de despesa
        final ExpenseEntity novaDespesa = new ExpenseEntity();
        novaDespesa.setValor(valor);
        novaDespesa.setDescricao(descricao);
        novaDespesa.setData(dataSelecionada);
        novaDespesa.setCategoriaId(categoriaId);

        new Thread(() -> {
            db.expenseDao().insertExpense(novaDespesa);

            runOnUiThread(() -> {
                Toast.makeText(this, "Despesa salva!", Toast.LENGTH_SHORT).show();
                // Limpa os campos
                txtValorDespesa.setText("");
                txtDescricaoDespesa.setText("");
                txtDataDespesa.setText(""); // Limpa o texto da data
                dataSelecionada = null; // Limpa a variável de data

                // Recarrega a lista de despesas e o resumo
                carregarDespesasDoBanco();
                atualizarResumoOrcamento();
            });
        }).start();
    }

    /**
     * NOVO: Carrega e exibe o histórico de despesas (Requisito 3.1 - Read)
     */
    private void carregarDespesasDoBanco() {
        new Thread(() -> {
            List<ExpenseEntity> despesasDoBanco = db.expenseDao().getAllExpenses();
            expenseList.clear();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy", Locale.getDefault());

            for (ExpenseEntity despesa : despesasDoBanco) {
                // Para exibir a categoria, precisamos buscá-la (idealmente, isso seria feito com um JOIN,
                // mas para simplificar, buscamos pelo ID)
                CategoryEntity categoria = db.categoryDao().getCategoryById(despesa.getCategoriaId());
                String nomeCategoria = (categoria != null) ? categoria.getName() : "Sem Categoria";

                String valorFormatado = String.format("%.2f", despesa.getValor());
                String dataFormatada = sdf.format(despesa.getData());

                expenseList.add(dataFormatada + " - " + nomeCategoria + " - R$ " + valorFormatado
                        + "\n(" + despesa.getDescricao() + ")");
            }

            runOnUiThread(() -> {
                expenseAdapter.notifyDataSetChanged();
            });
        }).start();
    }

    /**
     * NOVO: Calcula o total gasto por categoria no mês corrente (Requisitos 5.2 e 5.3)
     */
    private void atualizarResumoOrcamento() {
        new Thread(() -> {
            // 1. Define o início e o fim do mês corrente
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_MONTH, 1); // Primeiro dia do mês
            long inicioMes = cal.getTimeInMillis();

            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH)); // Último dia
            long fimMes = cal.getTimeInMillis();

            // 2. Busca todas as categorias
            List<CategoryEntity> todasCategorias = db.categoryDao().getAllCategories();
            StringBuilder resumo = new StringBuilder();

            for (CategoryEntity categoria : todasCategorias) {
                // 3. Para cada categoria, busca o total de gastos no período
                double totalGasto = db.expenseDao().getTotalExpensesForCategory(categoria.getId(), inicioMes, fimMes);
                double limite = categoria.getLimiteMensal();

                String gastoFormatado = String.format("%.2f", totalGasto);
                String limiteFormatado = String.format("%.2f", limite);

                // 4. Constrói a string de resumo
                resumo.append(categoria.getName() + ": R$ " + gastoFormatado + " de R$ " + limiteFormatado);

                // 5. Adiciona o Alerta Visual (Requisito 5.3)
                if (limite > 0 && totalGasto > limite) {
                    resumo.append(" (ESTOURADO!)"); // 🚨 Alerta Visual
                }
                resumo.append("\n");
            }

            if (resumo.length() == 0) {
                resumo.append("Nenhuma categoria cadastrada.");
            }

            // 6. Atualiza o TextView na UI Thread
            runOnUiThread(() -> {
                txtResumoOrcamento.setText(resumo.toString());
            });

        }).start();
    }
}