package com.storex.twopc.model;

public class PrepareResponse {

    private String participantName;
    private VoteResult vote;
    private String reason;

    public PrepareResponse(String participantName, VoteResult vote, String reason) {
        this.participantName = participantName;
        this.vote = vote;
        this.reason = reason;
    }

    public String getParticipantName() { return participantName; }
    public VoteResult getVote() { return vote; }
    public String getReason() { return reason; }
}
