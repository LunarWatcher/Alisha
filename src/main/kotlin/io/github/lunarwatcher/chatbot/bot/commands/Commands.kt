package io.github.lunarwatcher.chatbot.bot.commands

import com.google.common.base.Strings.repeat
import io.github.lunarwatcher.chatbot.Constants
import io.github.lunarwatcher.chatbot.bot.ReplyBuilder
import io.github.lunarwatcher.chatbot.bot.chat.BMessage
import io.github.lunarwatcher.chatbot.bot.command.CommandCenter
import io.github.lunarwatcher.chatbot.bot.command.CommandCenter.TRIGGER

interface Command{
    /**
     * Get the name of the command
     */
    fun getName() : String;

    /**
     * Get the commands aliases
     */
    fun getAliases(): Array<String>?;

    /**
     * Returns a description of the command
     */
    fun getDescription() : String;
    /**
     * Check if the input starts with the name or one of the command's aliases
     */
    fun matchesCommand(input: String) : Boolean;
    /**
     * Handle a given command
     */
    fun handleCommand(input: String, user: User) : BMessage?;

    /**
     * Get the help for a specific command.
     */
    fun getHelp() : String;
}

/**
 * Info about a user.
 */
class User(var userID: Long, var userName: String, var roomID: Int);

//TODO warning: class doesn't work
abstract class AbstractCommand(var name: String, var aliases: List<String>, var desc: String?, var help: String?) : Command{

    override fun matchesCommand(input: String): Boolean{
        val input = input.toLowerCase();
        val split = input.split(" ");
        if(split[0] == name.toLowerCase()){
            return true;
        }

        if (aliases?.size == 0) {
            return false;
        }else{
            for(alias: String in aliases ?: return false){
                if(split[0] == alias.toLowerCase()){
                    return true;
                }
            }
        }

        return false;
    }
}

class HelpCommand(var center: CommandCenter) : AbstractCommand("help", null,
        "Lists all the commands the bot has",
        "Use `" + CommandCenter.TRIGGER + "help` to list all the commands and `" + CommandCenter.TRIGGER + "help [command name]` to get more info about a specifc command"){

    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input)){
            return null;
        }


        //No arguments supplied
        val reply = ReplyBuilder();
        reply.fixedInput().append("###################### Help ######################")
                .nl().fixedInput().nl();
        val commands: MutableMap<String, String> = mutableMapOf()
        val names: MutableList<String> = mutableListOf()

        if(!center.commands.isEmpty()) {
            reply.fixedInput().append("==================== Commands").nl()
            for (command: Command in center.commands) {
                commands.put(command.getName(), command.getDescription());
            }
        }

        names.addAll(commands.keys);
        val maxLen = getMaxLen(names);

        for(command in commands){
            reply.fixedInput().append(TRIGGER + command.key);
            reply.append(repeat(" ", maxLen - command.key.length + 2) + "| ")
                    .append(command.value).nl();
        }
        return BMessage(reply.toString(), false);

    }

}

fun getMaxLen(list: MutableList<String>) : Int{
    var longest = 0;

    for(item in list){
        val len = item.length;
        if(len > longest)
            longest = len;
    }
    return longest;
}

class ShrugCommand(val shrug: String): AbstractCommand("shrug", listOf("dunno", "what"), "Shrugs", "Use `" + TRIGGER + "shrug` to use the command"){
    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input)){
            return null;
        }
        return BMessage(shrug, false);
    }
}

class AboutCommand() : AbstractCommand("about", listOf("whoareyou"), "Info about me", "Use `" + TRIGGER + "about` to show the info"){

    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input)){
            return null;
        }

        val reply: ReplyBuilder = ReplyBuilder();

        reply.append("Hello! I'm Alisha, a chatbot designed by [Zoe](https://stackoverflow.com/users/6296561/zoe).")
                .append("I'm open-source and the code is available on [Github](https://github.com/LunarWatcher/Alisha)")

        return BMessage(reply.toString(), true)
    }
}
