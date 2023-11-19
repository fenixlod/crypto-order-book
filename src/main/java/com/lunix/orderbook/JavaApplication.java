package com.lunix.orderbook;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import com.lunix.orderbook.consumers.OrderBookConsumer;
import com.lunix.orderbook.producer.kraken.KrakenWsProducer;

public class JavaApplication {
	private static final Logger logger = Logger.getLogger("com.lunix.orderbook");

	public static void main(String[] args) {
		try {
			logger.info("Starting...");
			CountDownLatch latch = new CountDownLatch(1);
			KrakenWsProducer provider = new KrakenWsProducer();
			// provider.addConsumer(new PrintConsumer());
			provider.addConsumer(new OrderBookConsumer());
			provider.start(List.of("BTC/USD", "ETH/USD"));
			latch.await();
			logger.info("Exit");
		} catch (Exception e) {
			logger.severe("Failed to track asstets order book: " + e.getMessage());
			e.printStackTrace();
		}
	}
}
