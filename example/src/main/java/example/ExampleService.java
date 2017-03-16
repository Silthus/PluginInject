package example;

import org.bukkit.entity.Player;
import org.jvnet.hk2.annotations.Service;

@Service
public class ExampleService implements ExampleContract
{
	@Override
	public void punishPlayer(Player player)
	{
		player.kickPlayer("Loser");
	}
}
