package me.olliejonas.saltmarsh.poll;

import java.util.Map;
import java.util.Set;

public record PollPOJO(String messageId, String question, String author, boolean anonymous, boolean singular, Map<String, Set<Integer>> votes) {
}
