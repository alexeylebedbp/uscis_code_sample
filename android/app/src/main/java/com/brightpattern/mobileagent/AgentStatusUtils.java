package com.brightpattern.mobileagent;

public class AgentStatusUtils {
    public String formatText(String input){
        String out;
        out = input.replaceAll("[_-]"," ");
        out = toTitleCase(out);
        return out;
    }

    private String toTitleCase(String string) {
        if (string == null) {
            return null;
        }
        boolean whiteSpace = true;
        StringBuilder builder = new StringBuilder(string);
        final int builderLength = builder.length();
        for (int i = 0; i < builderLength; ++i) {
            char c = builder.charAt(i);
            if (whiteSpace) {
                if (!Character.isWhitespace(c)) {
                    builder.setCharAt(i, Character.toTitleCase(c));
                    whiteSpace = false;
                }
            } else if (Character.isWhitespace(c)) {
                whiteSpace = true;
            } else {
                builder.setCharAt(i, Character.toLowerCase(c));
            }
        }
        return builder.toString();
    }

    public int getImage(String agent_state){
        if(agent_state==null) return R.drawable.decline;
        switch (agent_state)
        {
            case "not_ready":
                return R.drawable.not_ready;
            case "ready":
                return R.drawable.ready;
            case "supervising":
                return R.drawable.supervising;
            case "after_call_work":
                return R.drawable.acw;
            case "logged_out":
                return R.drawable.logged_out;
            case "busy":
                return R.drawable.busy;
            case "busy_call":
                return R.drawable.busy_call;
            case "busy_chat":
                return R.drawable.busy_chat;
            case "busy_email":
                return R.drawable.busy_email;
            case "busy_preview":
                return R.drawable.busy_preview;
            case "incoming_call":
                return R.drawable.incoming_call;
            case "incoming_chat":
                return R.drawable.incoming_chat;
            default:
                return R.drawable.unknown_state;
        }
    }
    public String determinTitleToDisplay(String agentState,String title){
        if(title == null || title == ""){
            return agentState;
        }
        return title;
    }

    public boolean isIncomingInteraction(String agentState){
        return (agentState=="incoming_call" || agentState == "incoming_chat");
    }

    public boolean isAgentState(String input){
        return "notready".equals(input.toLowerCase().replaceAll("[^a-z]",""));
    }
}
