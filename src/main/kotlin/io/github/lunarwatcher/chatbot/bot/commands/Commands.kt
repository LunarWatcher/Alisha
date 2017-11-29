package io.github.lunarwatcher.chatbot.bot.commands

import io.github.lunarwatcher.chatbot.Constants
import io.github.lunarwatcher.chatbot.bot.ReplyBuilder
import io.github.lunarwatcher.chatbot.bot.chat.BMessage
import io.github.lunarwatcher.chatbot.bot.command.CommandCenter
import io.github.lunarwatcher.chatbot.bot.command.CommandCenter.TRIGGER
import io.github.lunarwatcher.chatbot.bot.command.CommandCenter.isCommand
import io.github.lunarwatcher.chatbot.bot.sites.se.SEChat
import io.github.lunarwatcher.chatbot.utils.Utils
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

    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input)){
            return null;
        }


        //No arguments supplied
        val reply: ReplyBuilder = ReplyBuilder();
        reply.fixedInput().append("#################### Commands ####################")
                .nl().fixedInput().append("--------------------------------------------------")
                .nl();
        for (command: Command in center.commands) {
            reply.fixedInput().append(command.getName()).fixedInput().append("|").fixedInput().append(command.getDescription()).nl()
        }

        return BMessage(reply.toString(), false);

    }

}

class ShrugCommand(val shrug: String): AbstractCommand("shrug", arrayOf("dunno", "what"), "Shrugs", "Use `" + TRIGGER + "shrug` to use the command"){
    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input)){
            return null;
        }
        return BMessage(shrug, false);
    }
}

class Summon(val votes: Int, val chat: SEChat) : AbstractCommand("summon", arrayOf("join"), "Summon the bot to a room", "Joins a room after $votes votes"){
    var vts: MutableMap<Int, MutableList<Long>> = mutableMapOf();

    override fun handleCommand(input: String, user: User): BMessage? {

        if(!matchesCommand(input)){
            return null;
        }

        var votes = this.votes;

        if(Utils.isAdmin(user.userID, chat.config))
            votes = 1;

        try{
            val raw = input.split(" ")[1];
            val iRoom = raw.toInt();

            for(room in chat.getRooms()){
                if(room.id == iRoom){
                    return BMessage("I'm already in that room", true);
                }
            }

            var users: MutableList<Long>? = vts.get(iRoom);

            if(users == null){
                vts.put(iRoom, mutableListOf(user.userID))
                users = vts.get(iRoom);

            }else{

                for(uid in users){
                    if(uid == user.userID){
                        return BMessage("Can't vote multiple times for joining :D", true);
                    }
                }
                users.add(user.userID);
                vts.put(iRoom,users);
            }

            if(users!!.size >= votes){
                var message: SEChat.BMWrapper = chat.joinRoom(iRoom);
                vts.remove(iRoom);

                if(!message.exception) {
                    return message;
                }else{

                    return BMessage(Utils.getRandomJoinMessage(), true)
                }

            }else{
                return BMessage((votes - users.size).toString() + " more " + (if(votes - users.size == 1 ) "vote" else "votes") + " required", true);
            }

        }catch (e: IndexOutOfBoundsException){
            return BMessage("You have to specify a room...", true);
        }catch(e: ClassCastException){
            return BMessage("That's not a valid room ID", true);
        }catch(e: Exception){
            return BMessage("Something bad happened :/", true);
        }
    }
}

class UnSummon(val votes: Int, val chat: SEChat) : AbstractCommand("unsummon", arrayOf("leave"), "Makes the bot leave a specified room", "Leaves a room after $votes votes"){
    var vts: MutableMap<Int, MutableList<Long>> = mutableMapOf();

    override fun handleCommand(input: String, user: User): BMessage? {

        if(!matchesCommand(input)){
            return null;
        }

        var votes = this.votes;

        if(Utils.isAdmin(user.userID, chat.config))
            votes = 1;

        try{
            val raw = input.split(" ")[1];
            val iRoom = raw.toInt();

            var match = false;

            for(room in chat.getRooms()){
                if(room.id == iRoom){
                    match = true;
                }
            }

            if(match == false){
                return BMessage("I'm not in that room...", true);
            }

            var users: MutableList<Long>? = vts.get(iRoom);

            if(users == null){
                vts.put(iRoom, mutableListOf(user.userID))
                users = vts.get(iRoom);

            }else{

                for(uid in users){
                    if(uid == user.userID){
                        return BMessage("Can't vote multiple times for leaving :D", true);
                    }
                }
                users.add(user.userID);
                vts.put(iRoom,users);
            }

            if(users!!.size >= votes){
                val succeeded = chat.leaveRoom(iRoom);

                vts.remove(iRoom);
                if(!succeeded){
                    return BMessage("Something happened when trying to leave", true);
                }else{
                    return BMessage(Utils.getRandomJoinMessage(), true)
                }

            }else{
                return BMessage((votes - users.size).toString() + " more " + (if(votes - users.size == 1 ) "vote" else "votes") + " required", true);
            }

        }catch (e: IndexOutOfBoundsException){
            return BMessage("You have to specify a room...", true);
        }catch(e: ClassCastException){
            return BMessage("That's not a valid room ID", true);
        }catch(e: Exception){
            return BMessage("Something bad happened :/", true);
        }
    }
}

