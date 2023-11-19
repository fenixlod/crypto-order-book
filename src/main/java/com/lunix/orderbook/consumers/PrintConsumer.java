package com.lunix.orderbook.consumers;

import java.util.function.Consumer;

import com.lunix.orderbook.models.AssetOrders;

public class PrintConsumer implements Consumer<AssetOrders> {

	@Override
	public void accept(AssetOrders orders) {
		System.out.println(orders);
	}
}
