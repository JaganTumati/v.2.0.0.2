package com.restaurant.ai.ai.gemini;

import java.util.List;

public class GeminiGenerateResponse {

    private List<Candidate> candidates;

    public List<Candidate> getCandidates() {
        return candidates;
    }

    public void setCandidates(List<Candidate> candidates) {
        this.candidates = candidates;
    }

    public static class Candidate {
        private GeminiContent content;
        private String finishReason;

        public GeminiContent getContent() {
            return content;
        }

        public void setContent(GeminiContent content) {
            this.content = content;
        }

        public String getFinishReason() {
            return finishReason;
        }

        public void setFinishReason(String finishReason) {
            this.finishReason = finishReason;
        }
    }
}
