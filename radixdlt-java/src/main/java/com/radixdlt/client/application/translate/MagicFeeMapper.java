package com.radixdlt.client.application.translate;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.radixdlt.client.core.RadixUniverse;
import com.radixdlt.client.core.atoms.Atom;
import com.radixdlt.client.core.atoms.ParticleGroup;
import com.radixdlt.client.core.crypto.ECPublicKey;
import org.radix.common.tuples.Pair;

import java.util.List;
import java.util.Map;

/**
 * Maps a complete list of particles ready to be submitted to a POW fee particle.
 */
public class MagicFeeMapper implements FeeMapper {
	public MagicFeeMapper() {
	}

	@Override
	public Pair<Map<String, String>, List<ParticleGroup>> map(Atom atom, RadixUniverse universe, ECPublicKey key) {
		return Pair.of(ImmutableMap.of("magic", "magic!"), ImmutableList.of());
	}
}
