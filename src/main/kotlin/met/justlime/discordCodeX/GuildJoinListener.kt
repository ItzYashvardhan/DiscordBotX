package met.justlime.discordCodeX

import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class GuildJoinListener(private val allowedGuilds: List<String>) : ListenerAdapter() {
    override fun onGuildJoin(event: GuildJoinEvent) {
        val guild = event.guild
        if (guild.id !in allowedGuilds) {
            guild.leave().queue()
        }
    }
}