package com.lunix.orderbook.models;

import java.math.BigDecimal;

public record Order(BigDecimal price, BigDecimal volume) {

	@Override
	public String toString() {
		return "[%s, %s]".formatted(price.toString(), volume.toString());
	}
}
