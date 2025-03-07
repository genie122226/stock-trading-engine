package org.example;

public class Order {
    final boolean orderType;  // true: Buy, false: Sell
    final String tickerSymbol;
    int quantity;
    double price;
    Order next;

    Order(boolean orderType, String tickerSymbol, int quantity, double price) {
        this.orderType = orderType;
        this.tickerSymbol = tickerSymbol;
        this.quantity = quantity;
        this.price = price;
        this.next = null;
    }
}
