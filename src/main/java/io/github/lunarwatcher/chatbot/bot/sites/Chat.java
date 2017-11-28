package io.github.lunarwatcher.chatbot.bot.sites;

import io.github.lunarwatcher.chatbot.bot.chat.Message;

import java.io.IOException;

public interface Chat {
    void logIn() throws IOException;
    void sendMessage(Message message) throws IOException;
    void receiveMessage(Message message);

    /**
     * The raw message, not formatted into an instance of the Message class
     * @param input
     */
    void rawReceive(String input);

    /**
     *
     * @return whether or not the message was deleted
     */
    boolean deleteMessage();

    /**
     *
     * @return whether or nto the message was edited
     */
    boolean editMessage();

    void listen();

    void joinRoom(long id);
    void leaveRoom(long id);

}
