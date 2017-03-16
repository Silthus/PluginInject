package example;

import com.natemortensen.plugininject.InjectedPlugin;
import com.natemortensen.plugininject.PluginInject;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;

public class ExamplePlugin extends InjectedPlugin
{
	// Don't ever inject PluginInject, it's useless.
	// This is simply to demonstrate how one would inject another plugin
	@Inject
	private PluginInject inject;
	@Inject
	@Named("PluginInject")
	private JavaPlugin plugin;

	// InjectedPlugin doesn't allow onEnable to be overridden; use @PostConstruct instead
	@PostConstruct
	public void postConstruct()
	{
		getLogger().info("Are they the same plugin? " + (inject == plugin));
	}
	// InjectedPlugin implements Listener and is registered automatically
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		event.getPlayer().sendMessage("Welcome!");
	}
}
