package com.della.myseen.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.della.myseen.R;
import com.della.myseen.database.ShowDAO;
import com.della.myseen.model.Show;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private ShowDAO dao;
    private PieChart pieChart;
    private BarChart barChart;
    private TextView tvTotal, tvStatIngin, tvStatSedang, tvStatSudah;
    private TextView tvAvgRating, tvFromCount;
    private LinearLayout llTop5, llRecent, llStars;
    private Spinner spinnerFilter;

    private final String[] filterOptions = {"Semua Waktu", "Hari Ini", "Minggu Ini", "Bulan Ini"};
    private String selectedFilter = "Semua Waktu";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dao = new ShowDAO(requireContext());

        tvTotal = view.findViewById(R.id.tv_stat_total);
        tvStatIngin = view.findViewById(R.id.tv_stat_ingin);
        tvStatSedang = view.findViewById(R.id.tv_stat_sedang);
        tvStatSudah = view.findViewById(R.id.tv_stat_sudah);
        tvAvgRating = view.findViewById(R.id.tv_avg_rating);
        tvFromCount = view.findViewById(R.id.tv_from_count);
        llTop5 = view.findViewById(R.id.ll_top5);
        llRecent = view.findViewById(R.id.ll_recent);
        llStars = view.findViewById(R.id.ll_stars);
        pieChart = view.findViewById(R.id.pie_chart);
        barChart = view.findViewById(R.id.bar_chart);
        spinnerFilter = view.findViewById(R.id.spinner_time_filter);

        setupSpinner();
        loadData();
    }

    private void setupSpinner() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_spinner_item, filterOptions);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(spinnerAdapter);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedFilter = filterOptions[position];
                loadData();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private long getFromTimestamp() {
        long now = System.currentTimeMillis() / 1000L;
        switch (selectedFilter) {
            case "Hari Ini":
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                return cal.getTimeInMillis() / 1000L;
            case "Minggu Ini":
                return now - (7L * 24 * 60 * 60);
            case "Bulan Ini":
                return now - (30L * 24 * 60 * 60);
            default:
                return 0;
        }
    }

    private void loadData() {
        long fromTs = getFromTimestamp();
        Map<String, Integer> counts = dao.getCountByStatusFiltered(fromTs);
        Map<String, Integer> genreCounts = dao.getCountByGenreFiltered(fromTs);

        int ingin = counts.getOrDefault("Ingin Ditonton", 0);
        int sedang = counts.getOrDefault("Sedang Ditonton", 0);
        int sudah = counts.getOrDefault("Sudah Ditonton", 0);
        int total = ingin + sedang + sudah;

        tvTotal.setText(String.valueOf(total));
        tvStatIngin.setText(String.valueOf(ingin));
        tvStatSedang.setText(String.valueOf(sedang));
        tvStatSudah.setText(String.valueOf(sudah));

        setupPieChart(ingin, sedang, sudah, total);
        setupBarChart(genreCounts);
        setupRatingSection();
        setupTop5();
        setupRecentActivity();
    }

    private void setupPieChart(int ingin, int sedang, int sudah, int total) {
        List<PieEntry> entries = new ArrayList<>();
        if (ingin > 0) entries.add(new PieEntry(ingin, "Ingin Ditonton"));
        if (sedang > 0) entries.add(new PieEntry(sedang, "Sedang Ditonton"));
        if (sudah > 0) entries.add(new PieEntry(sudah, "Sudah Ditonton"));

        if (entries.isEmpty()) {
            pieChart.setNoDataText("Belum ada data");
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(
                Color.parseColor("#FFA500"),
                Color.parseColor("#2196F3"),
                Color.parseColor("#4CAF50")
        );
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setSliceSpace(3f);

        PieData pieData = new PieData(dataSet);
        pieData.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format("%.1f%%", value);
            }
        });

        pieChart.setData(pieData);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(55f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setCenterText(total + "\nTotal");
        pieChart.setCenterTextSize(14f);
        pieChart.setCenterTextColor(Color.parseColor("#1A1A2E"));
        pieChart.getDescription().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        pieChart.setUsePercentValues(true);

        Legend legend = pieChart.getLegend();
        legend.setEnabled(true);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);
        legend.setDrawInside(false);
        legend.setTextSize(11f);

        pieChart.animateY(800);
        pieChart.invalidate();
    }

    private void setupBarChart(Map<String, Integer> genreCounts) {
        List<BarEntry> entries = new ArrayList<>();
        final List<String> labels = new ArrayList<>(genreCounts.keySet());

        for (int i = 0; i < labels.size(); i++) {
            entries.add(new BarEntry(i, genreCounts.get(labels.get(i))));
        }

        if (entries.isEmpty()) {
            barChart.setNoDataText("Belum ada data");
            barChart.invalidate();
            return;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Genre");
        dataSet.setColor(Color.parseColor("#6C63FF"));
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.parseColor("#1A1A2E"));
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.6f);

        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setFitBars(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setDrawGridLines(false);
        xAxis.setTextSize(10f);

        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setGranularity(1f);
        barChart.getAxisRight().setEnabled(false);

        barChart.animateY(800);
        barChart.invalidate();
    }

    private void setupRatingSection() {
        double avg = dao.getAverageRating();
        int sudahCount = dao.getSudahCount();

        tvAvgRating.setText(String.format("%.1f", avg));
        tvFromCount.setText("Dari " + sudahCount + " tontonan yang sudah ditonton");

        // Draw star row (5 stars, filled based on avg/2)
        llStars.removeAllViews();
        float starScore = (float) (avg / 2.0); // Convert to 5-star scale
        for (int i = 1; i <= 5; i++) {
            ImageView star = new ImageView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(20, 20);
            params.setMarginEnd(2);
            star.setLayoutParams(params);
            star.setImageResource(R.drawable.ic_star);
            star.setAlpha(i <= Math.round(starScore) ? 1.0f : 0.25f);
            llStars.addView(star);
        }
    }

    private void setupTop5() {
        List<Show> top5 = dao.getTop5ByRating();
        llTop5.removeAllViews();
        for (int i = 0; i < top5.size(); i++) {
            Show show = top5.get(i);
            View item = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_top_show, llTop5, false);
            ((TextView) item.findViewById(R.id.tv_rank)).setText(String.valueOf(i + 1));
            ((TextView) item.findViewById(R.id.tv_title)).setText(show.getTitle());
            String ratingStr = show.getRating() != null
                    ? String.format("%.1f", show.getRating().doubleValue()) : "-";
            ((TextView) item.findViewById(R.id.tv_rating)).setText(ratingStr);
            llTop5.addView(item);
        }
    }

    private void setupRecentActivity() {
        List<Show> recent = dao.getRecentActivity(5);
        llRecent.removeAllViews();
        for (Show show : recent) {
            View item = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_recent_activity, llRecent, false);

            ImageView icon = item.findViewById(R.id.iv_status_icon);
            TextView tvActivity = item.findViewById(R.id.tv_activity);
            TextView tvDate = item.findViewById(R.id.tv_date);

            String activity;
            switch (show.getStatus()) {
                case "Ingin Ditonton":
                    icon.setImageResource(R.drawable.ic_hourglass);
                    activity = "Menambahkan \"" + show.getTitle() + "\" ke Ingin Ditonton";
                    break;
                case "Sedang Ditonton":
                    icon.setImageResource(R.drawable.ic_play_circle);
                    activity = "Menandai \"" + show.getTitle() + "\" sebagai Sedang Ditonton";
                    break;
                default:
                    icon.setImageResource(R.drawable.ic_check_circle);
                    activity = "Menandai \"" + show.getTitle() + "\" sebagai Sudah Ditonton";
                    break;
            }

            tvActivity.setText(activity);
            tvDate.setText(show.getAddedDate() + "\n" + show.getAddedTime());
            llRecent.addView(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }
}
