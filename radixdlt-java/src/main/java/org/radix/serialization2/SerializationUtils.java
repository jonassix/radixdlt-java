package org.radix.serialization2;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import org.radix.common.ID.EUID;

import java.nio.charset.StandardCharsets;

/**
 * Collection of Serialization-related utilities
 */
public class SerializationUtils {
	private static final HashFunction MURMUR_3_128 = Hashing.murmur3_128();

	private SerializationUtils() {
		throw new IllegalStateException("Cannot instantiate.");
	}

	public static EUID stringToNumericID(String id) {
		HashCode h = MURMUR_3_128.hashBytes(id.getBytes(StandardCharsets.UTF_8));
		return new EUID(h.asBytes());
	}
}
