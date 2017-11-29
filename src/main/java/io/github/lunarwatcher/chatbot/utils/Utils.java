package io.github.lunarwatcher.chatbot.utils;

import io.github.lunarwatcher.chatbot.Constants;
import io.github.lunarwatcher.chatbot.Database;
import io.github.lunarwatcher.chatbot.bot.commands.BotConfig;
import io.github.lunarwatcher.chatbot.bot.commands.UserInfo;
import org.jetbrains.annotations.Nullable;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Utils {
    private static final String fkeyHtmlRegex = "name=\"fkey\"\\s+(?>type=\"hidden\"\\s+)?value=\"([^\"]+)\"";
    /**
     * Regex for the fKey in HTML
     */

    private static final Pattern fkeyRegex = Pattern.compile(fkeyHtmlRegex);

    public static Random random;

    static {
        random = new Random(System.currentTimeMillis());
    }

    /**
     * Hidden constructor so this class can't be initialized
     */
    private Utils() {}

    /**
     * Inverts a boolean
     * @param input
     * @return
     */
    public static boolean invertBoolean(boolean input){
        return input ? false : true;
    }

    /**
     * Universal assertion technique that doesn't require the -enableAssertions flag
     * @param input Boolean check, throws exception if false
     * @param message Optional message
     */
    public static void assertion(boolean input, @Nullable String message){
        if(!input)
            throw new RuntimeException("Assertion failed! " + (message != null ? message : "No message provided"));
    }

    /**
     * Finds the fKey in a given input. It is used to handle requests to the API.
     * @param input the (most likely) HTML to find an fKey in
     * @return The fKey or null if it isn't found
     */
    public static String parseHtml(String input){
        Matcher m = fkeyRegex.matcher(input);
        return m.find() ? m.group(1) : null;
    }

    public static void sendMessage(IChannel channel, String message){
        RequestBuffer.request(() -> {
            try{
                channel.sendMessage(message);
            } catch (DiscordException e){
                System.err.println("Message could not be sent with error: ");
                e.printStackTrace();
            }catch(Exception e){
                e.printStackTrace();
            }
        });

    }

    public static String getRandomJoinMessage(){
        return Constants.joinMessages[random.nextInt(Constants.joinMessages.length)];
    }

    public static String getRandomLeaveMessage(){
        return Constants.leaveMessages[random.nextInt(Constants.leaveMessages.length)];
    }

    public static void saveConfig(BotConfig cf, Database db){
        List<Long> banned = cf.getBanned();
        List<Long> admin = cf.getAdmins();
        List<Long> priv = cf.getPrivelege();
        List<Integer> homes = cf.getHomes();

        String site = cf.getSite();

        db.put(Constants.HOME_ROOMS(site), homes);
        db.put(Constants.ADMIN_USERS(site), admin);
        db.put(Constants.PRIVILEGE_USERS(site), priv);
        db.put(Constants.BANNED_USERS(site), banned);

        db.commit();
    }

    public static void loadConfig(BotConfig cf, Database db){
        String site = cf.getSite();
        //Possible ClassCastException can occur from this
        try {
            List<Integer> homes = (List<Integer>) db.get(Constants.HOME_ROOMS(site));
            List<Long> admins = (List<Long>) db.get(Constants.ADMIN_USERS(site));
            List<Long> prived = (List<Long>) db.get(Constants.PRIVILEGE_USERS(site));
            List<Long> banned = (List<Long>) db.get(Constants.BANNED_USERS(site));
            cf.set(homes, admins, prived, banned);
        }catch(Exception e){
            e.printStackTrace();

        }
    }

    //Utility method for checking etc classes defined in BotConfig

    public static boolean isAdmin(long user, BotConfig conf){
        for(Long u : conf.getAdmins()){
            if(u == user){
                return true;
            }
        }
        return false;
    }

    public static boolean isBanned(long user, BotConfig conf){
        for(Long u : conf.getBanned()){
            if(u == user){
                return true;
            }
        }
        return false;
    }

    public static boolean isPriv(long user, BotConfig conf){
        for(Long u : conf.getPrivelege()){
            if(u == user){
                return true;
            }
        }
        return false;
    }

    public static boolean isHome(int room, BotConfig conf){
        for(int u : conf.getHomes()){
            if(u == room){
                return true;
            }
        }
        return false;
    }

}
