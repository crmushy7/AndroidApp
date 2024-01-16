package com.example.longlast;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
public class ReceiptAdapter extends RecyclerView.Adapter<ReceiptAdapter.ViewHolder> {
    private List<Receipt> receipts;

    public ReceiptAdapter(List<Receipt> receipts) {
        this.receipts = receipts;
    }

    public void setReceipts(List<Receipt> receipts) {
        this.receipts = receipts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.receiptitems, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Receipt receipt = receipts.get(position);
        holder.bind(receipt);
    }

    @Override
    public int getItemCount() {
        return receipts.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView debtorTextView;
        private TextView amountTextView;
        private TextView dateTextView;
        private TextView statusTextview;
        private TextView timeTextview;
        private TextView kifupiTextview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            debtorTextView = itemView.findViewById(R.id.receiptName);
            amountTextView = itemView.findViewById(R.id.receiptAmount);
            dateTextView = itemView.findViewById(R.id.receiptDate);
            timeTextview = itemView.findViewById(R.id.receiptTime);
            kifupiTextview = itemView.findViewById(R.id.receiptKifupi);
            statusTextview = itemView.findViewById(R.id.receiptStatus);
        }

        public void bind(Receipt receipt) {
            debtorTextView.setText(receipt.getDebtor());
            amountTextView.setText(receipt.getAmount().trim()+" Tsh");
            dateTextView.setText(receipt.getDate());
            statusTextview.setText(receipt.getStatus());
            timeTextview.setText(receipt.getTime());

            String fullName = receipt.getDebtor();

            if (fullName != null) {
                String[] names = fullName.split(" ", 2);

                if (names.length >= 2) {
                    String firstNameLater = names[0].toUpperCase();
                    String secondNameLater = names[1].toUpperCase();

                    kifupiTextview.setText(firstNameLater.charAt(0) + "" + secondNameLater.charAt(0));
                } else {
                    String firstNameLater = names[0].toUpperCase();
                    kifupiTextview.setText(firstNameLater.charAt(0)+""+firstNameLater.charAt(1));
                }
            }

        }
    }
}
