package org.example;

import java.util.Random;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    /**
     * 模拟活跃交易的 wrapper
     * 启动两个线程：
     *  - 一个线程不断随机生成订单，调用 addOrder 函数；
     *  - 另一个线程定时随机挑选一只股票，调用 matchOrder 尝试撮合订单。
     */
    public static void simulateOrders() {
        // 预设部分常用股票代码，注意实际支持 1024 支股票
        String[] tickerSymbols = {"AAPL", "GOOG", "MSFT", "AMZN", "FB", "TSLA", "NFLX", "NVDA", "ADBE", "INTC"};
        String[] orderTypes = {"Buy", "Sell"};
        Random rand = new Random();

        // 订单生成线程
        new Thread(() -> {
            while (true) {
                String orderType = orderTypes[rand.nextInt(orderTypes.length)];
                String tickerSymbol = tickerSymbols[rand.nextInt(tickerSymbols.length)];
                int quantity = rand.nextInt(100) + 1;      // 数量在 1~100
                double price = 50 + rand.nextDouble() * 100; // 价格在 50~150
                StockExchange.addOrder(orderType, tickerSymbol, quantity, price);
                try {
                    Thread.sleep(rand.nextInt(100)); // 随机短暂停顿
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();

        // 订单匹配线程
        new Thread(() -> {
            while (true) {
                String tickerSymbol = tickerSymbols[rand.nextInt(tickerSymbols.length)];
                StockExchange.matchOrder(tickerSymbol);
                try {
                    Thread.sleep(200); // 每隔200毫秒尝试一次撮合
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    /**
     * 主函数，启动模拟程序
     */
    public static void main(String[] args) {
        simulateOrders();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}