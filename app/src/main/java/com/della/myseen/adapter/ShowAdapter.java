package com.della.myseen.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.della.myseen.R;
import com.della.myseen.model.Show;

import java.util.List;

public class ShowAdapter extends RecyclerView.Adapter<ShowAdapter.ViewHolder> {

    public interface OnShowActionListener {
        void onEdit(Show show);
        void onDelete(Show show);
    }

    private final Context context;
    private List<Show> showList;
    private final OnShowActionListener listener;

    public ShowAdapter(Context context, List<Show> showList, OnShowActionListener listener) {
        this.context = context;
        this.showList = showList;
        this.listener = listener;
    }

    public void updateList(List<Show> newList) {
        this.showList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.item_show, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Show show = showList.get(position);

        holder.tvNo.setText(String.valueOf(position + 1));
        holder.tvTitle.setText(show.getTitle());
        holder.tvGenre.setText(show.getGenre());

        // Rating
        if (show.getRating() != null) {
            holder.ivStar.setVisibility(View.VISIBLE);
            holder.tvRating.setText(show.getRating() + "/10");
        } else {
            holder.ivStar.setVisibility(View.GONE);
            holder.tvRating.setText("-");
        }

        // Date + Time
        holder.tvDate.setText(show.getAddedDate() + "\n" + show.getAddedTime());

        // Status badge
        String status = show.getStatus();
        holder.tvStatus.setText(getShortStatus(status));
        applyStatusBadge(holder, status);

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(show));
        holder.btnDelete.setOnClickListener(v -> listener.onDelete(show));
    }

    private String getShortStatus(String status) {
        switch (status) {
            case "Ingin Ditonton": return "Ingin\nDitonton";
            case "Sedang Ditonton": return "Sedang\nDitonton";
            case "Sudah Ditonton": return "Sudah\nDitonton";
            default: return status;
        }
    }

    private void applyStatusBadge(ViewHolder holder, String status) {
        switch (status) {
            case "Ingin Ditonton":
                holder.badgeContainer.setBackgroundResource(R.drawable.bg_status_ingin);
                holder.ivStatusIcon.setImageResource(R.drawable.ic_hourglass);
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.ingin_ditonton, null));
                break;
            case "Sedang Ditonton":
                holder.badgeContainer.setBackgroundResource(R.drawable.bg_status_sedang);
                holder.ivStatusIcon.setImageResource(R.drawable.ic_play_circle);
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.sedang_ditonton, null));
                break;
            case "Sudah Ditonton":
            default:
                holder.badgeContainer.setBackgroundResource(R.drawable.bg_status_sudah);
                holder.ivStatusIcon.setImageResource(R.drawable.ic_check_circle);
                holder.tvStatus.setTextColor(context.getResources().getColor(R.color.sudah_ditonton, null));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return showList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNo, tvTitle, tvGenre, tvRating, tvStatus, tvDate;
        ImageView ivStar, ivStatusIcon;
        LinearLayout badgeContainer;
        ImageButton btnEdit, btnDelete;

        ViewHolder(View itemView) {
            super(itemView);
            tvNo = itemView.findViewById(R.id.tv_no);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvGenre = itemView.findViewById(R.id.tv_genre);
            tvRating = itemView.findViewById(R.id.tv_rating);
            ivStar = itemView.findViewById(R.id.iv_star);
            tvStatus = itemView.findViewById(R.id.tv_status);
            ivStatusIcon = itemView.findViewById(R.id.iv_status_icon);
            badgeContainer = itemView.findViewById(R.id.badge_container);
            tvDate = itemView.findViewById(R.id.tv_date);
            btnEdit = itemView.findViewById(R.id.btn_edit);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }
    }
}
