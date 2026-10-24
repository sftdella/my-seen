package com.della.myseen.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.della.myseen.R;
import com.della.myseen.adapter.ShowAdapter;
import com.della.myseen.database.ShowDAO;
import com.della.myseen.dialog.AddEditShowDialog;
import com.della.myseen.model.Show;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MyShowsFragment extends Fragment implements ShowAdapter.OnShowActionListener {

    private ShowDAO dao;
    private ShowAdapter adapter;
    private List<Show> currentList = new ArrayList<>();

    private TextView tvCountIngin, tvCountSedang, tvCountSudah, tvEmpty;
    private RecyclerView rvShows;
    private EditText etSearch;
    private TextView chipAll, chipIngin, chipSedang, chipSudah;

    private String currentFilter = "Semua";
    private String currentSearch = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_shows, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dao = new ShowDAO(requireContext());

        tvCountIngin = view.findViewById(R.id.tv_count_ingin);
        tvCountSedang = view.findViewById(R.id.tv_count_sedang);
        tvCountSudah = view.findViewById(R.id.tv_count_sudah);
        tvEmpty = view.findViewById(R.id.tv_empty);
        rvShows = view.findViewById(R.id.rv_shows);
        etSearch = view.findViewById(R.id.et_search);
        chipAll = view.findViewById(R.id.chip_all);
        chipIngin = view.findViewById(R.id.chip_ingin);
        chipSedang = view.findViewById(R.id.chip_sedang);
        chipSudah = view.findViewById(R.id.chip_sudah);

        adapter = new ShowAdapter(requireContext(), currentList, this);
        rvShows.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvShows.setAdapter(adapter);

        setupChips();
        setupSearch();
        refreshData();
    }

    private void setupChips() {
        chipAll.setOnClickListener(v -> setFilter("Semua"));
        chipIngin.setOnClickListener(v -> setFilter("Ingin Ditonton"));
        chipSedang.setOnClickListener(v -> setFilter("Sedang Ditonton"));
        chipSudah.setOnClickListener(v -> setFilter("Sudah Ditonton"));
    }

    private void setFilter(String filter) {
        currentFilter = filter;
        updateChipStyles();
        loadShows();
    }

    private void updateChipStyles() {
        chipAll.setBackgroundResource("Semua".equals(currentFilter) ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipAll.setTextColor(getResources().getColor("Semua".equals(currentFilter) ? R.color.white : R.color.text_primary, null));
        chipIngin.setBackgroundResource("Ingin Ditonton".equals(currentFilter) ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipIngin.setTextColor(getResources().getColor("Ingin Ditonton".equals(currentFilter) ? R.color.white : R.color.text_primary, null));
        chipSedang.setBackgroundResource("Sedang Ditonton".equals(currentFilter) ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipSedang.setTextColor(getResources().getColor("Sedang Ditonton".equals(currentFilter) ? R.color.white : R.color.text_primary, null));
        chipSudah.setBackgroundResource("Sudah Ditonton".equals(currentFilter) ? R.drawable.bg_chip_selected : R.drawable.bg_chip_unselected);
        chipSudah.setTextColor(getResources().getColor("Sudah Ditonton".equals(currentFilter) ? R.color.white : R.color.text_primary, null));
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentSearch = s.toString().trim();
                loadShows();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadShows() {
        List<Show> list;
        boolean hasSearch = !currentSearch.isEmpty();
        boolean hasFilter = !"Semua".equals(currentFilter);

        if (hasSearch && hasFilter) {
            list = dao.searchByStatus(currentSearch, currentFilter);
        } else if (hasSearch) {
            list = dao.search(currentSearch);
        } else if (hasFilter) {
            list = dao.getByStatus(currentFilter);
        } else {
            list = dao.getAll();
        }

        currentList = list;
        adapter.updateList(currentList);

        tvEmpty.setVisibility(currentList.isEmpty() ? View.VISIBLE : View.GONE);
        rvShows.setVisibility(currentList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void refreshData() {
        updateSummaryCards();
        loadShows();
    }

    private void updateSummaryCards() {
        Map<String, Integer> counts = dao.getCountByStatus();
        tvCountIngin.setText(String.valueOf(counts.getOrDefault("Ingin Ditonton", 0)));
        tvCountSedang.setText(String.valueOf(counts.getOrDefault("Sedang Ditonton", 0)));
        tvCountSudah.setText(String.valueOf(counts.getOrDefault("Sudah Ditonton", 0)));
    }

    public void openAddDialog() {
        AddEditShowDialog dialog = AddEditShowDialog.newInstance(null);
        dialog.setOnSavedListener(this::refreshData);
        dialog.show(getChildFragmentManager(), "AddShow");
    }

    @Override
    public void onEdit(Show show) {
        AddEditShowDialog dialog = AddEditShowDialog.newInstance(show);
        dialog.setOnSavedListener(this::refreshData);
        dialog.show(getChildFragmentManager(), "EditShow");
    }

    @Override
    public void onDelete(Show show) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_confirm_title)
                .setMessage(getString(R.string.delete_confirm_msg, show.getTitle()))
                .setPositiveButton(R.string.btn_delete, (d, w) -> {
                    dao.delete(show.getId());
                    refreshData();
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshData();
    }
}
