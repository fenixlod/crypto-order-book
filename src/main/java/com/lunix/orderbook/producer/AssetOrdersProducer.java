package com.lunix.orderbook.producer;

import java.util.List;
import java.util.function.Consumer;

import com.lunix.orderbook.models.AssetOrders;

public interface AssetOrdersProducer {
	public void addConsumer(Consumer<AssetOrders> consumer);

	public void start(List<String> assets) throws Exception;

	public void stop();
}
