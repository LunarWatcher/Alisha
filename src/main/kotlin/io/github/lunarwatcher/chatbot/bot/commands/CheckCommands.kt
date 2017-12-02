package io.github.lunarwatcher.chatbot.bot.commands

import io.github.lunarwatcher.chatbot.bot.chat.BMessage
import io.github.lunarwatcher.chatbot.bot.command.CommandCenter.TRIGGER
import io.github.lunarwatcher.chatbot.bot.sites.Chat
import io.github.lunarwatcher.chatbot.utils.Utils
import org.tritonus.share.TCircularBuffer

val USER_BANNED = 0;
val USER_ADMIN = 1;
val USER_PRIV = 2;
val USER_NORMAL = 3;

class CheckCommand(var site: Chat) : AbstractCommand("check", listOf(), "Checks as user's role",
        "Supported roles: `admin`, `normal`, `privileged`(/`priv`) and `banned`. Unknown roles defaults to a normal check." +
                " Example usage: `" + TRIGGER + "check admin 6296561`"){

    fun checkStatus(uid: Long, statusToCheck: Int) : Boolean = when(statusToCheck){
        USER_BANNED -> Utils.isBanned(uid, site.config)
        USER_ADMIN -> Utils.isAdmin(uid, site.config)
        USER_PRIV -> Utils.isPriv(uid, site.config);
        USER_NORMAL -> !Utils.isBanned(uid, site.config) && !Utils.isAdmin(uid, site.config) && !Utils.isPriv(uid, site.config);
        else -> false;
    }


    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input)){
            return null;
        }

        val type = input.replace(name + " ", "");
        if(type == input)
            return BMessage("Add role to check for and which user you want to check", true);

        if(type.isEmpty()){
            return BMessage("Define a role to check and a user you want to check", true);
        }else{
            val role: String;
            val user: Long;
            try {
                role = type.split(" ")[0]
                user = type.split(" ")[1].toLong();
            }catch(e: ClassCastException){
                return BMessage("Not a valid user ID!", true);
            }catch(e: IndexOutOfBoundsException){
                return BMessage("You have to supply two arguments", true);
            }

            return when(role){
                "admin" -> getAdminMessage(checkStatus(user, USER_ADMIN));
                "banned" -> getBannedMessage(checkStatus(user, USER_BANNED));
                "privileged" -> getPrivMessage(checkStatus(user, USER_PRIV));
                "priv" -> getPrivMessage(checkStatus(user, USER_PRIV));
                else -> getNormalMessage(checkStatus(user, USER_NORMAL));
            }
        }
    }

    fun getAdminMessage(bool: Boolean)  : BMessage = BMessage(if(bool) "User is a bot admin" else "User is not a bot admin", true);

    fun getBannedMessage(bool: Boolean) : BMessage = BMessage(if(bool) "User is banned" else "User is not banned", true);

    fun getPrivMessage(bool: Boolean)   : BMessage = BMessage(if(bool) "User has privileged access" else "User does not have privileged", true);

    fun getNormalMessage(bool: Boolean) : BMessage = BMessage(if(bool) "User is a normal user" else "User is not a normal user", true);

}
