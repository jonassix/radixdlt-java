package com.radixdlt.client.application.translate.tokens;

import com.radixdlt.client.atommodel.rri.RRIParticle;
import com.radixdlt.client.atommodel.tokens.TransferrableTokensParticle;
import com.radixdlt.client.atommodel.tokens.UnallocatedTokensParticle;
import com.radixdlt.client.core.atoms.particles.RRI;
import java.util.List;

import com.radixdlt.client.atommodel.tokens.MutableSupplyTokenDefinitionParticle;
import com.radixdlt.client.core.atoms.ParticleGroup;
import org.junit.Test;

import com.radixdlt.client.application.translate.tokens.CreateTokenAction.TokenSupplyType;
import com.radixdlt.client.atommodel.accounts.RadixAddress;
import com.radixdlt.client.core.crypto.ECPublicKey;
import org.radix.common.ID.EUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CreateTokenToParticleGroupsMapperTest {
	@Test
	public void testNormalConstruction() {
		CreateTokenAction tokenCreation = mock(CreateTokenAction.class);
		RadixAddress address = mock(RadixAddress.class);
		when(address.getUID()).thenReturn(mock(EUID.class));
		ECPublicKey key = mock(ECPublicKey.class);
		when(address.getPublicKey()).thenReturn(key);
		when(tokenCreation.getRRI()).thenReturn(RRI.of(address, "ISO"));
		when(tokenCreation.getInitialSupply()).thenReturn(TokenUnitConversions.getMinimumGranularity());
		when(tokenCreation.getGranularity()).thenReturn(TokenUnitConversions.getMinimumGranularity());
		when(tokenCreation.getTokenSupplyType()).thenReturn(TokenSupplyType.MUTABLE);

		CreateTokenToParticleGroupsMapper createTokenToParticlesMapper = new CreateTokenToParticleGroupsMapper();
		List<ParticleGroup> particleGroups = createTokenToParticlesMapper.mapToParticleGroups(tokenCreation);
		assertThat(particleGroups).anyMatch(p -> p.spunParticles().anyMatch(s -> s.getParticle() instanceof MutableSupplyTokenDefinitionParticle));
		assertThat(particleGroups).anyMatch(p -> p.spunParticles().anyMatch(s -> s.getParticle() instanceof TransferrableTokensParticle));
		assertThat(particleGroups).anyMatch(p -> p.spunParticles().anyMatch(s -> s.getParticle() instanceof UnallocatedTokensParticle));
		assertThat(particleGroups).anyMatch(p -> p.spunParticles().anyMatch(s -> s.getParticle() instanceof RRIParticle));
		assertThat(particleGroups.stream().flatMap(ParticleGroup::spunParticles).count()).isEqualTo(6);
	}
}