package me.clip.voteparty.exte

import co.aikar.commands.ACFUtil
import co.aikar.commands.CommandIssuer
import co.aikar.locales.MessageKeyProvider
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.voteparty.base.Addon
import me.clip.voteparty.conf.objects.Command
import me.clip.voteparty.conf.sections.PluginSettings
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitRunnable
import java.util.concurrent.TimeUnit

private val serializer = LegacyComponentSerializer.legacyAmpersand()

internal fun color(message: String): String
{
	return ChatColor.translateAlternateColorCodes('&', message)
}

internal fun deserialize(message: String): Component
{
	return serializer.deserialize(color(message))
}

internal fun formMessage(player: OfflinePlayer, message: String): String
{
	return color(PlaceholderAPI.setPlaceholders(player, message))
}

internal fun Addon.sendMessage(receiver: CommandIssuer, message: MessageKeyProvider, placeholderTarget: OfflinePlayer? = null, vararg replacements: Any = emptyArray())
{
	var msg = receiver.manager.locales.getMessage(receiver, message)
	
	if (replacements.isNotEmpty() && replacements.size % 2 == 0)
	{
		msg = ACFUtil.replaceStrings(msg, *replacements.map(Any::toString).toTypedArray())
	}
	
	val result = formMessage(Bukkit.getOfflinePlayer(placeholderTarget?.uniqueId ?: receiver.uniqueId), (party.conf().getProperty(PluginSettings.PREFIX) ?: PREFIX) + msg)
	
	party.audiences().sender(receiver.getIssuer()).sendMessage(Identity.nil(), serializer.deserialize(result))
}

internal fun msgAsString(issuer: CommandIssuer, key: MessageKeyProvider): String
{
	return issuer.manager.locales.getMessage(issuer, key)
}

val folia: Boolean by lazy {
	run {
		try {
			Class.forName("io.papermc.paper.threadedregions.RegionizedServer")
			true
		} catch (ignored: ClassNotFoundException) {
			false
		}
	}
}

internal interface CancellableTask {
	fun cancel()
}

internal fun Plugin.runTaskTimer(period: Long, task: CancellableTask.() -> Unit)
{
	if (folia) {
		server.globalRegionScheduler.runAtFixedRate(this, { task.invoke(object : CancellableTask {
			override fun cancel() {
				it.cancel()
			}
		}) }, period, period)
	} else {
		object : BukkitRunnable() {
			override fun run() {
				task.invoke(object : CancellableTask {
					override fun cancel() {
						cancel()
					}
				})
			}
		}.runTaskTimer(this, period, period)
	}
}

internal fun Plugin.runTaskTimerAsync(period: Long, task: () -> Unit)
{
	if (folia) {
		server.asyncScheduler.runAtFixedRate(
			this,
			{ task.invoke() },
			period * 50,
			period * 50,
			TimeUnit.MILLISECONDS
		)
	} else {
		server.scheduler.runTaskTimerAsynchronously(
			this,
			task,
			period,
			period
		)
	}
}

internal fun Plugin.runTaskLater(delay: Long, task: () -> Unit)
{
	if (folia) {
		server.globalRegionScheduler.runDelayed(this, { task.invoke() }, delay)
	} else {
		server.scheduler.runTaskLater(this, task, delay)
	}
}

internal fun Plugin.runTask(task: () -> Unit)
{
	if (folia) {
		server.globalRegionScheduler.run(this) { task.invoke() }
	} else {
		server.scheduler.runTask(this, task)
	}
}

internal fun Collection<Command>.takeRandomly(amount: Int): Collection<Command>
{
	return filter(Command::shouldExecute).shuffled().take(amount)
}


internal fun item(type: Material, amount: Int, function: ItemMeta.() -> Unit = {}): ItemStack
{
	return ItemStack(type, amount).meta(function)
}

internal fun ItemStack.meta(function: ItemMeta.() -> Unit): ItemStack
{
	itemMeta = itemMeta?.apply(function)
	
	return this
}

internal var ItemMeta.name: String
	get() = displayName
	set(value)
	{
		setDisplayName(color(value))
	}
