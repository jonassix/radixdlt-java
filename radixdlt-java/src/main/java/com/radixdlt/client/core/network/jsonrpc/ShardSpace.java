package com.radixdlt.client.core.network.jsonrpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.radix.serialization2.DsonOutput;
import org.radix.serialization2.DsonOutput.Output;
import org.radix.serialization2.SerializerConstants;
import org.radix.serialization2.SerializerDummy;
import org.radix.serialization2.SerializerId2;

@SerializerId2("radix.shard.space")
public final class ShardSpace {
	public static final int SHARD_CHUNKS = 1 << 20;
	public static final long SHARD_CHUNK_RANGE = -(Long.MIN_VALUE / SHARD_CHUNKS) * 2;
	public static final long SHARD_CHUNK_HALF_RANGE = -(Long.MIN_VALUE / SHARD_CHUNKS);
	public static final ShardRange SHARD_RANGE_FULL = new ShardRange(-SHARD_CHUNK_HALF_RANGE, SHARD_CHUNK_HALF_RANGE - 1);

	public static int toChunk(long shard) {
		shard &= ~1;
		return (int) (((shard / 2) + Math.abs(Long.MIN_VALUE / 2)) / ShardSpace.SHARD_CHUNK_HALF_RANGE);
	}

	public static long fromChunk(int chunk, long anchor) {
		if (anchor < -SHARD_CHUNK_HALF_RANGE || anchor > SHARD_CHUNK_HALF_RANGE) {
			throw new IllegalArgumentException("Anchor is invalid");
		}

		return (Long.MIN_VALUE + (ShardSpace.SHARD_CHUNK_RANGE * chunk) + anchor);
	}

	// Placeholder for the serializer ID
	@JsonProperty(SerializerConstants.SERIALIZER_NAME)
	@DsonOutput(Output.ALL)
	private SerializerDummy serializer = SerializerDummy.DUMMY;

	@JsonProperty("anchor")
	@DsonOutput(Output.ALL)
	private long anchor;

	@JsonProperty("range")
	@DsonOutput(Output.ALL)
	private ShardRange range;

	ShardSpace() {
	}

	public ShardSpace(long anchor, long range) {
		if (range < 0) {
			throw new IllegalArgumentException("Argument range is negative");
		}

		if (range > SHARD_CHUNK_RANGE) {
			throw new IllegalArgumentException("Argument range is greater than " + SHARD_CHUNK_RANGE);
		}

		this.anchor = anchor;

		long chunkOffset = anchor % SHARD_CHUNK_HALF_RANGE;

		long low = (chunkOffset - (range / 2));
		long high = low + range;

		long lowRemainder = 0;
		if (low < -SHARD_CHUNK_HALF_RANGE) {
			lowRemainder = -SHARD_CHUNK_HALF_RANGE - low;
			low += lowRemainder;
		}

		long highRemainder = 0;
		if (high > SHARD_CHUNK_HALF_RANGE) {
			highRemainder = high - SHARD_CHUNK_HALF_RANGE;
			high -= highRemainder;
		}

		low -= highRemainder;
		high += lowRemainder - 1;

		this.range = new ShardRange(low, high);
	}

	public ShardSpace(long anchor, ShardRange range) {
		if (range.getSpan() < 0) {
			throw new IllegalArgumentException("Argument range is negative");
		}

		if (range.getSpan() > SHARD_CHUNK_RANGE) {
			throw new IllegalArgumentException("Argument range is greater than " + SHARD_CHUNK_RANGE);
		}

		// TODO check to make sure the provided ShardRange is correct

		this.anchor = anchor;
		long chunkOffset = anchor % SHARD_CHUNK_HALF_RANGE;
		this.range = new ShardRange(range.getLow(), range.getHigh());
	}

	public long getAnchor() {
		return this.anchor;
	}

	public ShardRange getRange() {
		return this.range;
	}

	public boolean intersects(long shard) {
		long remainder = (shard % SHARD_CHUNK_HALF_RANGE);
		return this.range.intersects(remainder);
	}

	public boolean intersects(Collection<Long> shards) {
		for (long shard : shards) {
			if (this.range.intersects(shard % SHARD_CHUNK_HALF_RANGE)) {
				return true;
			}
		}

		return false;
	}

	public boolean intersects(ShardRange shardRange) {
		return this.range.intersects(shardRange);
	}

	public boolean intersects(ShardSpace shardSpace) {
		return this.range.intersects(shardSpace.getRange());
	}

	public Set<Long> intersection(Set<Long> shards) {
		Set<Long> intersections = new HashSet<Long>();

		for (Long shard : shards) {
			if (intersects(shard)) {
				intersections.add(shard);
			}
		}

		return Collections.unmodifiableSet(intersections);
	}

	@Override
	public String toString() {
		return "Anchor: " + getAnchor() + " Range: "
			+ this.range.getLow() + " -> " + this.range.getHigh();
	}
}
