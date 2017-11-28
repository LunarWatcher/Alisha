package io.github.lunarwatcher.chatbot.utils;

import org.jetbrains.annotations.Nullable;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RequestBuffer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
    private static final String fkeyHtmlRegex = "name=\"fkey\"\\s+(?>type=\"hidden\"\\s+)?value=\"([^\"]+)\"";
    /**
     * Regex for the fKey in HTML
     */

    private static final Pattern fkeyRegex = Pattern.compile(fkeyHtmlRegex);

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

        // This might look weird but it'll be explained in another page.
        RequestBuffer.request(() -> {
            try{
                channel.sendMessage(message);
            } catch (DiscordException e){
                System.err.println("Message could not be sent with error: ");
                e.printStackTrace();
            }
        });

    }

}
