package com.lunix.orderbook.models;

import java.util.List;

import com.lunix.orderbook.utils.OrderUtils;

public class OrderBook {
	private final SortedOrders bids;
	private final SortedOrders asks;

	public OrderBook() {
		bids = new SortedOrders(OrderUtils.priceDescSorting());
		asks = new SortedOrders(OrderUtils.priceAscSorting());
	}

	public void addAsks(List<Order> asksList) {
		asksList.forEach(ask -> asks.addOrder(ask));
	}

	public void addBids(List<Order> bidsList) {
		bidsList.forEach(bid -> bids.addOrder(bid));
	}

	@Override
	public String toString() {
		return """
				asks:
				%s
				bids:
				%s
				best ask: %s
				best bid: %s""".formatted(asks.toString(), bids.toString(),
				String.valueOf(asks.getBestOrder().orElse(null)),
				String.valueOf(bids.getBestOrder().orElse(null)));
	}
}