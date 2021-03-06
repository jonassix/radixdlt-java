package com.radixdlt.client.core.network.selector;

import com.radixdlt.client.core.network.RadixNodeState;

/**
 * An api version filter that rejects any incompatible peers as determined by the
 */
public class CompatibleApiVersionFilter implements RadixPeerFilter {
	private final int version;

	public CompatibleApiVersionFilter(int version) {
		this.version = version;
	}

	@Override
	public boolean test(RadixNodeState nodeState) {
		return nodeState.getVersion().map(v -> v == this.version).orElse(false);
	}
}
