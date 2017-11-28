package io.github.lunarwatcher.chatbot.bot.commands

import io.github.lunarwatcher.chatbot.Constants
import io.github.lunarwatcher.chatbot.bot.ReplyBuilder
import io.github.lunarwatcher.chatbot.bot.chat.BMessage
import io.github.lunarwatcher.chatbot.bot.command.CommandCenter
import io.github.lunarwatcher.chatbot.bot.command.CommandCenter.TRIGGER
import io.github.lunarwatcher.chatbot.utils.Utils.assertion
import java.util.*

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
    fun handleCommand(input: String) : BMessage?;

    /**
     * Get the help for a specific command.
     */
    fun getHelp() : String;
}

abstract class AbstractCommand(var nm: String, var als: Array<String>?, var desc: String?, var hlp: String?) : Command{

    override fun getHelp() : String = hlp ?: Constants.NO_HELP;
    override fun getDescription(): String = desc ?: Constants.NO_DESCRIPTION;
    override fun getName(): String = nm;
    override fun getAliases(): Array<String>? = als;
    override fun matchesCommand(input: String): Boolean{
        val input = input.toLowerCase();

        if(input.startsWith(nm)){
            return true;
        }
        if(als != null) {
            if (als?.size == 0) {
                return false;
            }else{
                for(alias: String in als ?: return false){
                    if(input.startsWith(alias)){
                        return true;
                    }
                }
            }
        }
        return false;
    }
}

class HelpCommand(var center: CommandCenter) : AbstractCommand("help", null,
        "Lists all the commands the bot has",
        "Use `" + CommandCenter.TRIGGER + "help` to list all the commands and `" + CommandCenter.TRIGGER + "help [command name]` to get more info about a specifc command"){

    override fun handleCommand(input: String): BMessage? {
        if(!matchesCommand(input)){
            return null;
        }
        val reply: ReplyBuilder = ReplyBuilder();
        reply.fixedInput().append("#################### Commands ####################")
                .nl().fixedInput().append("--------------------------------------------------")
                .nl();
        for(command: Command in center.commands){
            reply.fixedInput().append(command.getName()).fixedInput().append("|").fixedInput().append(command.getDescription()).nl()
        }

        return BMessage(reply.toString(), false);
    }
}

class ShrugCommand: AbstractCommand("shrug", arrayOf("dunno", "what"), "Shrugs", "Use `" + TRIGGER + "shrug` to use the command"){
    override fun handleCommand(input: String): BMessage? {
        if(!matchesCommand(input)){
            return null;
        }
        return BMessage("¯\\_(ツ)_/¯", false);
    }
}