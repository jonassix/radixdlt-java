package com.radixdlt.client.core.atoms;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableMap;
import com.radixdlt.client.core.atoms.particles.Particle;
import com.radixdlt.client.core.atoms.particles.Spin;
import com.radixdlt.client.core.atoms.particles.SpunParticle;
import com.radixdlt.client.core.crypto.ECSignature;
import org.radix.common.ID.AID;
import org.radix.common.ID.EUID;
import org.radix.serialization2.DsonOutput;
import org.radix.serialization2.SerializerConstants;
import org.radix.serialization2.SerializerDummy;
import org.radix.serialization2.SerializerId2;
import org.radix.serialization2.client.SerializableObject;
import org.radix.serialization2.client.Serialize;

import java.util.ArrayList;
import com.radixdlt.client.atommodel.accounts.RadixAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An atom is the fundamental atomic unit of storage on the ledger (similar to a block
 * in a blockchain) and defines the actions that can be issued onto the ledger.
 */
@SerializerId2("radix.atom")
public final class Atom {
	public static final String METADATA_TIMESTAMP_KEY = "timestamp";
	public static final String METADATA_POW_NONCE_KEY = "powNonce";

	@JsonProperty("version")
	@DsonOutput(DsonOutput.Output.ALL)
	private short version = 100;

	// Placeholder for the serializer ID
	@JsonProperty(SerializerConstants.SERIALIZER_NAME)
	// TODO serializer id for atoms is temporarily excluded from hash for compatibility with abstract atom
	@DsonOutput(value = {DsonOutput.Output.API, DsonOutput.Output.WIRE, DsonOutput.Output.PERSIST})
	private SerializerDummy serializer = SerializerDummy.DUMMY;

	@JsonProperty("particleGroups")
	@DsonOutput(DsonOutput.Output.ALL)
	private final List<ParticleGroup> particleGroups = new ArrayList<>();

	@JsonProperty("signatures")
	@DsonOutput(value = {DsonOutput.Output.API, DsonOutput.Output.WIRE, DsonOutput.Output.PERSIST})
	private final Map<String, ECSignature> signatures = new HashMap<>();

	@JsonProperty("metaData")
	@DsonOutput(DsonOutput.Output.ALL)
	private final ImmutableMap<String, String> metaData;

	private Atom() {
		this(0);
	}

	public Atom(long timestamp) {
		this(Collections.emptyList(), timestamp);
	}

	public Atom(ParticleGroup particleGroup, long timestamp) {
		this(Collections.singletonList(particleGroup), ImmutableMap.of(METADATA_TIMESTAMP_KEY, String.valueOf(timestamp)));
	}

	public Atom(List<ParticleGroup> particleGroups, long timestamp) {
		this(particleGroups, ImmutableMap.of(METADATA_TIMESTAMP_KEY, String.valueOf(timestamp)));
	}

	public Atom(List<ParticleGroup> particleGroups, Map<String, String> metaData) {
		Objects.requireNonNull(particleGroups, "particleGroups is required");
		Objects.requireNonNull(metaData, "metaData is required");

		this.particleGroups.addAll(particleGroups);
		this.metaData = ImmutableMap.copyOf(metaData);
	}

	private Atom(
		List<ParticleGroup> particleGroups,
		Map<String, String> metaData,
		EUID signatureId,
		ECSignature signature
	) {
		this(particleGroups, metaData);

		Objects.requireNonNull(signatureId, "signatureId is required");
		Objects.requireNonNull(signature, "signature is required");

		this.signatures.put(signatureId.toString(), signature);
	}

	public Atom withSignature(ECSignature signature, EUID signatureId) {
		return new Atom(
			this.particleGroups,
			this.metaData,
			signatureId,
			signature
		);
	}

	private Set<Long> getShards() {
		return this.spunParticles()
			.map(SpunParticle<Particle>::getParticle)
			.map(Particle::getDestinations)
			.flatMap(Set::stream)
			.map(EUID::getShard)
			.collect(Collectors.toSet());
	}

	// HACK
	public Set<Long> getRequiredFirstShard() {
		if (this.spunParticles().anyMatch(s -> s.getSpin() == Spin.DOWN)) {
			return this.spunParticles()
				.filter(s -> s.getSpin() == Spin.DOWN)
				.flatMap(s -> s.getParticle().getShardables().stream())
				.map(RadixAddress::getUID)
				.map(EUID::getShard)
				.collect(Collectors.toSet());
		} else {
			return this.getShards();
		}
	}

	public Stream<ParticleGroup> particleGroups() {
		return this.particleGroups.stream();
	}

	public Stream<SpunParticle> spunParticles() {
		return this.particleGroups.stream().flatMap(ParticleGroup::spunParticles);
	}

	public Stream<Particle> particles(Spin spin) {
		return this.spunParticles().filter(s -> s.getSpin() == spin).map(SpunParticle::getParticle);
	}

	public Stream<RadixAddress> addresses() {
		return this.spunParticles()
			.map(SpunParticle<Particle>::getParticle)
			.map(Particle::getShardables)
			.flatMap(Set::stream)
			.distinct();
	}

	public boolean hasTimestamp() {
		return this.metaData.containsKey(METADATA_TIMESTAMP_KEY);
	}

	/**
	 * Convenience method to retrieve timestamp
	 *
	 * @return The timestamp in milliseconds since epoch
	 */
	public long getTimestamp() {
		// TODO Not happy with this error handling as it moves some validation work into the atom data. See RLAU-951
		try {
			return Long.parseLong(this.metaData.get(METADATA_TIMESTAMP_KEY));
		} catch (NumberFormatException e) {
			return Long.MIN_VALUE;
		}
	}

	public Map<String, ECSignature> getSignatures() {
		return this.signatures;
	}

	public Optional<ECSignature> getSignature(EUID uid) {
		return Optional.ofNullable(this.signatures).map(sigs -> sigs.get(uid.toString()));
	}

	public byte[] toDson() {
		return Serialize.getInstance().toDson(this, DsonOutput.Output.HASH);
	}

	public RadixHash getHash() {
		return RadixHash.of(toDson());
	}

	public AID getAid() {
		return AID.from(getHash(), getShards());
	}

	/**
	 * Get the metadata associated with the atom
	 * @return an immutable map of the meta data
	 */
	public Map<String, String> getMetaData() {
		return this.metaData;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Atom)) {
			return false;
		}

		Atom atom = (Atom) o;
		return this.getHash().equals(atom.getHash());
	}

	@Override
	public int hashCode() {
		return this.getHash().hashCode();
	}

	@Override
	public String toString() {
		String particleGroupsStr = this.particleGroups.stream().map(ParticleGroup::toString).collect(Collectors.joining(","));
		return String.format("%s[%s:%s]", getClass().getSimpleName(), getAid(), particleGroupsStr);
	}
}
