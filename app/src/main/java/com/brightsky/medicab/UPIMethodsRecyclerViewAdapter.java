package com.brightsky.medicab;

import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class UPIMethodsRecyclerViewAdapter extends RecyclerView.Adapter<UPIMethodsRecyclerViewAdapter.ViewHolder> {

    List<UPIItems> upiItems;
    UPIItemClickListener upiItemClickListener;

    public UPIMethodsRecyclerViewAdapter(List<UPIItems> upiItems) {
        this.upiItems = upiItems;
    }

    public void setUpiItemClickListener(UPIItemClickListener upiItemClickListener) {
        this.upiItemClickListener = upiItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.upi_items, parent, false);
        return new ViewHolder(v, upiItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TextView upiMethodName = holder.getUpiItemNameTextView();
        ImageView upiMethodImage = holder.getUpiItemImageView();

        upiMethodName.setText(upiItems.get(position).getUpiItemName());
        upiMethodImage.setImageResource(R.mipmap.google_pay);
    }

    private Uri getUPIUri(String vpa, String merchantName, String price) {
        return new Uri.Builder()
                .scheme("upi")
                .authority("pay")
                .appendQueryParameter("pa", vpa)
                .appendQueryParameter("pn", merchantName)
                .appendQueryParameter("tn", "Demo Testing Google Pay UPI")
                .appendQueryParameter("am", price)
                .appendQueryParameter("cu", "INR")
                .build();
    }

    @Override
    public int getItemCount() {
        return upiItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView upiItemNameTextView;
        ImageView upiItemImageView;
        UPIItemClickListener upiItemClickListener;

        public ViewHolder(@NonNull View itemView, UPIItemClickListener upiItemClickListener) {
            super(itemView);

            this.upiItemNameTextView = itemView.findViewById(R.id.upi_item_name);
            this.upiItemImageView = itemView.findViewById(R.id.upi_method_icon);
            this.upiItemClickListener = upiItemClickListener;

            itemView.setOnClickListener(this);
        }

        public TextView getUpiItemNameTextView() {
            return upiItemNameTextView;
        }

        public ImageView getUpiItemImageView() {
            return upiItemImageView;
        }

        @Override
        public void onClick(View v) {
            Log.i("PaymentActivity", "Inside OnClick");

            if(getAdapterPosition() == 0) {
                Log.i("PaymentActivity", "Adapter Position 0");
                upiItemClickListener.onGooglePayItemClickListener();
            } else if(getAdapterPosition() == 1) {
                Log.i("PaymentActivity", "Adapter Position 1");
                upiItemClickListener.onPhonePeItemClickListener();
            }
        }
    }
}
