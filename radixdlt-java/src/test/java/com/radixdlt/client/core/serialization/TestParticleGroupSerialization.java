package com.radixdlt.client.core.serialization;

import org.junit.BeforeClass;
import org.junit.Test;
import org.radix.serialization2.Serialization;
import org.radix.serialization2.SerializationException;
import org.radix.serialization2.DsonOutput.Output;
import org.radix.serialization2.client.Serialize;

import com.google.common.base.Strings;
import com.radixdlt.client.core.atoms.Atom;
import com.radixdlt.client.core.atoms.ParticleGroup;

import static org.junit.Assert.assertEquals;

public class TestParticleGroupSerialization {
	static Serialization serialization;

	@BeforeClass
	public static void setupSerializer() {
		serialization = Serialize.getInstance();
	}

	@Test
	public void testLargeStringSerialization() throws SerializationException {
		long timestamp = 0x0001020304050607L;

		// "massive" must be greater length than (16000 / 4) - 4 = 3996
		String massive = Strings.repeat("X", 4096);
		ParticleGroup pg = ParticleGroup.builder().addMetaData("massive", massive).build();
		Atom atom = new Atom(pg, timestamp);

		byte[] atombytes = serialization.toDson(atom, Output.HASH);

		assertEquals(4249, atombytes.length);
	}
}
