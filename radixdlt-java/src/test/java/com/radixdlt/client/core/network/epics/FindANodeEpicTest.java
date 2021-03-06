package com.radixdlt.client.core.network.epics;

import com.google.common.collect.ImmutableMap;
import com.radixdlt.client.core.network.RadixNetworkState;
import com.radixdlt.client.core.network.RadixNode;
import com.radixdlt.client.core.network.RadixNodeAction;
import com.radixdlt.client.core.network.RadixNodeState;
import com.radixdlt.client.core.network.actions.CloseWebSocketAction;
import com.radixdlt.client.core.network.actions.ConnectWebSocketAction;
import com.radixdlt.client.core.network.actions.FindANodeRequestAction;
import com.radixdlt.client.core.network.actions.FindANodeResultAction;
import com.radixdlt.client.core.network.jsonrpc.ShardSpace;
import com.radixdlt.client.core.network.selector.GetFirstSelector;
import com.radixdlt.client.core.network.selector.RandomSelector;
import com.radixdlt.client.core.network.websocket.WebSocketClient;
import com.radixdlt.client.core.network.websocket.WebSocketStatus;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.ReplaySubject;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FindANodeEpicTest {
	private RadixNodeState mockedNodeState(WebSocketStatus status) {
		RadixNodeState nodeState = mock(RadixNodeState.class);
		when(nodeState.getStatus()).thenReturn(status);
		ShardSpace shardSpace = mock(ShardSpace.class);
		when(shardSpace.intersects(any(Collection.class))).thenReturn(true);
		when(nodeState.getShards()).thenReturn(Optional.of(shardSpace));

		return nodeState;
	}

	@Test
	public void testValidClient() {
		RadixNode node = mock(RadixNode.class);
		WebSocketClient ws = mock(WebSocketClient.class);
		when(ws.sendMessage(any())).thenReturn(true);

		FindANodeRequestAction request = mock(FindANodeRequestAction.class);
		when(request.getShards()).thenReturn(Collections.singleton(1L));

		FindANodeEpic findANodeFunction = new FindANodeEpic(new GetFirstSelector());
		TestObserver<RadixNodeAction> testObserver = TestObserver.create();
		findANodeFunction.epic(
			Observable.<RadixNodeAction>just(request).concatWith(Observable.never()),
			Observable.just(RadixNetworkState.of(node, mockedNodeState(WebSocketStatus.CONNECTED)))
		)
		.subscribe(testObserver);

		testObserver.awaitCount(1);
		testObserver.assertValue(u -> u.getNode().equals(node));
	}

	@Test
	public void disconnectedNodeConnectionTest() {
		RadixNode node = mock(RadixNode.class);
		FindANodeEpic findANodeEpic = new FindANodeEpic(new RandomSelector());

		RadixNodeState nodeState = mock(RadixNodeState.class);
		when(nodeState.getStatus()).thenReturn(WebSocketStatus.DISCONNECTED);
		when(nodeState.getShards()).thenReturn(Optional.of(new ShardSpace(10000, 20000)));

		FindANodeRequestAction request = mock(FindANodeRequestAction.class);
		when(request.getShards()).thenReturn(Collections.singleton(1L));

		TestObserver<RadixNodeAction> testObserver = TestObserver.create();
		findANodeEpic.epic(
			Observable.<RadixNodeAction>just(request).concatWith(Observable.never()),
			Observable.just(RadixNetworkState.of(node, nodeState)).concatWith(Observable.never())
		)
		.subscribe(testObserver);

		testObserver.awaitCount(1);
		testObserver.assertValue(u -> u instanceof ConnectWebSocketAction);
		testObserver.assertValue(u -> u.getNode().equals(node));
		testObserver.assertNotComplete();
	}

	@Test
	public void dontConnectToAllNodesTest() {
		RadixNode connectedPeer = mock(RadixNode.class);

		Map<RadixNode, RadixNodeState> networkStateMap = IntStream.range(0, 100).boxed().collect(Collectors.toMap(
			i -> {
				if (i == 0) {
					return connectedPeer;
				}

				RadixNode peer = mock(RadixNode.class);
				return peer;
			},
			i -> mockedNodeState(i == 0 ? WebSocketStatus.CONNECTED : WebSocketStatus.DISCONNECTED)
		));

		FindANodeRequestAction request = mock(FindANodeRequestAction.class);
		when(request.getShards()).thenReturn(Collections.singleton(1L));

		FindANodeEpic findANodeFunction = new FindANodeEpic(new GetFirstSelector());
		TestObserver<RadixNodeAction> testObserver = TestObserver.create();
		findANodeFunction.epic(
			Observable.<RadixNodeAction>just(request).concatWith(Observable.never()),
			Observable.just(new RadixNetworkState(networkStateMap)).concatWith(Observable.never())
		)
		.subscribe(testObserver);

		testObserver.assertValue(u -> u.getNode().equals(connectedPeer));
		testObserver.assertValue(u -> ((FindANodeResultAction) u).getRequest().equals(request));
	}

	@Test
	public void when_first_node_takes_too_long__then_second_node_should_connect() {
		RadixNode badPeer = mock(RadixNode.class);
		RadixNode goodPeer = mock(RadixNode.class);

		ReplaySubject<RadixNetworkState> networkState = ReplaySubject.create();


		networkState.onNext(new RadixNetworkState(ImmutableMap.of(
			badPeer, mockedNodeState(WebSocketStatus.DISCONNECTED),
			goodPeer, mockedNodeState(WebSocketStatus.DISCONNECTED)
		)));

		FindANodeEpic findANodeEpic = new FindANodeEpic(new GetFirstSelector());
		TestObserver<RadixNodeAction> testObserver = TestObserver.create();

		FindANodeRequestAction request = mock(FindANodeRequestAction.class);
		when(request.getShards()).thenReturn(Collections.singleton(1L));

		findANodeEpic.epic(
			Observable.<RadixNodeAction>just(request).concatWith(Observable.never()),
			networkState
		)
			.doOnNext(i -> {
				if (i.getNode().equals(badPeer)) {
					networkState.onNext(new RadixNetworkState(ImmutableMap.of(
						badPeer, mockedNodeState(WebSocketStatus.CONNECTING),
						goodPeer, mockedNodeState(WebSocketStatus.DISCONNECTED)
					)));
				} else {
					networkState.onNext(new RadixNetworkState(ImmutableMap.of(
						badPeer, mockedNodeState(WebSocketStatus.CONNECTING),
						goodPeer, mockedNodeState(WebSocketStatus.CONNECTED)
					)));
				}
			})
			.subscribe(testObserver);

		testObserver.awaitCount(4);
		testObserver.assertValueAt(0, u -> u instanceof ConnectWebSocketAction && u.getNode().equals(badPeer));
		testObserver.assertValueAt(1, u -> u instanceof ConnectWebSocketAction && u.getNode().equals(goodPeer));
		testObserver.assertValueAt(2, u -> u instanceof CloseWebSocketAction && u.getNode().equals(badPeer));
		testObserver.assertValueAt(3, u -> ((FindANodeResultAction) u).getRequest().equals(request) && u.getNode().equals(goodPeer));
	}

	@Test
	public void when_first_node_fails__then_second_node_should_connect() {
		RadixNode badPeer = mock(RadixNode.class);
		RadixNode goodPeer = mock(RadixNode.class);

		ReplaySubject<RadixNetworkState> networkState = ReplaySubject.create();


		networkState.onNext(new RadixNetworkState(ImmutableMap.of(
			badPeer, mockedNodeState(WebSocketStatus.DISCONNECTED),
			goodPeer, mockedNodeState(WebSocketStatus.DISCONNECTED)
		)));

		FindANodeEpic findANodeEpic = new FindANodeEpic(new GetFirstSelector());
		TestObserver<RadixNodeAction> testObserver = TestObserver.create();

		FindANodeRequestAction request = mock(FindANodeRequestAction.class);
		when(request.getShards()).thenReturn(Collections.singleton(1L));

		findANodeEpic.epic(
			Observable.<RadixNodeAction>just(request).concatWith(Observable.never()),
			networkState
		)
		.doOnNext(i -> {
			if (i.getNode().equals(badPeer)) {
				networkState.onNext(new RadixNetworkState(ImmutableMap.of(
					badPeer, mockedNodeState(WebSocketStatus.FAILED),
					goodPeer, mockedNodeState(WebSocketStatus.DISCONNECTED)
				)));
			} else {
				networkState.onNext(new RadixNetworkState(ImmutableMap.of(
					badPeer, mockedNodeState(WebSocketStatus.FAILED),
					goodPeer, mockedNodeState(WebSocketStatus.CONNECTED)
				)));
			}
		})
		.subscribe(testObserver);

		testObserver.awaitCount(4);
		testObserver.assertValueAt(0, u -> u instanceof ConnectWebSocketAction && u.getNode().equals(badPeer));
		testObserver.assertValueAt(1, u -> u instanceof ConnectWebSocketAction && u.getNode().equals(goodPeer));
		testObserver.assertValueAt(2, u -> u instanceof CloseWebSocketAction && u.getNode().equals(badPeer));
		testObserver.assertValueAt(3, u -> ((FindANodeResultAction) u).getRequest().equals(request) && u.getNode().equals(goodPeer));
	}
}