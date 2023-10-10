package me.olliejonas.saltmarsh.scheduledevents;

import java.util.Collection;

public record EventEmbed(String eventId, Collection<String> notGoing) {
}
