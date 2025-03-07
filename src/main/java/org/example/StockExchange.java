package org.example;

/**
 * Simulated stock trading system: Uses Harris lock-free sorted linked list to implement
 * an order book and order matching.
 */
public class StockExchange {

    /**
     * Each stock has an associated order book that contains two lock-free sorted linked lists:
     * - buyOrders: All buy orders, sorted in descending order by price (highest buy price first)
     * - sellOrders: All sell orders, sorted in ascending order by price (lowest sell price first)
     */
    static class OrderBook {
        LockFreeSortedOrderList buyOrders = new LockFreeSortedOrderList(true);
        LockFreeSortedOrderList sellOrders = new LockFreeSortedOrderList(false);
    }

    // Use a fixed-size array to simulate order books for 1,024 stocks (without using dictionaries or maps)
    static final int NUM_TICKERS = 1024;
    static OrderBook[] orderBooks = new OrderBook[NUM_TICKERS];

    // Static initializer: Create an order book for each stock
    static {
        for (int i = 0; i < NUM_TICKERS; i++) {
            orderBooks[i] = new OrderBook();
        }
    }

    /**
     * Maps a stock ticker symbol to an index in the orderBooks array (using modulo 1024).
     */
    static int getTickerIndex(String tickerSymbol) {
        return Math.abs(tickerSymbol.hashCode()) % NUM_TICKERS;
    }

    /**
     * addOrder function:
     *
     * Creates an order with the given parameters and adds it to the corresponding stock's order book
     * (using a lock-free sorted linked list).
     */
    public static void addOrder(String orderType, String tickerSymbol, int quantity, double price) {
        boolean isBuy = orderType.equalsIgnoreCase("Buy");
        Order newOrder = new Order(isBuy, tickerSymbol, quantity, price);
        int index = getTickerIndex(tickerSymbol);
        OrderBook book = orderBooks[index];
        if (isBuy) {
            book.buyOrders.add(newOrder);
        } else {
            book.sellOrders.add(newOrder);
        }
//        System.out.println("Added " + orderType + " order: " + tickerSymbol +
//                " Qty:" + quantity + " Price:" + price);
    }

    /**
     * matchOrder function:
     * For a specified ticker symbol, retrieves from the corresponding order book:
     *   - The head order of the buy orders list (highest buy price)
     *   - The head order of the sell orders list (lowest sell price)
     * If the highest buy price is greater than or equal to the lowest sell price, a match is made and both orders are removed.
     */
    public static void matchOrder(String tickerSymbol) {
        int index = getTickerIndex(tickerSymbol);
        OrderBook book = orderBooks[index];

        Order bestBuy = book.buyOrders.peek();
        Order bestSell = book.sellOrders.peek();

        if (bestBuy == null || bestSell == null) {
            return;
        }

        if (bestBuy.price >= bestSell.price) {
//            System.out.println("Matched for ticker " + tickerSymbol +
//                    ": Buy at " + bestBuy.price + " vs Sell at " + bestSell.price);
            book.buyOrders.remove(bestBuy);
            book.sellOrders.remove(bestSell);
        }
    }
}
