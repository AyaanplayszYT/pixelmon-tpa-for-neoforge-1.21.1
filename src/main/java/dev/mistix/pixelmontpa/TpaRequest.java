package dev.mistix.pixelmontpa;

import java.time.Instant;
import java.util.UUID;

public record TpaRequest(UUID requesterId, UUID targetId, Instant createdAt, RequestType requestType) {
	public enum RequestType {
		TPA,
		TPA_HERE
	}
}
