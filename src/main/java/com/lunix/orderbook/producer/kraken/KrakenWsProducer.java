package com.lunix.orderbook.producer.kraken;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.WebSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.function.Consumer;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lunix.orderbook.models.AssetOrders;
import com.lunix.orderbook.producer.AssetOrdersProducer;
import com.lunix.orderbook.producer.kraken.dto.AssetPairs;
import com.lunix.orderbook.producer.kraken.dto.SubscribeRequest;

public class KrakenWsProducer implements AssetOrdersProducer {
	private static final Logger logger = Logger.getLogger("com.lunix.orderbook");
	private final static String KRAKEN_ASSET_PAIRS_API_URL = "https://api.kraken.com/0/public/AssetPairs";
	private final static String WEB_SOCKET_URL = "wss://ws.kraken.com/";
	private final static String ASSET_PAIR_VALIDATION_REGEX = "^\\w+\\/?\\w+$";
	private final ObjectMapper objMapper;
	private final List<Consumer<AssetOrders>> consumers;
	private WebSocket socket;
	private KrakenWsMessageParser messageParser;

	private class WebSocketListener implements WebSocket.Listener {
		private StringBuilder sb = new StringBuilder();

		@Override
		public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
			try {
				sb.append(data);
				webSocket.request(1);
				if (last) {
					Optional<AssetOrders> orders = messageParser.messageToAssetOrders(sb.toString());
					if (orders.isPresent())
						produce(orders.get());

					sb.setLength(0);
				}
			} catch (IOException e) {
				logger.severe("Unable to parse Kraken ws message: " + e.getMessage());
				e.printStackTrace();
			}
			return WebSocket.Listener.super.onText(webSocket, data, false);
		}
	}

	public KrakenWsProducer() {
		consumers = new ArrayList<>();
		objMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		messageParser = new DefaultKrakenWsMessageParser(objMapper);
	}

	public void setMessageParser(KrakenWsMessageParser messageParser) {
		this.messageParser = messageParser;
	}

	@Override
	public void addConsumer(Consumer<AssetOrders> newConsumer) {
		consumers.add(newConsumer);
	}

	@Override
	public void start(List<String> assetPairs) throws IOException, InterruptedException, IllegalArgumentException {
		logger.info("Starting Kraken websockets API order book provider...");
		validateAssetPairs(assetPairs);
		List<String> pairNames = getAssetPairsNames(assetPairs);
		socket = openWsConnection(pairNames);
		logger.info("Kraken Websockets API provider is running...");
	}

	@Override
	public void stop() {
		if (Objects.nonNull(socket)) {
			logger.info("Kraken websockets API order book provider is closing...");
			socket.sendClose(2000, "Close");
		}
	}

	private void validateAssetPairs(List<String> assetPairs) {
		Objects.requireNonNull(assetPairs);

		if (assetPairs.isEmpty())
			throw new IllegalArgumentException("At least one asset pair is required");

		List<String> invalidPairs = assetPairs.stream().filter(pair -> !pair.matches(ASSET_PAIR_VALIDATION_REGEX))
				.collect(Collectors.toList());

		if (!invalidPairs.isEmpty())
			throw new IllegalArgumentException("Invalid asset pair(s): " + String.join(", ", invalidPairs));
	}

	private List<String> getAssetPairsNames(List<String> assetPairs) throws IOException, InterruptedException {
		String url = "%s?pair=%s".formatted(KRAKEN_ASSET_PAIRS_API_URL, String.join(",", assetPairs));
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.build();
		String responseBody = HttpClient.newHttpClient()
				.send(request, BodyHandlers.ofString())
				.body();
		AssetPairs pairs = objMapper.readValue(responseBody, AssetPairs.class);
		return pairs.result().values().stream().map(v -> v.wsname()).collect(Collectors.toList());
	}

	private WebSocket openWsConnection(List<String> pairNames) throws IOException {
		SubscribeRequest subscribeRequest = new SubscribeRequest(pairNames, "book");
		WebSocket ws = HttpClient.newHttpClient()
				.newWebSocketBuilder()
				.buildAsync(URI.create(WEB_SOCKET_URL), new WebSocketListener())
				.join();
		ws.sendText(objMapper.writeValueAsString(subscribeRequest), true);
		return ws;
	}

	private void produce(AssetOrders orders) {
		consumers.forEach(c -> c.accept(orders));

		// Current consumers notification is not async, but this can be changed like:
		// consumers.forEach(c -> CompletableFuture.runAsync(() -> c.accept(orders)));
		// This implementation will require consumers to be thread safe, which are
		// currently not.
	}
}
