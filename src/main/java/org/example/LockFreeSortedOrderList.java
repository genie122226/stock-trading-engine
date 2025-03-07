package org.example;

import java.util.concurrent.atomic.AtomicMarkableReference;

/**
 * LockFreeSortedOrderList implements a lock-free sorted linked list using Harris' algorithm
 * for maintaining an order book.
 *
 * To differentiate between buy orders and sell orders, we use a boolean flag isBuyList:
 * - For the buy order list, we use key = -price so that the highest buy price appears at the head.
 * - For the sell order list, we use key = price so that the lowest sell price appears at the head.
 */
public class LockFreeSortedOrderList {
    private final boolean isBuyList;

    // Inner Node class: stores an Order, a sorting key, and an atomic markable reference to the next node.
    private class Node {
        final Order order;    // The order stored in this node (for sentinel nodes, order is null)
        final double key;     // The sorting key
        AtomicMarkableReference<Node> next;

        Node(Order order, double key) {
            this.order = order;
            this.key = key;
            this.next = new AtomicMarkableReference<>(null, false);
        }
    }

    // Helper class: Window, representing a pair of adjacent nodes (predecessor and current) during search.
    private class Window {
        Node pred, curr;
        Window(Node pred, Node curr) {
            this.pred = pred;
            this.curr = curr;
        }
    }

    private final Node head;
    private final Node tail;

    public LockFreeSortedOrderList(boolean isBuyList) {
        this.isBuyList = isBuyList;
        // Initialize sentinel nodes: head with key -∞, tail with key +∞.
        head = new Node(null, Double.NEGATIVE_INFINITY);
        tail = new Node(null, Double.POSITIVE_INFINITY);
        head.next.set(tail, false);
    }

    /**
     * Helper method: find
     * Searches for the first node with key >= the given key, returning a window consisting of
     * the predecessor and the current node.
     */
    private Window find(double key) {
        Node pred = null, curr = null, succ = null;
        boolean[] marked = {false};
        retry:
        while (true) {
            pred = head;
            curr = pred.next.getReference();
            while (true) {
                succ = curr.next.get(marked);
                // If curr is logically deleted, help physically remove it.
                while (marked[0]) {
                    if (!pred.next.compareAndSet(curr, succ, false, false)) {
                        continue retry;
                    }
                    curr = succ;
                    succ = curr.next.get(marked);
                }
                // When we find the first node with key >= given key, return the window.
                if (curr.key >= key) {
                    return new Window(pred, curr);
                }
                pred = curr;
                curr = succ;
            }
        }
    }

    /**
     * Adds an order to the list while maintaining sorted order.
     * For a buy order list, key = -price; for a sell order list, key = price.
     */
    public boolean add(Order order) {
        double key = isBuyList ? -order.price : order.price;
        while (true) {
            Window window = find(key);
            Node pred = window.pred;
            Node curr = window.curr;
            Node newNode = new Node(order, key);
            newNode.next.set(curr, false);
            if (pred.next.compareAndSet(curr, newNode, false, false)) {
                return true;
            }
        }
    }

    /**
     * Removes the specified order from the list.
     * Both the key and the order reference are compared.
     */
    public boolean remove(Order order) {
        double key = isBuyList ? -order.price : order.price;
        while (true) {
            Window window = find(key);
            Node pred = window.pred;
            Node curr = window.curr;
            // If the corresponding node is not found, return false.
            if (curr == tail || curr.key != key || curr.order != order) {
                return false;
            }
            Node succ = curr.next.getReference();
            // Attempt to logically delete the current node by marking its next reference.
            if (!curr.next.attemptMark(succ, true)) {
                continue;
            }
            // Help physically remove the logically deleted node.
            pred.next.compareAndSet(curr, succ, false, false);
            return true;
        }
    }

    /**
     * Returns the first valid order in the list (i.e., the first node after the head that is not deleted).
     */
    public Order peek() {
        Node curr = head.next.getReference();
        if (curr == tail) {
            return null;
        }
        return curr.order;
    }
}
