package com.digitalart.wallet.domain;

public enum TransactionType {
    DEPOSIT("Deposit"),
    WITHDRAWAL("Withdrawal"),
    PURCHASE("Purchase"),
    REFUND("Refund"),
    ARTIST_EARNING("Artist Earning");

    private final String displayName;

    TransactionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
