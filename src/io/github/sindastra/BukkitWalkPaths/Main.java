/**
 * BukkitWalkPaths
 * Copyright (C) 2017 Sindastra <https://github.com/sindastra>
 * All rights reserved.
 *
 * This and the above copyright notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * 
 * This software is not affiliated with Bukkit.
 * 
 * @author Sindastra
 * @copyright Copyright (C) 2017 Sindastra. All rights reserved.
 */

package io.github.sindastra.BukkitWalkPaths;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

	Map<String,Boolean> pathWalkers = new HashMap<String,Boolean>();
	
	@Override
	public void onEnable()
	{
		getServer().getPluginManager().registerEvents(this, this);
		getServer().getLogger().info("Enabled!");
	}
	
	@Override
	public void onDisable()
	{
		getServer().getLogger().info("Disabled!");
	}
	
	@EventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent event)
	{
		Player player = (Player)event.getPlayer();
		
		if( pathWalkers.get(player.getName()) == null )
			return;
		
		if( !(boolean)pathWalkers.get(player.getName()) )
			return;
		
		Location playerLoc = (Location) player.getLocation();
		Location pathLoc = playerLoc.add(0,-1,0);
		
		if(pathLoc.getBlock().isEmpty())
			return;
		
		if(pathLoc.getBlock().isLiquid())
			return;
		
		pathLoc.getBlock().setType(Material.GRASS_PATH);
	}
	
	@Override
	public boolean onCommand( CommandSender sender , Command cmd , String label , String[] args )
	{
		if( cmd.getName().equalsIgnoreCase("walkpath") )
		{
			if( !(sender instanceof Player) )
			{
				sender.sendMessage("This command must be run by a player.");
			}
			else
			{
				Player player = (Player)sender;
				
				if(player.isOp() || player.hasPermission("sindastra.walkpaths.use"))
				{
					if(pathWalkers.get(player.getName()) == null)
						pathWalkers.put(player.getName(), true);
					else
						pathWalkers.put(player.getName(), !(boolean)pathWalkers.get(player.getName()));
					
					player.sendMessage("Path walking "+ ((boolean)pathWalkers.get(player.getName())?"enabled!":"disabled.") );
				}
				else
				{
					player.sendMessage(ChatColor.RED + "Permission denied.");
				}
			}
			return true;
		}		
		return false;
	}
}
