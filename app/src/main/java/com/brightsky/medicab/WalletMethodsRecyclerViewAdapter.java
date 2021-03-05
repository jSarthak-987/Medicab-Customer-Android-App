package com.brightsky.medicab;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class WalletMethodsRecyclerViewAdapter extends RecyclerView.Adapter<WalletMethodsRecyclerViewAdapter.ViewHolder> {

    List<WalletItems> walletItems;

    public WalletMethodsRecyclerViewAdapter(List<WalletItems> walletItems) {
        this.walletItems = walletItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.wallet_items, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView paymentMethodName = holder.getWalletItemNameTextView();

        paymentMethodName.setText(walletItems.get(position).getWalletItemName());
    }

    @Override
    public int getItemCount() {
        return walletItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView walletItemNameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            this.walletItemNameTextView = itemView.findViewById(R.id.wallet_item_name);
        }

        public TextView getWalletItemNameTextView() {
            return walletItemNameTextView;
        }
    }
}
