package com.lunix.orderbook.models;

import java.util.Comparator;
import java.util.Optional;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class SortedOrders {
	private final SortedSet<Order> orders;
	
	public SortedOrders(Comparator<Order> comparator) {
		orders = new TreeSet<>(comparator);
	}

	public void addOrder(Order order) {
		if (order.volume().signum() == 0) {
			orders.remove(order);
			return;
		}

		orders.add(order);
	}

	public Optional<Order> getBestOrder() {
		return orders.stream().findFirst();
	}

	@Override
	public String toString() {
		return "[%s]".formatted(
				String.join(",\n", orders.stream().map(Order::toString).collect(Collectors.toList())));
	}
}