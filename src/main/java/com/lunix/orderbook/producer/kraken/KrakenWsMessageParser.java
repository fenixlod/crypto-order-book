package com.lunix.orderbook.producer.kraken;

import java.io.IOException;
import java.util.Optional;

import com.lunix.orderbook.models.AssetOrders;

public interface KrakenWsMessageParser {

	/**
	 * Parse messages produced by Kraken Websockets API.
	 * Returns AssetOrders only if the message is from the "book" topic.
	 * 
	 * @param message
	 * @return
	 * @throws Exception
	 */
	Optional<AssetOrders> messageToAssetOrders(String message) throws IOException;
}