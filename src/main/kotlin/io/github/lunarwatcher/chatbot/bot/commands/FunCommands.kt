package io.github.lunarwatcher.chatbot.bot.commands

import io.github.lunarwatcher.chatbot.bot.chat.BMessage
import java.util.*

class RandomNumber() : AbstractCommand("random", listOf("dice"), "Generates a random number"){
    val random: Random = Random(System.currentTimeMillis());

    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input)){
            return null;
        }
        try {
            val split = input.split(" ");
            if (split.size == 1)
                return BMessage(randomNumber(6, 1), true);
            else if (split.size == 2)
                return BMessage(randomNumber(split[1].toInt(), 1), true)
            else if (split.size >= 3)
                return BMessage(randomNumber(split[1].toInt(), split[2].toInt()), true)
        }catch(e: Exception){
            return BMessage("Something went terribly wrong", true);
        }
        return BMessage("You shouldn't see this", true);
    }

    fun randomNumber(limit: Int, count: Int): String{
        val count = if(count > 500) 500 else count;
        val builder: StringBuilder = StringBuilder()
        for(i in 0 until count){
            builder.append((if(i == count - 1) random.nextInt(limit) else random.nextInt(limit).toString() + ", "))
        }
        return builder.toString();
    }
}


val GOOGLE_LINK = "https://www.google.com/search?q=";
class LMGTFY : AbstractCommand("lmgtfy", listOf("searchfor", "google"), "Sends a link to Google in chat"){
    override fun handleCommand(input: String, user: User): BMessage? {
        if(!matchesCommand(input)){
            return null;
        }

        var query = removeName(input).replaceFirst(" ", "");
        if(query.isEmpty())
            return BMessage("You have to supply a query", true);
        query = query.replace(" ", "+")

        return BMessage(GOOGLE_LINK + query, false)
    }
}