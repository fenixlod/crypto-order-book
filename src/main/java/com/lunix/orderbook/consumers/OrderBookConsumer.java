package com.lunix.orderbook.consumers;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import com.lunix.orderbook.models.AssetOrders;
import com.lunix.orderbook.models.OrderBook;

public class OrderBookConsumer implements Consumer<AssetOrders> {
	private final Map<String, OrderBook> orderBooks;

	public OrderBookConsumer() {
		orderBooks = new HashMap<>();
	}

	@Override
	public void accept(AssetOrders orders) {
		OrderBook assetOrders = orderBooks.computeIfAbsent(orders.pairName(), (k) -> new OrderBook());
		assetOrders.addAsks(orders.asks());
		assetOrders.addBids(orders.bids());
		System.out.println(this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Entry<String, OrderBook> entry : orderBooks.entrySet()) {
			sb.append("""
					<------------------------------------>
					%s
					%s
					>------------------------------------<
					""".formatted(entry.getValue(), entry.getKey()));
		}
		return sb.toString();
	}
}
