package io.github.lunarwatcher.chatbot

import java.util.*

/**
 * Site specific config
 */
data class Site(var name: String, val url: String, val config: SiteConfig){

    fun `is`(name: String) : Boolean {
        return this.name == name;
    }
}

/**
 * The account details for the site
 */
data class SiteConfig(var username: String, var password: String, var email: String, var userID: Long, var messageOnLeave: Boolean = true);
