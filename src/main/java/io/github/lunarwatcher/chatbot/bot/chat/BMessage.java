package io.github.lunarwatcher.chatbot.bot.chat;

import lombok.AllArgsConstructor;

/**
 * Extremely basic message not containing any information about where it is, where it's going, etc
 */

public class BMessage {
    public String content;
    public boolean replyIfPossible;

    public BMessage(String content, boolean rip){
        this.content = content;
        this.replyIfPossible = rip;
    }

}
