package me.clip.voteparty.config.sections

import ch.jalu.configme.SettingsHolder
import ch.jalu.configme.properties.Property
import ch.jalu.configme.properties.PropertyInitializer.newBeanProperty
import ch.jalu.configme.properties.PropertyInitializer.newListProperty
import ch.jalu.configme.properties.PropertyInitializer.newProperty
import me.clip.voteparty.plugin.XMaterial

object CrateSettings : SettingsHolder
{
	@JvmField
	val ENABLED = newProperty("crate.enabled", true)
	
	@JvmField
	val LORE = newListProperty("crate.lore", "&aPlace me &e:)")
	
	@JvmField
	val MATERIAL: Property<XMaterial> = newBeanProperty(XMaterial::class.java,"crate.material", XMaterial.CHEST)
	
	@JvmField
	val NAME = newProperty("crate.name", "&b&lVote&f&lParty &7Crate");
}