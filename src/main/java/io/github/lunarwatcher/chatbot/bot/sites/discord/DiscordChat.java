package io.github.lunarwatcher.chatbot.bot.sites.discord;

import io.github.lunarwatcher.chatbot.Database;
import io.github.lunarwatcher.chatbot.Site;
import io.github.lunarwatcher.chatbot.bot.chat.BMessage;
import io.github.lunarwatcher.chatbot.bot.chat.Message;
import io.github.lunarwatcher.chatbot.bot.command.CommandCenter;
import io.github.lunarwatcher.chatbot.bot.commands.AbstractCommand;
import io.github.lunarwatcher.chatbot.bot.commands.Command;
import io.github.lunarwatcher.chatbot.bot.sites.Chat;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IGuild;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.lunarwatcher.chatbot.bot.command.CommandCenter.TRIGGER;

public class DiscordChat implements Chat{
    Site site;
    CommandCenter commands;
    IDiscordClient client;
    Properties botProps;
    @Getter
    private Database db;
    public static Map<String, Pattern> regMatch = new HashMap<>();
    private List<RMatch> regex;

    public DiscordChat(Site site, Properties botProps, Database db) throws IOException {
        this.site = site;
        this.db = db;
        this.botProps = botProps;
        logIn();
        commands = new CommandCenter(botProps);
        commands.loadNSFW();
        regex = new ArrayList<>();

        for(IGuild guild : client.getGuilds()){
            System.out.println(guild.getName());
        }

    }

    public void load(){

    }

    public void save(){

    }

    @Override
    public void logIn() throws IOException {
        client = new ClientBuilder()
                .withToken(site.getConfig().getEmail())
                .online()
                .build();
        client.getDispatcher().registerListener(this);
        client.login();

    }

    @Override
    public void sendMessage(Message message) throws IOException {

    }

    @Override
    public void receiveMessage(Message message) {

    }

    @Override
    public void rawReceive(String input) {

    }

    @Override
    public boolean deleteMessage() {
        return false;
    }

    @Override
    public boolean editMessage() {
        return false;
    }

    @Override
    public void listen() {

    }

    @Override
    public void joinRoom(long id) {

    }

    @Override
    public void leaveRoom(long id) {

    }

    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event){
        String msg = event.getMessage().getContent();
        System.out.println(msg);

        if(CommandCenter.isCommand(msg)){
            if(msg.startsWith(TRIGGER + "stats")){
                String cmd = msg.replace(TRIGGER + "stats ", "");
                RMatch match = null;

                for(RMatch m : regex){
                    if(m.usern.toLowerCase().equals(cmd.toLowerCase())){
                        match = m;
                        break;
                    }
                }

                if(match != null){
                    event.getChannel().sendMessage(match.message());
                }else{
                    event.getChannel().sendMessage("User not listed. Yet :smirk:");
                }
            }else {
                try {
                    List<BMessage> replies = commands.parseMessage(msg);
                    if(replies == null){
                        if(msg.contains("slut") && msg.contains("zoe")){
                            event.getChannel().sendMessage("Fuck you :D");
                        }else
                            event.getChannel().sendMessage("Look up the manual maybe?");
                    }else {
                        for (BMessage r : replies) {
                            event.getChannel().sendMessage(r.content);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }else{
            long uid = event.getAuthor().getLongID();
            String uname = event.getAuthor().getName();
            System.out.println("(" + uid + ")<" + uname + ">");
            RMatch u = null;

            for(RMatch m : regex){
                if(m.userid == uid){
                    u = m;
                }
            }

            if(u == null){
                u = new RMatch(uid, uname);
                regex.add(u);
            }

            u.match(event.getMessage().getContent());

        }
    }

    public class RMatch{
        public long userid;
        public String usern;
        public Map<String, Long> occurences;
        public long totalMessages;
        public long hits;

        public RMatch(long uid, String name){
            this.userid = uid;
            this.usern = name;

            occurences = new HashMap<>();


            occurences.put("geis+?noo+?b", 0L);
            occurences.put("geis+", 0L);
            occurences.put("noo+?b", 0L);
            occurences.put("lo+l", 0L);
            occurences.put("lmf*?ao+", 0L);
            occurences.put("(ha+(ha+)+)", 0L);

            if(regMatch.size() == 0){
                for(Map.Entry<String, Long> occurence : occurences.entrySet()){
                    regMatch.put(occurence.getKey(), Pattern.compile(occurence.getKey()));

                }
            }
        }

        public void match(String input){
            totalMessages++;
            for(Map.Entry<String, Pattern> reg : regMatch.entrySet()){
                Pattern p = reg.getValue();

                Matcher m = p.matcher(input);
                if (m.find()) {
                    occurences.put(reg.getKey(), occurences.get(reg.getKey()) + 1);
                    System.out.println("Matched for " + reg.getValue());
                    hits++;
                    break;
                }
            }
        }

        public String message(){
            StringBuilder sb = new StringBuilder();
            sb.append("Regex reactions for user \"" + usern + "\"").append("\n");
            for(Map.Entry<String, Long> e : occurences.entrySet()){
                sb.append(e.getKey() + " - : - " + e.getValue()).append("\n");
            }
            sb.append("This user sent ").append(totalMessages).append(" in which there were found ").append(hits).append(" matches.").append("\n");
            sb.append("The total match rate is ").append((double)((double)hits / (double)totalMessages) * 100).append("%").append("\n");
            return sb.toString();
        }
    }

    public static class Match extends AbstractCommand {

        public Match() {
            super("stats", null, "Get the status for a user", TRIGGER + "stats <username>");
        }

        @Override
        public BMessage handleCommand(@NotNull String input) {
            return null;
        }
    }
}