package com.example.rastreadordespesas;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

// Importações da biblioteca de gráficos
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.animation.Easing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardFragment extends Fragment {

    private TextView txtTotalDashboard;
    private PieChart pieChart;
    private AppDatabase db;

    public DashboardFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtTotalDashboard = view.findViewById(R.id.txtTotalDashboard);
        pieChart = view.findViewById(R.id.pieChart);

        db = AppDatabase.getDatabase(getContext());

        configurarEstiloGrafico();
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarDadosDashboard();
    }

    private void configurarEstiloGrafico() {
        pieChart.setUsePercentValues(false);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);

        pieChart.setDragDecelerationFrictionCoef(0.95f);

        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setTransparentCircleRadius(61f);
        pieChart.setHoleRadius(58f);

        pieChart.setCenterText("Despesas");
        pieChart.setCenterTextSize(16f);

        pieChart.animateY(1400, Easing.EaseInOutQuad);

        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(true);
    }

    private void carregarDadosDashboard() {
        new Thread(() -> {
            double totalGasto = db.expenseDao().getTotalExpenseSum();

            List<ExpenseEntity> despesas = db.expenseDao().getAllExpenses();
            List<CategoryEntity> categorias = db.categoryDao().getAllCategories();

            Map<Integer, String> mapaNomes = new HashMap<>();
            for (CategoryEntity cat : categorias) {
                mapaNomes.put(cat.getId(), cat.getName());
            }

            Map<String, Float> somaPorCategoria = new HashMap<>();

            for (ExpenseEntity d : despesas) {
                String nomeCat = mapaNomes.get(d.getCategoriaId());
                if (nomeCat == null) nomeCat = "Outros";

                float valorAtual = somaPorCategoria.getOrDefault(nomeCat, 0f);
                somaPorCategoria.put(nomeCat, valorAtual + (float) d.getValor());
            }

            ArrayList<PieEntry> entradas = new ArrayList<>();
            for (Map.Entry<String, Float> entry : somaPorCategoria.entrySet()) {
                if (entry.getValue() > 0) {
                    entradas.add(new PieEntry(entry.getValue(), entry.getKey()));
                }
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    txtTotalDashboard.setText(String.format(Locale.getDefault(), "R$ %.2f", totalGasto));

                    atualizarDadosGrafico(entradas);
                });
            }
        }).start();
    }

    private void atualizarDadosGrafico(ArrayList<PieEntry> entradas) {
        if (entradas.isEmpty()) {
            pieChart.setCenterText("Sem gastos\nainda");
            pieChart.setData(null);
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entradas, "Categorias");

        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        ArrayList<Integer> cores = new ArrayList<>();

        for (int c : ColorTemplate.MATERIAL_COLORS) cores.add(c);
        for (int c : ColorTemplate.JOYFUL_COLORS) cores.add(c);
        for (int c : ColorTemplate.COLORFUL_COLORS) cores.add(c);
        for (int c : ColorTemplate.LIBERTY_COLORS) cores.add(c);
        for (int c : ColorTemplate.PASTEL_COLORS) cores.add(c);

        dataSet.setColors(cores);
        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.invalidate();
    }
}