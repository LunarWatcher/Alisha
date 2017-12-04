package io.github.lunarwatcher.chatbot.bot.commands

import io.github.lunarwatcher.chatbot.Constants.Ranks.ranks
import io.github.lunarwatcher.chatbot.bot.chat.BMessage
import io.github.lunarwatcher.chatbot.bot.command.CommandCenter
import io.github.lunarwatcher.chatbot.bot.sites.Chat
import io.github.lunarwatcher.chatbot.bot.sites.se.SEChat
import io.github.lunarwatcher.chatbot.utils.Utils
import sun.java2d.pipe.AAShapePipe

@Suppress("NAME_SHADOWING")
//TODO make this better kotlin
class BotConfig{
    val site: Chat;
    val ranks: MutableMap<Long, RankInfo>;
    val homes: MutableList<Int>;

    constructor(site: Chat){
        this.site = site;

        ranks = mutableMapOf();
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

    fun set(homes: List<Int>?, ranked: Map<Long, RankInfo>?){
        if(homes != null) {
            this.homes.addAll(homes)
        }
        ranked?.forEach{
            this.ranks.put(it.key, it.value)
        }
    }

    fun addRank(user: Long, rank: Int, username: String?){
        var username = username

        if(username == null){
            val rank = ranks.get(user);
            if(rank?.username != null){
                username = rank.username;
            }
        }
        ranks.put(user, RankInfo(user, rank, username));
    }

    fun getRank(user: Long) : RankInfo? = ranks[user];
}

class RankInfo(val uid: Long, val rank: Int, var /*usernames can change*/username: String?/*Nullable because it isn't always this can be passed*/)

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

        if(Utils.getRank(user.userID, center.site.config) < 7){
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