package com.radixdlt.client.application.translate.tokens;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.radixdlt.client.application.translate.ShardedParticleStateId;
import com.radixdlt.client.atommodel.tokens.TransferrableTokensParticle;
import com.radixdlt.client.core.atoms.particles.RRI;
import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.Test;
import com.radixdlt.client.atommodel.accounts.RadixAddress;
import java.util.Collections;

public class TransferTokensToParticleGroupsMapperTest {

	@Test
	public void createTransactionWithNoFunds() {
		RadixAddress address = mock(RadixAddress.class);

		RRI token = mock(RRI.class);
		when(token.getName()).thenReturn("TEST");

		TransferTokensAction transferTokensAction = mock(TransferTokensAction.class);
		when(transferTokensAction.getAmount()).thenReturn(new BigDecimal("1.0"));
		when(transferTokensAction.getFrom()).thenReturn(address);
		when(transferTokensAction.getRRI()).thenReturn(token);

		TokenBalanceState state = mock(TokenBalanceState.class);
		when(state.getBalance()).thenReturn(Collections.emptyMap());

		TransferTokensToParticleGroupsMapper transferTranslator = new TransferTokensToParticleGroupsMapper();

		assertThat(transferTranslator.requiredState(transferTokensAction))
			.containsExactly(ShardedParticleStateId.of(TransferrableTokensParticle.class, address));

		assertThatThrownBy(() -> transferTranslator.mapToParticleGroups(transferTokensAction, Stream.empty()))
			.isEqualTo(new InsufficientFundsException(token, BigDecimal.ZERO, new BigDecimal("1.0")));
	}

}