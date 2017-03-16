package example;

import com.natemortensen.plugininject.Module;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.jvnet.hk2.annotations.Service;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

@Service
public class ExampleModule implements Module, Listener
{
	private final BukkitScheduler scheduler;
	private final JavaPlugin plugin;
	private final ExampleContract punishment;

	@Inject
	public ExampleModule(BukkitScheduler scheduler, JavaPlugin plugin, ExampleContract punishment)
	{
		this.scheduler = scheduler;
		this.plugin = plugin;
		this.punishment = punishment;
	}

	@PostConstruct
	public void postConstruct()
	{
		plugin.getLogger().info("Module initialized!");
		plugin.getLogger().info("Did we get the right plugin? " + (plugin.getName().equals("Example")));
	}

	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event)
	{
		scheduler.runTask(plugin, () -> {
			if (event.getPlayer().isValid())
			{
				punishment.punishPlayer(event.getPlayer());
			}
		});
	}
}
