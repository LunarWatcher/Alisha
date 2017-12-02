package io.github.lunarwatcher.chatbot;

public final class Constants {
    public static final boolean AUTO_BOOT = false;
    public static boolean LEAVE_ROOM_ON_UNHOME;
    public static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";
    public static final String IDENTIFIER_USERNAME = "name";
    public static final String IDENTIFIER_EMAIL = "email";
    public static final String IDENTIFIER_PASSWORD = "password";
    public static final String IDENTIFIER_ID = "id";
    public static final String DEFAULT_DATABASE = "memory.json";
    public static final String NO_HELP = "No help was supplied for this command";
    public static final String NO_DESCRIPTION = "No description was supplied for this command";
    public static final long SAVE_INTERVAL = 30000;
    public static final int RELOCATION_VOTES = 3;
    public static final String LEARNED_COMMANDS = "learned";
    public static final boolean DEFAULT_NSFW = true;

    public static final String[] joinMessages = {
            "As you command, I'll leave for that room as soon as possible",
            "Joined",
            "You can't tell me what to do! Oh wait, you can? I guess I should get over there...",
            "Are there going to be quests there too? :D"
    };

    public static final String[] leaveMessages = {
            "Less to worry about I guess",
            "But I really liked it there!",
            "I wasn't finished with my quest yet!",
            "Alright, left the room"
    };

    public static final String[] hrMessages = {
            "I can't *just* leave my HOME!",
            "You can't make me",
            "There are still quests to finish, I can't leave yet!",
            "Yeah... not doing that."
    };

    public static final String[] learnedMessages = {
            "The more I learn, the better",
            "Oh, THAT's how I do that!",
            "I already knew that! Yeah... *coughs*"
    };

    public static final String[] forgotMessage = {
            "Forgotten",
            "I can't remember that one"
    };

    public static String BANNED_USERS(String site){
        return "banned-users-" + site;
    }

    public static String ADMIN_USERS(String site){
        return "admin-users-" + site;
    }

    public static String PRIVILEGE_USERS(String site){
        return "privilege-users-" + site;
    }

    public static String HOME_ROOMS(String site){
        return "home-rooms-" + site;
    }

}
