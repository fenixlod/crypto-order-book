package com.lunix.orderbook.producer.kraken.dto;

import java.util.Map;

public record AssetPairs(Map<String, AssetPair> result) {
}
