### LockFreeSortedOrderList

A lock-free sorted linked list (Harris algorithm).

### Order

Holds basic order information (buy/sell, ticker symbol, quantity, price).

### StockExchange

Uses a fixed-size array (1,024 entries) to store order books.
Each order book has two lock-free lists: one for buys and one for sells.
Provides methods to addOrder and matchOrder.
