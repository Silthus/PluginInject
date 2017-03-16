package com.natemortensen.plugininject;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.inventory.ItemFactory;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scoreboard.ScoreboardManager;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

public class ServerBinder extends AbstractBinder
{
	protected void configure()
	{
		bind(Bukkit.getServer()).to(Server.class);
		bind(Bukkit.getPluginManager()).to(PluginManager.class);
		bind(Bukkit.getScoreboardManager()).to(ScoreboardManager.class);
		bind(Bukkit.getItemFactory()).to(ItemFactory.class);
		bind(Bukkit.getMessenger()).to(Messenger.class);
		bind(Bukkit.getScheduler()).to(BukkitScheduler.class);
		for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
		{
			bindPlugin((JavaPlugin) plugin);
		}
	}

	private void bindPlugin(JavaPlugin plugin)
	{
		doBind(plugin.getClass(), plugin);
	}

	private <T extends JavaPlugin> void doBind(Class<T> tClass, JavaPlugin plugin)
	{
		bind(tClass.cast(plugin)).to(tClass).to(JavaPlugin.class).to(Plugin.class).named(plugin.getName());
	}
}
