package io.github.lunarwatcher.chatbot.bot.commands

import io.github.lunarwatcher.chatbot.Constants
import io.github.lunarwatcher.chatbot.bot.chat.BMessage
import io.github.lunarwatcher.chatbot.bot.sites.se.SEChat
import io.github.lunarwatcher.chatbot.utils.Utils

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
        if(raw.size == 1)
            iRoom = user.roomID
        else
            iRoom = raw[1].toInt()

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
            if(!bmwrap){
                return BMessage("Room was removed as a home room, but I could not leave the room!", true)
            }else{
                return BMessage("Room successfully removed as a home room and left", true);
            }

        }
        return BMessage("Room removed as a home room", true);
    }
}

