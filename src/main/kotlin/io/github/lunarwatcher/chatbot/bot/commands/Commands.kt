package io.github.lunarwatcher.chatbot.bot.commands

import com.google.common.base.Strings.repeat
import io.github.lunarwatcher.chatbot.Constants
import io.github.lunarwatcher.chatbot.bot.ReplyBuilder
import io.github.lunarwatcher.chatbot.bot.chat.BMessage
import io.github.lunarwatcher.chatbot.bot.command.CommandCenter
import io.github.lunarwatcher.chatbot.bot.command.CommandCenter.TRIGGER

interface Command{
    val name: String;
    val aliases: List<String>
    val desc: String;
    val help: String;

    /**
     * Check if the input starts with the name or one of the command's aliases
     */
    fun matchesCommand(input: String) : Boolean;
    /**
     * Handle a given command
     */
    fun handleCommand(input: String, user: User) : BMessage?;

}

/**
 * Info about a user.
 */
class User(var site: String, var userID: Long, var userName: String, var roomID: Int, var nsfwSite: Boolean = false);

/**
 * Utility implementation of [Command]
 */
abstract class AbstractCommand(override val name: String, override val aliases: List<String>,
                               override val desc: String = Constants.NO_DESCRIPTION,
                               override val help: String = Constants.NO_HELP) : Command{

    override fun matchesCommand(input: String): Boolean{
        val input = input.toLowerCase();
        val split = input.split(" ");
        if(split[0] == name.toLowerCase()){
            return true;
        }

        return aliases.any{split[0] == it.toLowerCase()}
    }

    fun parseArguments(input: String) : List<String>?{
        if(input.replace(name, "").isEmpty()){
            //no arguments passed
            return null;
        }

        val split = input.split(" ", limit = 2)[1]
        //If a match isn't found, it ends up the exactly same as split
        var multiArgs = split.split("&quot; &quot;")
        val multiArgs2 = split.split("\" \"")
        if(multiArgs != multiArgs2){
            for(x in multiArgs){
                if(x.contains("\"")){
                    multiArgs = multiArgs2;
                    break;
                }
            }
        }
        if(multiArgs.size == 1){
            //Avoid IndexOutOfBounds by splitting the check in two
            if(multiArgs[0] == split){
                return listOf(split.replace("\"", "").replace("&quot;", ""));
            }
        }
        val returnList: MutableList<String> = mutableListOf()
        multiArgs.forEach{returnList.add(it.replace("\"", "").replace("&quot;", ""))}

        return returnList;
    }
}

class HelpCommand(var center: CommandCenter) : AbstractCommand("help", listOf(),
        "Lists all the commands the bot has",
        "Use `" + CommandCenter.TRIGGER + "help` to list all the commands and `" + CommandCenter.TRIGGER + "help [command name]` to get more info about a specifc command"){

    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input)){
            return null;
        }


        //No arguments supplied
        val reply = ReplyBuilder(center.site.name == "discord");
        reply.fixedInput().append("###################### Help ######################")
                .nl().fixedInput().nl();
        val commands: MutableMap<String, String> = mutableMapOf()
        val learnedCommands: MutableMap<String, String> = mutableMapOf()

        val names: MutableList<String> = mutableListOf()

        if(!center.commands.isEmpty()) {

            for (command: Command in center.commands) {
                commands.put(command.name, command.desc);
            }
        }

        if(!CommandCenter.tc.commands.isEmpty()){
            for(cmd: LearnedCommand in CommandCenter.tc.commands){
                learnedCommands.put(cmd.name, cmd.desc)
            }
        }

        names.addAll(commands.keys);
        names.addAll(learnedCommands.keys)

        val maxLen = getMaxLen(names);

        if(!commands.isEmpty()) {
            reply.fixedInput().append("==================== Commands").nl()
            for (command in commands) {
                reply.fixedInput().append(TRIGGER + command.key);
                reply.append(repeat(" ", maxLen - command.key.length + 2) + "| ")
                        .append(command.value).nl();
            }
        }

        if(!learnedCommands.isEmpty()){
            reply.fixedInput().append("==================== Learned Commands").nl()
            for (command in CommandCenter.tc.commands) {
                if(command.nsfw && !user.nsfwSite){
                    continue;
                }

                reply.fixedInput().append(TRIGGER + command.name);
                reply.append(repeat(" ", maxLen - command.name.length + 2) + "| ")
                        .append(command.desc);
                if(command.nsfw)
                    reply.append(" - NSFW");
                reply.nl();
            }
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

class Alive : AbstractCommand("alive", listOf(), "Used to check if the bot is working"){
    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input))
            return null;

        return BMessage("I'm alive. Are you?", true);
    }
}