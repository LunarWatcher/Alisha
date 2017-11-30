package io.github.lunarwatcher.chatbot.bot.command;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lunarwatcher.chatbot.bot.chat.BMessage;
import io.github.lunarwatcher.chatbot.bot.chat.Message;
import io.github.lunarwatcher.chatbot.bot.commands.*;
import io.github.lunarwatcher.chatbot.bot.sites.discord.DiscordChat;
import io.github.lunarwatcher.chatbot.bot.sites.se.SEChat;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static io.github.lunarwatcher.chatbot.Constants.RELOCATION_VOTES;

public class CommandCenter {
    public static String TRIGGER;

    @Getter
    public List<Command> commands;
    //List<Listener> listeners;

    public CommandCenter(Properties botProps, boolean shrugAlt) {
        TRIGGER = botProps.getProperty("bot.trigger");
        commands = new ArrayList<>();
        commands.add(new HelpCommand(this));
        commands.add(new ShrugCommand(shrugAlt ? "¯\\\\_(ツ)_/¯" : "¯\\_(ツ)_/¯"));

        //listeners = new ArrayList<>();
    }

    public void loadSE(SEChat chat){
        commands.add(new Summon(RELOCATION_VOTES, chat));
        commands.add(new UnSummon(RELOCATION_VOTES, chat));
        commands.add(new AddHome(chat));
        commands.add(new RemoveHome(chat));
    }

    public void loadDiscord(DiscordChat chat){
        commands.add(new DiscordChat.Match());
    }
    /**
     * method used to load commands/listeners that are considered NSFW on some sites. This doesn't necessarily mean actually
     * NSFW, it basically means the commands that aren't wanted on sites like StackExchange. On most Discord servers
     * it's different so that's enabled by default.
     *
     * NSFW is up to whoever forks this bot, the method could even be called from the constructor to automatically
     * load it. The general usage here is for anything that involves swearing and the real NSFW, as the SE network
     * doesn't exactly allow swearing (it'll get the bot banned when in the SE network).
     *
     * Whether or not this needs to be used separately depends on whether or not what the bot says being NSFW
     * doesn't matter. If it can say anything on a given site without problems this method can be removed in general.
     * But since this also is meant to be used with the SE network, it isn't going to work
     */
    public void loadNSFW(){

    }

    public List<BMessage> parseMessage(String message, User user) throws IOException{
        if(message == null)
            return null;

        if(!isCommand(message))
            return null;


        message = message.replaceFirst(TRIGGER, "");

        List<BMessage> replies = new ArrayList<>();

        for(Command c : commands){
            BMessage x = c.handleCommand(message, user);
            if(x != null) {
                replies.add(x);
            }
        }

        if(replies.size() == 0)
            replies = null;

        return replies;
    }

    public static boolean isCommand(String input){
        return input.startsWith(TRIGGER);
    }

    public static String[] splitCommand(String input, String commandName){
        String[] retVal = new String[2];
        retVal[0] = commandName;
        retVal[1] = input.replace(commandName + " ", "");
        return retVal;
    }

    public void manualCommandInjection(Command c){
        if(c == null)
            return;

        commands.add(c);
    }
}
