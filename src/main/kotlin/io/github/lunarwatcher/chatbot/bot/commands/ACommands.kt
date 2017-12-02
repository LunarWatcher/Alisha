package io.github.lunarwatcher.chatbot.bot.commands

import io.github.lunarwatcher.chatbot.Constants
import io.github.lunarwatcher.chatbot.bot.chat.BMessage
import io.github.lunarwatcher.chatbot.bot.command.CommandCenter
import io.github.lunarwatcher.chatbot.bot.command.CommandCenter.tc
import io.github.lunarwatcher.chatbot.bot.sites.Chat
import io.github.lunarwatcher.chatbot.bot.sites.se.SEChat
import io.github.lunarwatcher.chatbot.utils.Utils
import sun.java2d.cmm.kcms.CMM.checkStatus

class AddHome(val site: SEChat) : AbstractCommand("home", listOf(),
        "Adds a home room - Admins only", "Adds a home room for the bot on this site"){
    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input))
            return null;
        if(!Utils.isAdmin(user.userID, site.config)){
            return BMessage("I'm afraid I can't let you do that, User", true);
        }

        val raw = input.split(" ");
        var iRoom: Int;
        if(raw.size == 1)
            iRoom = user.roomID
        else
            iRoom = raw[1].toInt()

        val added = site.config.addHomeRoom(iRoom);

        if(!added){
            return BMessage("Room was not added as a home room", true);
        }else{
            site.joinRoom(iRoom);
        }
        return BMessage("Room added as a home room", true);
    }
}

class RemoveHome(val site: SEChat) : AbstractCommand("remhome", listOf(),
        "Removes a home room - Admins only", "Removes a home room for the bot on this site"){
    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input))
            return null;
        if(!Utils.isAdmin(user.userID, site.config)){
            return BMessage("I'm afraid I can't let you do that, User", true);
        }

        val raw = input.split(" ");
        var iRoom: Int;
        iRoom = if(raw.size == 1)
            user.roomID
        else
            raw[1].toInt()

        if(Utils.isHardcodedRoom(iRoom, site)){
            return BMessage("Unfortunately for you, that's a hard-coded room. These cannot be removed by command. " +
                    "They are listed in bot.properties if you want to remove it. Please note that if there are no rooms supplied, " +
                    "it defaults to one even if it's empty", true);
        }

        val added = site.config.removeHomeRoom(iRoom);

        if(!added){
            return BMessage("Room was not removed as a home room", true);
        }

        if(Constants.LEAVE_ROOM_ON_UNHOME){
            val bmwrap = site.leaveRoom(iRoom);
            return if(!bmwrap){
                BMessage("Room was removed as a home room, but I could not leave the room!", true)
            }else{
                BMessage("Room successfully removed as a home room and left", true);
            }

        }
        return BMessage("Room removed as a home room", true);
    }
}

class AddAdmin(val site: Chat) : AbstractCommand("admin", listOf(), "Adds an admin to the bot. Only usable by hardcoded bot admins"){
    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input))
            return null;
        if(!Utils.isHardcodedAdmin(user.userID, site)){
            return BMessage("You don't have the rights required to do that", true);
        }

        val type = input.replace(name + " ", "");
        if(type == input)
            return BMessage("Specify a user to add -_-", true);

        if(type.isEmpty()){
            return BMessage("Define a user to add", true);
        }
        val iUser: Long
        try {
            iUser = type.split(" ")[0].toLong()

        }catch(e: ClassCastException){
            return BMessage("Not a valid user ID!", true);
        }



        val response = site.config.addAdmin(iUser);

        return when(response.code){
            EXISTED -> BMessage("User is already an admin", true);
            ADDED -> BMessage("Successfully added new admin", true);
            BANNED -> BMessage("USer not added, (s)he is banned", true);
            else -> BMessage("User is hard-coded and can't be added", true)
        }
    }
}

class RemoveAdmin(val site: Chat) : AbstractCommand("remadmin", listOf(), "Removes an admin from the bot. Only usable by hardcoded bot admins"){
    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input))
            return null;
        if(!Utils.isHardcodedAdmin(user.userID, site)){
            return BMessage("You don't have the rights required to do that", true);
        }

        val type = input.replace(name + " ", "");
        if(type == input)
            return BMessage("Specify a user to remove -_-", true);

        if(type.isEmpty()){
            return BMessage("Specify a user to remove -_-", true);
        }
        val iUser: Long
        try {
            iUser = type.split(" ")[0].toLong()

        }catch(e: ClassCastException){
            return BMessage("Not a valid user ID!", true);
        }

        val response = site.config.removeAdmin(iUser);
        return when(response.code){
            EXISTED -> BMessage("User wasn't an admin in the first place", true);
            ADDED -> BMessage("Successfully removed admin", true);
            else -> BMessage("User is hard-coded and can't be removed as an admin", true)
        }
    }
}

class BanUser(val site: Chat) : AbstractCommand("ban", listOf(), "Bans a user from using the bot. Only usable by hardcoded bot admins"){
    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input))
            return null;
        if(!Utils.isHardcodedAdmin(user.userID, site)){
            return BMessage("You don't have the rights required to do that", true);
        }


        val type = input.replace(name + " ", "");
        if(type == input)
            return BMessage("Specify a user to add -_-", true);

        if(type.isEmpty()){
            return BMessage("Define a user to add", true);
        }
        val iUser: Long
        try {
            iUser = type.split(" ")[0].toLong()

        }catch(e: ClassCastException){
            return BMessage("Not a valid user ID!", true);
        }

        if(Utils.isHardcodedAdmin(iUser, site)){
            return BMessage("You can't ban other hardcoded moderators", true);
        }
        if(Utils.isAdmin(iUser, site.config) && !Utils.isHardcodedAdmin(user.userID, site)){
            return BMessage("You can't ban other moderators -_-", true);
        }

        val response = site.config.ban(iUser);

        return when(response.code){
            EXISTED -> BMessage("User is already banned", true);
            ADDED -> BMessage("User was successfully banned", true);
            else -> BMessage("User is hard-coded and can't be banned", true)
        }
    }
}

class Unban(val site: Chat) : AbstractCommand("unban", listOf(), "Unbans a banned user. Only usable by hardcoded bot admins"){
    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input))
            return null;
        if(!Utils.isAdmin(user.userID, site.config)){
            return BMessage("You don't have the rights required to do that", true);
        }

        val type = input.replace(name + " ", "");
        if(type == input)
            return BMessage("Specify a user to remove -_-", true);

        if(type.isEmpty()){
            return BMessage("Specify a user to remove -_-", true);
        }
        val iUser: Long
        try {
            iUser = type.split(" ")[0].toLong()

        }catch(e: ClassCastException){
            return BMessage("Not a valid user ID!", true);
        }

        val response = site.config.unban(iUser);
        return when(response.code){
            EXISTED -> BMessage("The user you tried to unban isn't banned", true);
            ADDED -> BMessage("User successfully banned", true);
            else -> BMessage("If you see this in chat, something has gone extremely wrong", true)
        }
    }
}

class SaveCommand(val site: Chat) : AbstractCommand("save", listOf(), "Saves the database"){
    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input))
            return null;
        if(!Utils.isAdmin(user.userID, site.config))
            return BMessage("I'm afraid I can't let you do that User", true)

        site.database.commit();

        return BMessage("Saved.", true);
    }
}

class WhoMade(val commands: CommandCenter) : AbstractCommand("WhoMade", listOf(), "Gets the user ID of the user who created a command"){
    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input))
            return null;
        try {
            val arg = parseArguments(input)?.get(0) ?: return null;

            if(commands.isBuiltIn(arg)){
                return BMessage("It's a built-in command, meaning it was made by the project developer(s)", true);
            }

            if(CommandCenter.tc.doesCommandExist(arg)){
                CommandCenter.tc.commands
                        .forEach {
                            if(it.name == arg)
                                return BMessage("The command `" + arg + "` was made by a user with the User ID "
                                        + it.creator + ". The command was created on " +
                                        (if(it.site == "Unknown") "an unknown site"
                                        else it.site) + ".", true)
                        }
            }
        }catch(e: ClassCastException){
            return BMessage(e.message, false);
        }

        return BMessage("That command doesn't appear to exist.", true);
    }
}