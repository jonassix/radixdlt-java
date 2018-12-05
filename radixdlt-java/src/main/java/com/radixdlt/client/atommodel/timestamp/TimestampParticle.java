package com.radixdlt.client.atommodel.timestamp;

import com.radixdlt.client.atommodel.quarks.ChronoQuark;
import com.radixdlt.client.core.atoms.particles.Particle;
import org.radix.serialization2.SerializerId2;

/**
 * Particle which stores time related aspects of an atom.
 */
@SerializerId2("TIMESTAMPPARTICLE")
public class TimestampParticle extends Particle {
	private TimestampParticle() {
	}

	public TimestampParticle(long timestamp) {
		super(new ChronoQuark("default", timestamp));
	}

	public long getTimestamp() {
		return getQuarkOrError(ChronoQuark.class).getTimestamp();
	}
}
