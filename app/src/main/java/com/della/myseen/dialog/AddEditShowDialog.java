package com.della.myseen.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.della.myseen.R;
import com.della.myseen.database.ShowDAO;
import com.della.myseen.model.Show;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddEditShowDialog extends BottomSheetDialogFragment {

    private static final String ARG_SHOW_ID = "show_id";
    private static final String ARG_SHOW_TITLE = "show_title";
    private static final String ARG_SHOW_GENRE = "show_genre";
    private static final String ARG_SHOW_STATUS = "show_status";
    private static final String ARG_SHOW_RATING = "show_rating";
    private static final String ARG_SHOW_DATE = "show_date";
    private static final String ARG_SHOW_TIME = "show_time";
    private static final String ARG_SHOW_NOTE = "show_note";

    private static final String[] GENRES = {
            "Action", "Drama", "Comedy", "Thriller", "Sci-Fi",
            "Animation", "Romance", "Horror", "Fantasy"
    };

    private static final String STATUS_INGIN = "Ingin Ditonton";
    private static final String STATUS_SEDANG = "Sedang Ditonton";
    private static final String STATUS_SUDAH = "Sudah Ditonton";

    private ShowDAO dao;
    private Runnable onSavedListener;

    private TextInputEditText etTitle, etDate, etTime, etNote;
    private MaterialAutoCompleteTextView actvGenre;
    private LinearLayout cardStatusIngin, cardStatusSedang, cardStatusSudah;
    private android.widget.RadioButton rbIngin, rbSedang, rbSudah;
    private LinearLayout llRatingInfo, llRatingSection, llRatingButtons;
    private ImageView ivRatingInfoIcon;
    private TextView tvRatingInfo, tvDialogTitle, tvDialogSubtitle;
    private MaterialButton btnSave, btnCancel;
    private ImageButton btnBack;

    private String selectedStatus = STATUS_INGIN;
    private int selectedRating = 0;
    private Show editShow = null;

    public static AddEditShowDialog newInstance(@Nullable Show show) {
        AddEditShowDialog dialog = new AddEditShowDialog();
        Bundle args = new Bundle();
        if (show != null) {
            args.putInt(ARG_SHOW_ID, show.getId());
            args.putString(ARG_SHOW_TITLE, show.getTitle());
            args.putString(ARG_SHOW_GENRE, show.getGenre());
            args.putString(ARG_SHOW_STATUS, show.getStatus());
            args.putInt(ARG_SHOW_RATING, show.getRating() != null ? show.getRating() : 0);
            args.putString(ARG_SHOW_DATE, show.getAddedDate());
            args.putString(ARG_SHOW_TIME, show.getAddedTime());
            args.putString(ARG_SHOW_NOTE, show.getNote() != null ? show.getNote() : "");
        }
        dialog.setArguments(args);
        return dialog;
    }

    public void setOnSavedListener(Runnable listener) {
        this.onSavedListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        dialog.setOnShowListener(d -> {
            BottomSheetDialog bsd = (BottomSheetDialog) d;
            FrameLayout bottomSheet = bsd.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                ViewGroup.LayoutParams lp = bottomSheet.getLayoutParams();
                lp.height = ViewGroup.LayoutParams.MATCH_PARENT;
                bottomSheet.setLayoutParams(lp);
                BottomSheetBehavior<FrameLayout> behavior = bsd.getBehavior();
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                behavior.setSkipCollapsed(true);
                behavior.setDraggable(false);
            }
        });
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_edit_show, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        dao = new ShowDAO(requireContext());

        bindViews(view);
        setupGenreDropdown();
        setupStatusCards();
        setupDateTimePickers();
        setupButtons();
        setupRatingButtons();

        Bundle args = getArguments();
        if (args != null && args.containsKey(ARG_SHOW_ID)) {
            loadEditData(args);
        } else {
            setDefaultDateAndTime();
            updateStatusUI(STATUS_INGIN);
        }
    }

    private void bindViews(View view) {
        etTitle = view.findViewById(R.id.et_title);
        actvGenre = view.findViewById(R.id.actv_genre);
        cardStatusIngin = view.findViewById(R.id.card_status_ingin);
        cardStatusSedang = view.findViewById(R.id.card_status_sedang);
        cardStatusSudah = view.findViewById(R.id.card_status_sudah);
        rbIngin = view.findViewById(R.id.rb_ingin);
        rbSedang = view.findViewById(R.id.rb_sedang);
        rbSudah = view.findViewById(R.id.rb_sudah);
        llRatingInfo = view.findViewById(R.id.ll_rating_info);
        llRatingSection = view.findViewById(R.id.ll_rating_section);
        llRatingButtons = view.findViewById(R.id.ll_rating_buttons);
        ivRatingInfoIcon = view.findViewById(R.id.iv_rating_info_icon);
        tvRatingInfo = view.findViewById(R.id.tv_rating_info);
        tvDialogTitle = view.findViewById(R.id.tv_dialog_title);
        tvDialogSubtitle = view.findViewById(R.id.tv_dialog_subtitle);
        etDate = view.findViewById(R.id.et_date);
        etTime = view.findViewById(R.id.et_time);
        etNote = view.findViewById(R.id.et_note);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
        btnBack = view.findViewById(R.id.btn_back);
    }

    private void setupGenreDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(), android.R.layout.simple_dropdown_item_1line, GENRES);
        actvGenre.setAdapter(adapter);
        actvGenre.setThreshold(0);
        actvGenre.setOnClickListener(v -> actvGenre.showDropDown());
    }

    private void setupStatusCards() {
        View.OnClickListener listener = v -> {
            if (v.getId() == R.id.card_status_ingin) selectStatus(STATUS_INGIN);
            else if (v.getId() == R.id.card_status_sedang) selectStatus(STATUS_SEDANG);
            else selectStatus(STATUS_SUDAH);
        };
        cardStatusIngin.setOnClickListener(listener);
        cardStatusSedang.setOnClickListener(listener);
        cardStatusSudah.setOnClickListener(listener);
        rbIngin.setOnClickListener(v -> selectStatus(STATUS_INGIN));
        rbSedang.setOnClickListener(v -> selectStatus(STATUS_SEDANG));
        rbSudah.setOnClickListener(v -> selectStatus(STATUS_SUDAH));
    }

    private void selectStatus(String status) {
        boolean wasSudah = STATUS_SUDAH.equals(selectedStatus);
        selectedStatus = status;
        updateStatusUI(status);
        if (STATUS_SUDAH.equals(status) && !wasSudah) {
            showRatingDialog();
        }
    }

    private void updateStatusUI(String status) {
        rbIngin.setChecked(STATUS_INGIN.equals(status));
        rbSedang.setChecked(STATUS_SEDANG.equals(status));
        rbSudah.setChecked(STATUS_SUDAH.equals(status));

        cardStatusIngin.setBackgroundResource(STATUS_INGIN.equals(status)
                ? R.drawable.bg_status_card_ingin : R.drawable.bg_status_card_normal);
        cardStatusSedang.setBackgroundResource(STATUS_SEDANG.equals(status)
                ? R.drawable.bg_status_card_sedang : R.drawable.bg_status_card_normal);
        cardStatusSudah.setBackgroundResource(STATUS_SUDAH.equals(status)
                ? R.drawable.bg_status_card_sudah : R.drawable.bg_status_card_normal);

        // Update card text colors
        updateCardTextColor(cardStatusIngin, STATUS_INGIN.equals(status)
                ? R.color.ingin_ditonton : R.color.text_secondary);
        updateCardTextColor(cardStatusSedang, STATUS_SEDANG.equals(status)
                ? R.color.sedang_ditonton : R.color.text_secondary);
        updateCardTextColor(cardStatusSudah, STATUS_SUDAH.equals(status)
                ? R.color.sudah_ditonton : R.color.text_secondary);

        if (STATUS_SUDAH.equals(status)) {
            llRatingInfo.setVisibility(View.VISIBLE);
            llRatingInfo.setBackgroundResource(R.drawable.bg_info_sudah);
            ivRatingInfoIcon.setImageResource(R.drawable.ic_check_circle);
            tvRatingInfo.setText(R.string.rating_info_open);
            tvRatingInfo.setTextColor(requireContext().getColor(R.color.sudah_ditonton));
            llRatingSection.setVisibility(View.VISIBLE);
        } else {
            llRatingSection.setVisibility(View.GONE);
            if (STATUS_SEDANG.equals(status)) {
                llRatingInfo.setBackgroundResource(R.drawable.bg_info_sedang);
                ivRatingInfoIcon.setImageResource(R.drawable.ic_play_circle);
                tvRatingInfo.setTextColor(requireContext().getColor(R.color.sedang_ditonton));
            } else {
                llRatingInfo.setBackgroundResource(R.drawable.bg_info_ingin);
                ivRatingInfoIcon.setImageResource(R.drawable.ic_hourglass);
                tvRatingInfo.setTextColor(requireContext().getColor(R.color.ingin_ditonton));
            }
            llRatingInfo.setVisibility(View.VISIBLE);
            tvRatingInfo.setText(R.string.rating_info_locked);
            selectedRating = 0;
            updateRatingButtonsUI(0);
        }
    }

    private void updateCardTextColor(LinearLayout card, int colorRes) {
        for (int i = 0; i < card.getChildCount(); i++) {
            View child = card.getChildAt(i);
            if (child instanceof TextView) {
                ((TextView) child).setTextColor(requireContext().getColor(colorRes));
            }
        }
    }

    private void setupRatingButtons() {
        llRatingButtons.removeAllViews();
        for (int i = 1; i <= 10; i++) {
            final int rating = i;
            TextView btn = new TextView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    (int) (36 * getResources().getDisplayMetrics().density));
            params.weight = 1;
            params.setMarginEnd(2);
            btn.setLayoutParams(params);
            btn.setText(String.valueOf(i));
            btn.setGravity(android.view.Gravity.CENTER);
            btn.setTextSize(13f);
            btn.setId(View.generateViewId());
            btn.setTag(i);
            btn.setOnClickListener(v -> {
                selectedRating = rating;
                updateRatingButtonsUI(rating);
                showRatingDialog();
            });
            llRatingButtons.addView(btn);
        }
        updateRatingButtonsUI(selectedRating);
    }

    private void updateRatingButtonsUI(int rating) {
        for (int i = 0; i < llRatingButtons.getChildCount(); i++) {
            View child = llRatingButtons.getChildAt(i);
            if (child instanceof TextView) {
                TextView btn = (TextView) child;
                int btnRating = (Integer) btn.getTag();
                boolean selected = btnRating == rating && rating > 0;
                if (selected) {
                    btn.setBackgroundResource(R.drawable.bg_rating_selected);
                    btn.setTextColor(requireContext().getColor(R.color.white));
                    btn.setTypeface(null, android.graphics.Typeface.BOLD);
                } else {
                    btn.setBackgroundResource(R.drawable.bg_rating_unselected);
                    btn.setTextColor(requireContext().getColor(R.color.text_primary));
                    btn.setTypeface(null, android.graphics.Typeface.NORMAL);
                }
            }
        }
    }

    private void showRatingDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_rating, null);
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(requireContext());
        builder.setView(dialogView);
        android.app.AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        LinearLayout llButtons = dialogView.findViewById(R.id.ll_rating_dialog_buttons);
        MaterialButton btnBatal = dialogView.findViewById(R.id.btn_rating_cancel);
        MaterialButton btnSimpan = dialogView.findViewById(R.id.btn_rating_save);
        final int[] tempRating = {selectedRating > 0 ? selectedRating : 8};

        // Populate rating buttons programmatically
        float dp = getResources().getDisplayMetrics().density;
        for (int i = 1; i <= 10; i++) {
            final int rating = i;
            TextView btn = new TextView(requireContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, (int) (36 * dp));
            params.weight = 1;
            params.setMarginEnd(2);
            btn.setLayoutParams(params);
            btn.setText(String.valueOf(i));
            btn.setGravity(android.view.Gravity.CENTER);
            btn.setTextSize(12f);
            btn.setTag(i);
            btn.setOnClickListener(v -> {
                tempRating[0] = rating;
                updateDialogRatingUI(llButtons, rating);
            });
            llButtons.addView(btn);
        }
        updateDialogRatingUI(llButtons, tempRating[0]);

        btnBatal.setOnClickListener(v -> dialog.dismiss());
        btnSimpan.setOnClickListener(v -> {
            selectedRating = tempRating[0];
            updateRatingButtonsUI(selectedRating);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showSimpleRatingDialog() {
        String[] ratingOptions = {"1","2","3","4","5","6","7","8","9","10"};
        int checkedIdx = selectedRating > 0 ? selectedRating - 1 : 7;
        new android.app.AlertDialog.Builder(requireContext())
                .setTitle(R.string.rating_dialog_title)
                .setSingleChoiceItems(ratingOptions, checkedIdx, (d, which) -> {
                    selectedRating = which + 1;
                })
                .setPositiveButton(R.string.btn_save_rating, (d, w) -> {
                    updateRatingButtonsUI(selectedRating);
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    private void updateDialogRatingUI(LinearLayout container, int rating) {
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof TextView) {
                TextView btn = (TextView) child;
                int r = (int) btn.getTag();
                boolean selected = r == rating && rating > 0;
                if (selected) {
                    btn.setBackgroundResource(R.drawable.bg_rating_selected);
                    btn.setTextColor(requireContext().getColor(R.color.white));
                } else {
                    btn.setBackgroundResource(R.drawable.bg_rating_unselected);
                    btn.setTextColor(requireContext().getColor(R.color.text_primary));
                }
            }
        }
    }

    private void setupDateTimePickers() {
        etDate.setOnClickListener(v -> showDatePicker());
        etTime.setOnClickListener(v -> showTimePicker());
    }

    private void showDatePicker() {
        Calendar cal = Calendar.getInstance();
        String existing = etDate.getText() != null ? etDate.getText().toString() : "";
        // Try to parse existing date for initial picker value
        new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(year, month, dayOfMonth);
                    String formatted = formatDate(selected.getTime());
                    etDate.setText(formatted);
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).show();
    }

    private void showTimePicker() {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> {
                    String formatted = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                    etTime.setText(formatted);
                },
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true
        ).show();
    }

    private String formatDate(Date date) {
        String[] bulan = {"Januari","Februari","Maret","April","Mei","Juni",
                          "Juli","Agustus","September","Oktober","November","Desember"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.DAY_OF_MONTH) + " " +
               bulan[cal.get(Calendar.MONTH)] + " " +
               cal.get(Calendar.YEAR);
    }

    private void setDefaultDateAndTime() {
        Calendar cal = Calendar.getInstance();
        etDate.setText(formatDate(cal.getTime()));
        etTime.setText(String.format(Locale.getDefault(), "%02d:%02d",
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE)));
    }

    private void setupButtons() {
        btnBack.setOnClickListener(v -> dismiss());
        btnCancel.setOnClickListener(v -> dismiss());
        btnSave.setOnClickListener(v -> saveShow());
    }

    private void saveShow() {
        String title = etTitle.getText() != null ? etTitle.getText().toString().trim() : "";
        String genre = actvGenre.getText() != null ? actvGenre.getText().toString().trim() : "";
        String date = etDate.getText() != null ? etDate.getText().toString().trim() : "";
        String time = etTime.getText() != null ? etTime.getText().toString().trim() : "";
        String note = etNote.getText() != null ? etNote.getText().toString().trim() : "";

        if (title.isEmpty()) {
            Toast.makeText(requireContext(), R.string.validation_title_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (genre.isEmpty()) {
            Toast.makeText(requireContext(), R.string.validation_genre_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (date.isEmpty()) {
            Toast.makeText(requireContext(), R.string.validation_date_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (time.isEmpty()) {
            Toast.makeText(requireContext(), R.string.validation_time_empty, Toast.LENGTH_SHORT).show();
            return;
        }
        if (STATUS_SUDAH.equals(selectedStatus) && selectedRating == 0) {
            Toast.makeText(requireContext(), R.string.validation_rating_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        Integer rating = STATUS_SUDAH.equals(selectedStatus) ? selectedRating : null;
        long ts = System.currentTimeMillis() / 1000L;

        Show show = new Show(title, genre, selectedStatus, rating, date, time,
                note.isEmpty() ? null : note, ts);

        if (editShow != null) {
            show.setId(editShow.getId());
            show.setAddedTimestamp(editShow.getAddedTimestamp());
            dao.update(show);
        } else {
            dao.insert(show);
        }

        if (onSavedListener != null) onSavedListener.run();
        dismiss();
    }

    private void loadEditData(Bundle args) {
        editShow = new Show();
        editShow.setId(args.getInt(ARG_SHOW_ID));
        editShow.setTitle(args.getString(ARG_SHOW_TITLE, ""));
        editShow.setGenre(args.getString(ARG_SHOW_GENRE, ""));
        editShow.setStatus(args.getString(ARG_SHOW_STATUS, STATUS_INGIN));
        int r = args.getInt(ARG_SHOW_RATING, 0);
        editShow.setRating(r > 0 ? r : null);
        editShow.setAddedDate(args.getString(ARG_SHOW_DATE, ""));
        editShow.setAddedTime(args.getString(ARG_SHOW_TIME, ""));
        editShow.setNote(args.getString(ARG_SHOW_NOTE, ""));

        tvDialogTitle.setText(R.string.edit_show_title);
        tvDialogSubtitle.setText(R.string.edit_show_subtitle);

        etTitle.setText(editShow.getTitle());
        actvGenre.setText(editShow.getGenre(), false);
        etDate.setText(editShow.getAddedDate());
        etTime.setText(editShow.getAddedTime());
        etNote.setText(editShow.getNote());

        selectedStatus = editShow.getStatus();
        selectedRating = r;

        updateStatusUI(selectedStatus);
        updateRatingButtonsUI(selectedRating);
    }
}
