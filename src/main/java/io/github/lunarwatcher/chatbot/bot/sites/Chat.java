package io.github.lunarwatcher.chatbot.bot.sites;

import io.github.lunarwatcher.chatbot.bot.chat.Message;
import io.github.lunarwatcher.chatbot.bot.commands.BotConfig;

import java.io.IOException;

public interface Chat {
    void logIn() throws IOException;
    void save();
    void load();

    BotConfig getConfig();
    String getName();

}
