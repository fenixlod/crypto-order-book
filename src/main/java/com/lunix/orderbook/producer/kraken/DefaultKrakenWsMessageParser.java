package com.lunix.orderbook.producer.kraken;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunix.orderbook.models.AssetOrders;
import com.lunix.orderbook.models.Order;

public class DefaultKrakenWsMessageParser implements KrakenWsMessageParser {
	private static final Logger logger = Logger.getLogger("com.lunix.orderbook");
	private final static String SNAPSHOT_ASKS_TOKEN = "as";
	private final static String UPDATET_ASKS_TOKEN = "a";
	private final static String SNAPSHOT_BIDS_TOKEN = "bs";
	private final static String UPDATE_BIDS_TOKEN = "b";
	private final static int ORDERS_POSITION = 1;
	private final static int ASSETS_PAIR_POSITION = 3;
	private final static int ORDER_PRICE_POSITION = 0;
	private final static int ORDER_VOLUME_POSITION = 1;
	private final ObjectMapper objMapper;

	public DefaultKrakenWsMessageParser(ObjectMapper objMapper) {
		this.objMapper = objMapper;
	}

	@Override
	public Optional<AssetOrders> messageToAssetOrders(String message) throws IOException {
		Object messageObject = objMapper.readValue(message, Object.class);
		if (!(messageObject instanceof List<?> messageList))
			return Optional.empty();

		if (!(messageList.get(ORDERS_POSITION) instanceof Map<?, ?> ordersMap))
			return Optional.empty();

		int positionOffset = 0;
		List<Order> asks = new ArrayList<>(), bids = new ArrayList<>();
		parseOrdersObject(ordersMap, asks, bids);

		if (messageList.size() == 5) {
			positionOffset = 1;
			if (messageList.get(ORDERS_POSITION + 1) instanceof Map<?, ?> ordersMap2)
				parseOrdersObject(ordersMap2, asks, bids);
		}

		String assetsPairName = (String) messageList.get(ASSETS_PAIR_POSITION + positionOffset);
		return Optional.of(new AssetOrders(assetsPairName, asks, bids));
	}

	private void parseOrdersObject(Object object, List<Order> asks, List<Order> bids) {
		if (!(object instanceof Map<?, ?> ordersMap))
			return;

		Object askList = ordersMap.get(SNAPSHOT_ASKS_TOKEN);
		if (Objects.isNull(askList))
			askList = ordersMap.get(UPDATET_ASKS_TOKEN);
		parseOrdersList(askList, asks);

		Object bidsList = ordersMap.get(SNAPSHOT_BIDS_TOKEN);
		if (Objects.isNull(bidsList))
			bidsList = ordersMap.get(UPDATE_BIDS_TOKEN);
		parseOrdersList(bidsList, bids);
	}

	private void parseOrdersList(Object orders, List<Order> ordersList) {
		if (Objects.isNull(orders))
			return;

		if (!(orders instanceof List<?> values))
			return;

		for (Object value : values) {
			Optional<Order> order = parseOrder(value);
			if (order.isPresent())
				ordersList.add(order.get());
		}
	}

	private Optional<Order> parseOrder(Object value) {
		if (Objects.isNull(value))
			return Optional.empty();

		if (!(value instanceof List<?> values))
			return Optional.empty();
		
		try {
			BigDecimal price = new BigDecimal((String) values.get(ORDER_PRICE_POSITION));
			BigDecimal volume = new BigDecimal((String) values.get(ORDER_VOLUME_POSITION));
			return Optional.of(new Order(price, volume));
		} catch (NumberFormatException | ClassCastException e) {
			logger.severe("Unable to parse order data: " + e.getMessage());
			return Optional.empty();
		}
	}
}
