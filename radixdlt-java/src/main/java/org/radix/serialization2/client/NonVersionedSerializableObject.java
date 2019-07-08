package org.radix.serialization2.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.radix.serialization2.DsonOutput;
import org.radix.serialization2.DsonOutput.Output;
import org.radix.serialization2.SerializerConstants;
import org.radix.serialization2.SerializerDummy;

/**
 * Temporary class to hold new non-versioned classes
 */
public class NonVersionedSerializableObject {
	// Placeholder for the serializer ID
	@JsonProperty(SerializerConstants.SERIALIZER_NAME)
	@DsonOutput(Output.ALL)
	private SerializerDummy serializer = SerializerDummy.DUMMY;
}
