package com.example.rastreadordespesas;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View; // IMPORTANTE: Adicionar View
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

    private EditText txtCategoriaNome;
    private EditText txtCategoriaLimite;
    private Button btnSalvarCategoria;
    private ListView listviewCategorias;
    private ArrayAdapter<String> categoryAdapter;
    private ArrayList<String> categoryList;
    private List<CategoryEntity> listaDeCategorias;
    private EditText txtValorDespesa;
    private EditText txtDescricaoDespesa;
    private Spinner spinnerCategorias;
    private TextView txtDataDespesa;
    private Button btnSalvarDespesa;
    private Date dataSelecionada;
    private Button btnVerResumo;
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

        // --- Inicialização Categoria ---
        txtCategoriaNome = findViewById(R.id.txtCategoriaNome);
        txtCategoriaLimite = findViewById(R.id.txtCategoriaLimite);
        btnSalvarCategoria = findViewById(R.id.btnSalvarCategoria);
        listviewCategorias = findViewById(R.id.listViewCategorias);
        categoryList = new ArrayList<>();
        listaDeCategorias = new ArrayList<>();
        categoryAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, categoryList);
        listviewCategorias.setAdapter(categoryAdapter);

        // --- Inicialização Despesa ---
        txtValorDespesa = findViewById(R.id.txtValorDespesa);
        txtDescricaoDespesa = findViewById(R.id.txtDescricaoDespesa);
        spinnerCategorias = findViewById(R.id.spinnerCategorias);
        txtDataDespesa = findViewById(R.id.txtDataDespesa);
        btnSalvarDespesa = findViewById(R.id.btnSalvarDespesa);
        btnVerResumo = findViewById(R.id.btnVerResumo);


        // --- Carregar Dados Iniciais ---
        carregarCategoriasDoBanco();

        // --- Listeners de Categoria ---
        btnSalvarCategoria.setOnClickListener(v -> salvarNovaCategoria());

        listviewCategorias.setOnItemLongClickListener((parent, view, position, id) -> {
            CategoryEntity categoriaSelecionada = listaDeCategorias.get(position);
            mostrarDialogoOpcoesCategoria(categoriaSelecionada);
            return true;
        });

        // --- Listeners de Despesa ---
        btnSalvarDespesa.setOnClickListener(v -> salvarNovaDespesa());
        txtDataDespesa.setOnClickListener(v -> abrirSeletorData());

        // --- Listener de Navegação (Novo) ---
        btnVerResumo.setOnClickListener(v -> {
            // Cria uma Intent para abrir a ResumoActivity
            Intent intent = new Intent(MainActivity.this, ResumoActivity.class);
            startActivity(intent);
        });
    }

    // =================================================================================
    // --- MÉTODOS DE CATEGORIA (CRUD) ---
    // =================================================================================

    private void salvarNovaCategoria() {
        // ... (Este método está correto, sem alterações)
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

        new Thread(() -> {
            db.categoryDao().insertCategory(novaCategoria);
            runOnUiThread(() -> {
                Toast.makeText(MainActivity.this, "Categoria salva com sucesso!", Toast.LENGTH_SHORT).show();
                txtCategoriaNome.setText("");
                txtCategoriaLimite.setText("");
                carregarCategoriasDoBanco();
            });
        }).start();
    }

    private void carregarCategoriasDoBanco() {
        // ... (Este método está correto, sem alterações)
        new Thread(() -> {
            listaDeCategorias = db.categoryDao().getAllCategories();
            categoryList.clear();
            ArrayList<String> nomesSpinner = new ArrayList<>();

            for (CategoryEntity categoria : listaDeCategorias) {
                String limiteFormatado = String.format("%.2f", categoria.getLimiteMensal());
                categoryList.add(categoria.getName() + " (Limite: R$ " + limiteFormatado + " )");
                nomesSpinner.add(categoria.getName());
            }

            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(MainActivity.this,
                    android.R.layout.simple_spinner_item, nomesSpinner);
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            runOnUiThread(() -> {
                categoryAdapter.notifyDataSetChanged();
                spinnerCategorias.setAdapter(spinnerAdapter);
            });
        }).start();
    }

    // MODIFICADO: Este método agora inclui a opção "Editar"
    private void mostrarDialogoOpcoesCategoria(CategoryEntity categoria) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Opções para: " + categoria.getName());

        // MODIFICADO: A opção "Editar" agora está funcional
        builder.setItems(new CharSequence[]{"Excluir", "Editar"}, (dialog, which) -> {
            switch (which) {
                case 0: // Excluir
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Confirmar Exclusão")
                            .setMessage("Tem certeza que deseja excluir a categoria '" + categoria.getName() + "'? Todas as despesas associadas também serão excluídas.")
                            .setPositiveButton("Sim, Excluir", (d, w) -> excluirCategoria(categoria))
                            .setNegativeButton("Cancelar", null)
                            .show();
                    break;
                case 1: // Editar
                    // NOVO: Chama o diálogo de edição
                    mostrarDialogoEditarCategoria(categoria);
                    break;
            }
        });
        builder.show();
    }

    /**
     * NOVO: Método para mostrar o diálogo de edição usando o XML "form_edit_categoria.xml"
     */
    private void mostrarDialogoEditarCategoria(CategoryEntity categoria) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Categoria");

        // Infla o layout customizado (form_edit_categoria.xml)
        View dialogView = getLayoutInflater().inflate(R.layout.form_edit_categoria, null);
        builder.setView(dialogView);

        // Pega as referências dos componentes do diálogo
        EditText editNome = dialogView.findViewById(R.id.editCategoriaNome);
        EditText editLimite = dialogView.findViewById(R.id.editCategoriaLimite);

        // Preenche os campos com os dados existentes
        editNome.setText(categoria.getName());
        editLimite.setText(String.format(Locale.US, "%.2f", categoria.getLimiteMensal()));

        // Configura o botão "Salvar"
        builder.setPositiveButton("Salvar", (dialog, which) -> {
            String novoNome = editNome.getText().toString();
            String novoLimiteStr = editLimite.getText().toString();

            if (novoNome.isEmpty()) {
                Toast.makeText(this, "O nome não pode ser vazio", Toast.LENGTH_SHORT).show();
                return;
            }

            double novoLimite = 0.0;
            if (!novoLimiteStr.isEmpty()) {
                try {
                    novoLimite = Double.parseDouble(novoLimiteStr);
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Limite inválido", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Atualiza o objeto original
            categoria.setName(novoNome);
            categoria.setLimiteMensal(novoLimite);

            // Salva no banco de dados
            atualizarCategoria(categoria);
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    /**
     * NOVO: Método auxiliar para salvar a categoria atualizada no banco
     */
    private void atualizarCategoria(CategoryEntity categoria) {
        new Thread(() -> {
            db.categoryDao().updateCategory(categoria);
            runOnUiThread(() -> {
                Toast.makeText(this, "Categoria atualizada!", Toast.LENGTH_SHORT).show();
                carregarCategoriasDoBanco(); // Atualiza a lista e o spinner
            });
        }).start();
    }


    private void excluirCategoria(CategoryEntity categoria) {
        // ... (Este método está correto, sem alterações)
        new Thread(() -> {
            db.categoryDao().deleteCategory(categoria);
            runOnUiThread(() -> {
                Toast.makeText(this, "Categoria excluída", Toast.LENGTH_SHORT).show();
                carregarCategoriasDoBanco();
            });
        }).start();
    }


    // =================================================================================
    // --- MÉTODOS DE DESPESA (CRUD) ---
    // (Sem alterações aqui)
    // =================================================================================

    private void abrirSeletorData() {
        // ... (Este método está correto, sem alterações)
        Calendar cal = Calendar.getInstance();
        int ano = cal.get(Calendar.YEAR);
        int mes = cal.get(Calendar.MONTH);
        int dia = cal.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    Calendar calSelecionado = Calendar.getInstance();
                    calSelecionado.set(year, month, dayOfMonth);
                    dataSelecionada = calSelecionado.getTime();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    txtDataDespesa.setText(sdf.format(dataSelecionada));

                }, ano, mes, dia);

        datePickerDialog.show();
    }

    private void salvarNovaDespesa() {
        // ... (Este método está correto, sem alterações)
        String valorStr = txtValorDespesa.getText().toString();
        String descricao = txtDescricaoDespesa.getText().toString();

        if (valorStr.isEmpty()) {
            Toast.makeText(this, "Preencha o valor da despesa", Toast.LENGTH_SHORT).show();
            return;
        }
        double valor;
        try {
            valor = Double.parseDouble(valorStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Valor inválido", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dataSelecionada == null) {
            Toast.makeText(this, "Selecione a data da despesa", Toast.LENGTH_SHORT).show();
            return;
        }

        if (spinnerCategorias.getSelectedItem() == null) {
            Toast.makeText(this, "Nenhuma categoria selecionada. Crie uma primeiro.", Toast.LENGTH_SHORT).show();
            return;
        }

        int posicaoSpinner = spinnerCategorias.getSelectedItemPosition();
        CategoryEntity categoriaSelecionada = listaDeCategorias.get(posicaoSpinner);
        int categoriaId = categoriaSelecionada.getId();

        final ExpenseEntity novaDespesa = new ExpenseEntity();
        novaDespesa.setValor(valor);
        novaDespesa.setDescricao(descricao);
        novaDespesa.setData(dataSelecionada);
        novaDespesa.setCategoriaId(categoriaId);

        new Thread(() -> {
            db.expenseDao().insertExpense(novaDespesa);

            runOnUiThread(() -> {
                Toast.makeText(this, "Despesa salva!", Toast.LENGTH_SHORT).show();
                txtValorDespesa.setText("");
                txtDescricaoDespesa.setText("");
                txtDataDespesa.setText("");
                dataSelecionada = null;
            });
        }).start();
    }
}