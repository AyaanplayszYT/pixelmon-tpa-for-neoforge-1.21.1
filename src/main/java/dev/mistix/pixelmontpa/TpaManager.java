package dev.mistix.pixelmontpa;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import net.minecraft.server.level.ServerPlayer;
import dev.mistix.pixelmontpa.TpaRequest.RequestType;

public class TpaManager {
    private final Map<UUID, TpaRequest> requestsByTarget = new HashMap<>();
    private final Map<UUID, Instant> lastRequestByRequester = new HashMap<>();
    private final Map<UUID, Boolean> tpaToggleByPlayer = new HashMap<>();

    public synchronized Optional<String> createRequest(ServerPlayer requester, ServerPlayer target, RequestType requestType) {
        if (requester.getUUID().equals(target.getUUID())) {
            return Optional.of("You cannot send a TPA request to yourself.");
        }

        cleanupExpired();

        if (!isTpaEnabled(target.getUUID())) {
            return Optional.of(target.getName().getString() + " has TPA requests disabled.");
        }

        Instant now = Instant.now();
        Duration commandCooldown = Duration.ofSeconds(TpaConfig.commandCooldownSeconds());
        Instant cooldownEnd = lastRequestByRequester.getOrDefault(requester.getUUID(), Instant.EPOCH).plus(commandCooldown);
        if (cooldownEnd.isAfter(now)) {
            long secondsLeft = Duration.between(now, cooldownEnd).toSeconds() + 1;
            return Optional.of("Please wait " + secondsLeft + "s before sending another request.");
        }

        requestsByTarget.put(target.getUUID(), new TpaRequest(requester.getUUID(), target.getUUID(), now, requestType));
        lastRequestByRequester.put(requester.getUUID(), now);
        return Optional.empty();
    }

    public synchronized Optional<TpaRequest> getRequestForTarget(UUID targetId) {
        cleanupExpired();
        return Optional.ofNullable(requestsByTarget.get(targetId));
    }

    public synchronized Optional<TpaRequest> acceptRequest(UUID targetId) {
        cleanupExpired();
        TpaRequest request = requestsByTarget.remove(targetId);
        return Optional.ofNullable(request);
    }

    public synchronized Optional<TpaRequest> denyRequest(UUID targetId) {
        cleanupExpired();
        TpaRequest request = requestsByTarget.remove(targetId);
        return Optional.ofNullable(request);
    }

    public synchronized boolean hasPendingRequest(UUID targetId) {
        cleanupExpired();
        return requestsByTarget.containsKey(targetId);
    }

    public synchronized List<TpaRequest> getAllRequests() {
        cleanupExpired();
        return new ArrayList<>(requestsByTarget.values())
                .stream()
                .sorted(Comparator.comparing(TpaRequest::createdAt).reversed())
                .collect(Collectors.toList());
    }

    public synchronized boolean toggleTpa(UUID playerId) {
        boolean newState = !isTpaEnabled(playerId);
        tpaToggleByPlayer.put(playerId, newState);
        return newState;
    }

    public synchronized boolean isTpaEnabled(UUID playerId) {
        return tpaToggleByPlayer.getOrDefault(playerId, true);
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        Duration requestTimeout = Duration.ofSeconds(TpaConfig.requestTimeoutSeconds());
        requestsByTarget.entrySet().removeIf(entry -> Duration.between(entry.getValue().createdAt(), now).compareTo(requestTimeout) > 0);
    }
}
