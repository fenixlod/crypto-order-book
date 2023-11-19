package com.lunix.orderbook.models;

import java.util.List;

public record AssetOrders(String pairName, List<Order> asks, List<Order> bids) {
}
