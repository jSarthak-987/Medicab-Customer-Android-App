package com.brightsky.medicab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class CardDetailsBottomSheetFragment extends BottomSheetDialogFragment implements View.OnClickListener {

    public interface CardDetailsListener {
        void getDetails(CardDetails cardDetails);
    }

    private CardDetailsListener cardDetailsListener;
    private EditText cardNumberEditText;
    private EditText cardExpiryDateEditText;
    private EditText cardCVVEditText;
    private EditText cardHolderNameEditText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.card_payment_bottom_sheet_fragment_layout, container, false);

        cardNumberEditText = v.findViewById(R.id.card_number_field);
        cardExpiryDateEditText = v.findViewById(R.id.card_expiry_date_field);
        cardCVVEditText = v.findViewById(R.id.card_cvv_field);
        cardHolderNameEditText = v.findViewById(R.id.card_holder_name_field);

        Button payNowButton = v.findViewById(R.id.pay_now_button);
        payNowButton.setOnClickListener(this);

        return v;
    }

    public void setCardDetailsListener(CardDetailsListener cardDetailsListener) {
        this.cardDetailsListener = cardDetailsListener;
    }

    @Override
    public void onClick(View v) {
        String cardNumber = cardNumberEditText.getText().toString();
        String cardExpiryDate = cardExpiryDateEditText.getText().toString();
        String cardCVV = cardCVVEditText.getText().toString();
        String cardHolderName = cardHolderNameEditText.getText().toString();

        CardDetails cardDetails = new CardDetails(cardNumber, cardCVV, cardHolderName, cardExpiryDate);
        cardDetailsListener.getDetails(cardDetails);
    }
}

class CardDetails {
    private final String cardNumber;
    private final String cardCVV;
    private final String cardHolderName;
    private final String cardExpiryDate;

    public CardDetails(String cardNumber, String cardCVV, String cardHolderName, String cardExpiryDate) {
        this.cardCVV = cardCVV;
        this.cardHolderName = cardHolderName;
        this.cardExpiryDate = cardExpiryDate;
        this.cardNumber = cardNumber;
    }

    public String getCardCVV() {
        return cardCVV;
    }

    public String getCardExpiryDate() {
        return cardExpiryDate;
    }

    public String getCardHolderName() {
        return cardHolderName;
    }

    public String getCardNumber() {
        return cardNumber;
    }
}