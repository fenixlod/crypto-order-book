package com.lunix.orderbook.producer.kraken.dto;

import java.util.List;

public record SubscribeRequest(String event, List<String> pair, Subscription subscription) {
	public record Subscription(String name) {
	}
	
	public SubscribeRequest(List<String> pair, String eventName) {
		this("subscribe", pair, new Subscription(eventName));
	}
}
