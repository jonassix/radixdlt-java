package com.radixdlt.client.core.network.selector;

import com.radixdlt.client.core.network.RadixNode;

import java.util.List;
import java.util.function.Function;

/**
 * Peer selectors that select single peer out of a list of available peers with at least one viable peer in it
 */
public interface RadixPeerSelector extends Function<List<RadixNode>, RadixNode> {
}
