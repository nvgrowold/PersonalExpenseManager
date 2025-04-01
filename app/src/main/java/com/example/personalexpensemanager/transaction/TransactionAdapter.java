package com.example.personalexpensemanager.transaction;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.personalexpensemanager.R;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //delare the interface at class level
    //click to view detail of a transaction
    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
    }
    private List<TransactionItem> items;
    private OnTransactionClickListener listener;   //click to view detail of a transaction

    public TransactionAdapter(List<TransactionItem> items, OnTransactionClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position){
        return items.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TransactionItem.TYPE_HEADER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_date_header, parent, false);
            return new HeaderViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_item_transaction, parent, false);
            return new TransactionViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).tvDate.setText(((DateHeader) items.get(position)).getDate());
        } else {
            Transaction transaction = (Transaction) items.get(position);
            TransactionViewHolder h = (TransactionViewHolder) holder;
            h.tvName.setText(transaction.getName());
            h.tvAmount.setText(String.format((transaction.getTransactionType().equals("income") ? "+$%.2f" : "-$%.2f"), transaction.getAmount()));
            h.tvAmount.setTextColor(transaction.getTransactionType().equals("income") ?
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.colorBrightBlue) :
                    ContextCompat.getColor(holder.itemView.getContext(), R.color.colorBrightRed));
            h.ivIcon.setImageResource(transaction.getTransactionType().equals("income") ?
                    R.drawable.icon_arrow_circle_down : R.drawable.icon_arrow_circle_up);
            //click to view detail of a transaction
            h.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onTransactionClick(transaction);
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;

        HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date_header);
        }
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvAmount;
        ImageView ivIcon;

        TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tv_merchant);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            ivIcon = itemView.findViewById(R.id.iv_transaction_icon);
        }
    }
}
