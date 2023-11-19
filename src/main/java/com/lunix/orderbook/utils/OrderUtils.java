package com.lunix.orderbook.utils;

import java.util.Comparator;

import com.lunix.orderbook.models.Order;

public class OrderUtils {
	public static Comparator<Order> priceAscSorting() {
		return (o1, o2) -> o1.price().compareTo(o2.price());
	}

	public static Comparator<Order> priceDescSorting() {
		return (o1, o2) -> o2.price().compareTo(o1.price());
	}
}
