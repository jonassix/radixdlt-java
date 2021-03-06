package com.radixdlt.client.atommodel.unique;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.radixdlt.client.atommodel.Identifiable;
import com.radixdlt.client.atommodel.accounts.RadixAddress;
import com.radixdlt.client.core.atoms.particles.Particle;
import com.radixdlt.client.core.atoms.particles.RRI;
import org.radix.serialization2.DsonOutput;
import org.radix.serialization2.SerializerId2;

@SerializerId2("radix.particles.unique")
public class UniqueParticle extends Particle implements Identifiable {
	@JsonProperty("name")
	@DsonOutput(DsonOutput.Output.ALL)
	private String name;

	@JsonProperty("address")
	@DsonOutput(DsonOutput.Output.ALL)
	private RadixAddress address;

	@JsonProperty("nonce")
	@DsonOutput(DsonOutput.Output.ALL)
	private long nonce;

	private UniqueParticle() {
		super();
	}

	public UniqueParticle(RadixAddress address, String unique) {
		super(address.getUID());
		this.address = address;
		this.name = unique;
		this.nonce = System.nanoTime();
	}

	public String getName() {
		return name;
	}

	public RRI getRRI() {
		return RRI.of(address, name);
	}
}
