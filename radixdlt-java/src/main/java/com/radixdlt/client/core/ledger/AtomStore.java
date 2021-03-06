package com.radixdlt.client.core.ledger;

import com.radixdlt.client.atommodel.accounts.RadixAddress;

import com.radixdlt.client.core.atoms.Atom;
import com.radixdlt.client.core.atoms.ParticleGroup;
import com.radixdlt.client.core.atoms.particles.Particle;
import io.reactivex.Observable;
import java.util.List;
import java.util.stream.Stream;
import javax.annotation.Nullable;

/**
 * The interface in which a client retrieves the state of the ledger.
 * Particle conflict handling along with Atom DELETEs and STOREs all
 * occur at this layer.
 */
public interface AtomStore {

	/**
	 * Temporary interface for propagating when the current store
	 * is synced with some node on a given address.
	 * TODO: This is probably the wrong place for this and will probably
	 * TODO: move this into a different layer but good enough for now
	 *
	 * @param address The address to check for sync
	 * @return a never ending observable which emits when local store is
	 * synced with some origin
	 */
	Observable<Long> onSync(RadixAddress address);

	/**
	 * Retrieve the current set of validated atoms at a given shardable
	 *
	 * @param address the address to get the atoms under
	 * @return a stream of all stored atoms of the current local view
	 */
	Stream<Atom> getStoredAtoms(RadixAddress address);

	/**
	 * Retrieve a never ending observable of atom observations (STORED and DELETED)
	 * which are then processed by the local store.
	 *
	 * @param address the address to get the updates from
	 * @return a never ending observable of updates
	 */
	Observable<AtomObservation> getAtomObservations(RadixAddress address);

	/**
	 * Retrieve the current set of validated up particles at a given shardable.
	 * If uuid is provided also retrieves and staged particles under that uuid.
	 *
	 * @param address the address to get the particles under
	 * @param uuid uuid of staged particles to include
	 * @return a stream of all up particles of the current local view
	 */
	Stream<Particle> getUpParticles(RadixAddress address, @Nullable String uuid);

	/**
	 * Adds the particle group to the staging area for the given uuid
	 *
	 * @param uuid the uuid to add the particle group to
	 * @param particleGroup the particle group to add to staging area
	 */
	void stageParticleGroup(String uuid, ParticleGroup particleGroup);

	/**
	 * Retrieves all staged particle groups and clears the staging area
	 * for the given uuid.
	 * TODO: Cleanup interface
	 *
	 * @param uuid uuid to retrieve the staged particle groups for
	 * @return all staged particle groups in the order they were staged
	 */
	List<ParticleGroup> getStagedAndClear(String uuid);
}
