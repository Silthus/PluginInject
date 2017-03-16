package example;

import org.bukkit.entity.Player;
import org.jvnet.hk2.annotations.Contract;

@Contract
public interface ExampleContract
{
	void punishPlayer(Player player);
}
