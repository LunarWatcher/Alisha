package io.github.lunarwatcher.chatbot.bot.commands

import io.github.lunarwatcher.chatbot.bot.chat.BMessage
import io.github.lunarwatcher.chatbot.bot.command.CommandCenter
import io.github.lunarwatcher.chatbot.bot.sites.Chat
import io.github.lunarwatcher.chatbot.bot.sites.se.SEChat
import io.github.lunarwatcher.chatbot.utils.Utils
import sun.java2d.pipe.AAShapePipe

//TODO make this better kotlin
class BotConfig{
    val site: Chat;
    val admins: MutableList<Long>;
    val privelege: MutableList<Long>;
    val banned: MutableList<Long>;
    val homes: MutableList<Int>;

    constructor(site: Chat){
        this.site = site;

        admins = mutableListOf()
        privelege = mutableListOf()
        banned = mutableListOf()
        homes = mutableListOf()
    }

    fun addHomeRoom(newRoom: Int) : Boolean{
        homes.filter { it == newRoom }
                .forEach { return false }

        homes.add(newRoom);
        return true;
    }

    fun removeHomeRoom(rr: Int) : Boolean{
        for(i in (homes.size - 1)downTo 0){
            if(homes[i] == rr){
                homes.removeAt(i)
                return true;
            }
        }

        return false;
    }

    fun addAdmin(newUser: Long) : ARRequests{
        if(Utils.isBanned(newUser, this)){
            return ARRequests(BANNED)
        }
        if(Utils.isAdmin(newUser, this)){
            return ARRequests(EXISTED);
        }

        admins.add(newUser)
        return ARRequests(ADDED);
    }

    fun removeAdmin(rr: Long) : ARRequests{
        if(Utils.isHardcodedAdmin(rr, site))
            return ARRequests(HARDCODED);

        for(i in (admins.size - 1) downTo 0){
            if(admins[i] == rr){
                admins.removeAt(i)
                return ARRequests(ADDED);
            }
        }
        return ARRequests(EXISTED);
    }

    fun addPriv(newUser: Long){
        if(Utils.isBanned(newUser, this)){
            return
        }
        privelege.filter { it == newUser }
                .forEach { return }

        privelege.add(newUser)
    }

    fun removePriv(rr: Long){
        for(i in (privelege.size - 1) downTo 0){
            if(privelege[i] == rr){
                privelege.removeAt(i)
            }
        }
    }

    fun ban(newUser: Long) : ARRequests{
        if(Utils.isHardcodedAdmin(newUser, site)){
            return ARRequests(HARDCODED)
        }
        if(Utils.isBanned(newUser, this)){
            return ARRequests(EXISTED);
        }

        banned.add(newUser)
        return ARRequests(ADDED);
    }

    fun unban(rr: Long)  : ARRequests{
        if(Utils.isBanned(rr, this)){
            banned.remove(rr);
            return ARRequests(ADDED);
        }

        return ARRequests(EXISTED);
    }

    fun set(homes: List<Int>?, admins: List<Long>?, prived: List<Long>?, banned: List<Long>?){
        if(homes != null) {
            this.homes.addAll(homes)
        }
        if(admins != null) {
            for(x in admins){
                this.admins.add(x.toLong());
            }
        }
        if(prived != null){
            for(x in privelege){
                this.privelege.add(x.toLong());
            }
        }
        if(banned != null){
            for(x in banned){
                this.banned.add(x.toLong());
            }
        }
    }

}

val EXISTED: Int = 0;
val ADDED: Int = 1;
val HARDCODED = 2;
val BANNED = 3;

data class ARRequests(val code: Int);

class ChangeCommandStatus(val center: CommandCenter) : AbstractCommand("declare", listOf(), "Changes a commands status. Only commands available on the site can be edited"){
    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input)){
            return null;
        }

        if(!Utils.isAdmin(user.userID, center.site.config) && !Utils.isPriv(user.userID, center.site.config)){
            return BMessage("I'm afraid I can't let you do that, User", true);
        }
        try {
            val args = input.split(" ");
            val command = args[1];

            var newState: String = args[2];
            val actual: Boolean
            if(newState == "sfw"){
                actual = false;
            }else if(newState == "nsfw"){
                actual = true;
            }else
                actual = newState.toBoolean();

            if (center.isBuiltIn(command)) {
                System.out.println(command);
                return BMessage("You can't change the status of included commands.", true);
            }
            if (CommandCenter.tc.doesCommandExist(command)) {
                CommandCenter.tc.commands.forEach{
                    if(it.value.name == command) {
                        if(it.value.nsfw == actual){
                            return BMessage("The status was already set to " + (if (actual) "NSFW" else "SFW"), true);
                        }
                        it.value.nsfw = actual;

                        return BMessage("Command status changed to " + (if (actual) "NSFW" else "SFW"), true);
                    }
                }
            } else {
                return BMessage("The command doesn't exist.", true);
            }
        }catch(e:ClassCastException){
            return BMessage("Something just went terribly wrong. Sorry 'bout that", true);
        }catch(e: IndexOutOfBoundsException){
            return BMessage("Not enough arguments. I need the command name and new state", true);
        }
        return BMessage("This is in theory unreachable code. If you read this message something bad happened", true);
    }
}