package com.lunix.orderbook;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.lunix.orderbook.models.Order;
import com.lunix.orderbook.models.SortedOrders;
import com.lunix.orderbook.utils.OrderUtils;

public class TestsSortedOrders {

	@Test
	public void checkOrder() {
		Order lowOrder = new Order(BigDecimal.valueOf(10), BigDecimal.valueOf(1000));
		Order bigOrder = new Order(BigDecimal.valueOf(50), BigDecimal.valueOf(1000));
		SortedOrders orders = new SortedOrders(OrderUtils.priceAscSorting());
		orders.addOrder(bigOrder);
		orders.addOrder(lowOrder);
		Optional<Order> bestOrder = orders.getBestOrder();
		assert (bestOrder.isPresent());
		assert (bestOrder.get().equals(lowOrder));
		System.out.println("Test successfull");
	}
}