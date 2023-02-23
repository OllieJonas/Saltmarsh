package me.olliejonas.saltmarsh.poll;

import java.util.HashSet;
import java.util.Set;

public class PollOption {
    private final String prompt;
    private final Set<String> voters;

    private int noVotes;

    public PollOption(String prompt) {
        this.prompt = prompt;
        this.voters = new HashSet<>();
        this.noVotes = 0;
    }

    public String prompt() {
        return prompt;
    }

    public Set<String> voters() {
        return voters;
    }

    public int noVotes() {
        return noVotes;
    }

    public boolean vote(String voter) {
        boolean alreadyVoted = alreadyVoted(voter);

        if (alreadyVoted) {
            voters.remove(voter);
            noVotes--;
        } else {
            voters.add(voter);
            noVotes++;
        }

        return alreadyVoted;
    }

    private boolean alreadyVoted(String voter) {
        return voters.contains(voter);
    }
}
