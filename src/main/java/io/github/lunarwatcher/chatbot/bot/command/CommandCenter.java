package io.github.lunarwatcher.chatbot.bot.command;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.lunarwatcher.chatbot.Constants;
import io.github.lunarwatcher.chatbot.Database;
import io.github.lunarwatcher.chatbot.MapUtils;
import io.github.lunarwatcher.chatbot.bot.Bot;
import io.github.lunarwatcher.chatbot.bot.chat.BMessage;
import io.github.lunarwatcher.chatbot.bot.chat.Message;
import io.github.lunarwatcher.chatbot.bot.commands.*;
import io.github.lunarwatcher.chatbot.bot.listener.*;
import io.github.lunarwatcher.chatbot.bot.sites.Chat;
import io.github.lunarwatcher.chatbot.bot.sites.discord.DiscordChat;
import io.github.lunarwatcher.chatbot.bot.sites.se.SEChat;
import lombok.Getter;
import lombok.NonNull;

import javax.print.DocFlavor;
import java.io.IOException;
import java.util.*;

import static io.github.lunarwatcher.chatbot.Constants.RELOCATION_VOTES;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class CommandCenter {
    public static String TRIGGER;

    @Getter
    public Map<CmdInfo, Command> commands;
    public List<Listener> listeners;
    //List<Listener> listeners;
    public Chat site;
    public static TaughtCommands tc;
    public static Bot bot;
    public Database db;

    public CommandCenter(Properties botProps, boolean shrugAlt, Chat site) {
        this.db = site.getDatabase();
        if (tc == null) {
            tc = new TaughtCommands(db);
        }
        this.site = site;
        TRIGGER = botProps.getProperty("bot.trigger");
        commands = new HashMap<>();
        addCommand(new HelpCommand(this));
        addCommand(new ShrugCommand(shrugAlt ? "¯\\\\_(ツ)_/¯" : "¯\\_(ツ)_/¯"));
        addCommand(new AboutCommand());
        addCommand(new Learn(tc, this));
        addCommand(new UnLearn(tc, this));
        addCommand(new UpdateRank(site));
        addCommand(new CheckCommand(site));
        addCommand(new BanUser(site));
        addCommand(new Unban(site));
        addCommand(new SaveCommand(site));
        addCommand(new Alive());
        addCommand(new WhoMade(this));
        addCommand(new ChangeCommandStatus(this));
        addCommand(new RandomNumber());
        addCommand(new LMGTFY());
        addCommand(new UpdateRank(site));
        addCommand(new DebugRanks(site));
        addCommand(new Kill(site));
        addCommand(new Lick(site));

        listeners = new ArrayList<>();
        listeners.add(new WaveListener());
        MentionListener ml = new MentionListener(site);
        listeners.add(new KnockKnock(ml));
        listeners.add(new Train(5));
        listeners.add(ml);

    }

    public void loadSE() {
        if(site instanceof SEChat){
            addCommand(new Summon(RELOCATION_VOTES, (SEChat) site));
            addCommand(new UnSummon(RELOCATION_VOTES, (SEChat) site));
            addCommand(new AddHome((SEChat) site));
            addCommand(new RemoveHome((SEChat) site));
        }
    }

    public void loadDiscord() {

        addCommand(new DiscordChat.Match());
        if(site instanceof DiscordChat) {
            addCommand(new NSFWState((DiscordChat) site));
        }
    }

    /**
     * method used to load commands/listeners that are considered NSFW on some sites. This doesn't necessarily mean actually
     * NSFW, it basically means the commands that aren't wanted on sites like StackExchange. On most Discord servers
     * it's different so that's enabled by default.
     * <p>
     * NSFW is up to whoever forks this bot, the method could even be called from the constructor to automatically
     * load it. The general usage here is for anything that involves swearing and the real NSFW, as the SE network
     * doesn't exactly allow swearing (it'll get the bot banned when in the SE network).
     * <p>
     * Whether or not this needs to be used separately depends on whether or not what the bot says being NSFW
     * doesn't matter. If it can say anything on a given site without problems this method can be removed in general.
     * But since this also is meant to be used with the SE network, it isn't going to work
     * <p>
     * These are the hard-coded ones that are unwanted in the SE network and similar sites
     */
    public void loadNSFW() {

    }

    public List<BMessage> parseListener(String message, User user, boolean nsfw) throws IOException{
        if (message == null)
            return null;
        message = message.replace("&#8238;", "");
        message = message.replace("\u202E", "");
        message = message.trim();
        String om = message;
        List<BMessage> replies = new ArrayList<>();

        for(Listener l : listeners){
            BMessage x = l.handleInput(om, user);
            if(x != null){
                replies.add(x);
            }
        }

        if(replies.size() == 0)
            replies = null;

        return replies;
    }


    public List<BMessage> parseMessage(String message, User user, boolean nsfw) throws IOException {
        if (message == null)
            return null;
        message = message.replace("&#8238;", "");
        message = message.replace("\u202E", "");
        message = message.trim();
        message = message.replaceAll(" +", " ");
        String om = message;
        List<BMessage> replies = new ArrayList<>();
        if(isCommand(message)) {
            message = message.substring(TRIGGER.length());

            //Get rid of white space to avoid problems down the line
            message = message.trim();


            String name = message.split(" ")[0];

            Command c = get(name);
            if (c != null) {
                BMessage x = c.handleCommand(message, user);
                if (x != null) {
                    //There are still some commands that could return null here
                    replies.add(x);
                }
            }

            LearnedCommand lc = tc.get(name);
            if (lc != null) {
                //If the command is NSFW but the site doesn't allow it, don't handle the command
                if (lc.getNsfw() && !nsfw)
                    System.out.println("command ignored");
                else {

                    BMessage x = lc.handleCommand(message, user);
                    if (x != null)
                        replies.add(x);
                }
            }
        }

        for(Listener l : listeners){
            BMessage x = l.handleInput(om, user);
            if(x != null){
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

        addCommand(c);
    }

    public boolean isBuiltIn(String cmdName){
        if(cmdName == null)
            return false;
        return get(cmdName) != null;
    }

    public static void save(){
        if(tc != null)
            tc.save();
    }

    public void addCommand(Command c){
        String name = c.getName();
        List<String> aliases = c.getAliases();
        commands.putIfAbsent(new CmdInfo(name, aliases), c);
    }

    public Command get(String key){
        return (Command) MapUtils.Companion.get(key, commands);
    }

    public Command get(CmdInfo key){
        return (Command) MapUtils.Companion.get(key, commands);
    }

    public void hookupToRanks(long user, String username){
        if(site.getConfig().getRank(user) == null){
            //This code exists in an attempt to map every. Single. User. who uses the bot or even talk around it
            //This will build up a fairly big database, but that's why there is (going to be) a purge method
            //for the database
            site.getConfig().addRank(user, Constants.DEFAULT_RANK, username);
        }else{
            //Wrong inspection from Java here. There will not be any NPE's as the rank retrieved can't be null if it does into this
            //statement
            if(site.getConfig().getRank(user).getUsername() == null
                    || !site.getConfig().getRank(user).getUsername().equals(username)){
                site.getConfig().addRank(user, site.getConfig().getRank(user).getRank(), username);
            }
        }
    }
}
