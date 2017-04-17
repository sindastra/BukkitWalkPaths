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
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin implements Listener {

	Map<UUID,Boolean> pathWalkers = new HashMap<UUID,Boolean>();
	Map<String,Material> pathMaterial = new HashMap<String,Material>();
	
	private String helpMsgAvailablePaths = ChatColor.DARK_AQUA + "Available path types: path, grass, dirt, stone, cobblestone, glowstone, stonebrick";
	private String helpMsgSpecifyPaths   = ChatColor.GREEN     + "Use '/walkpath [path_type]' to specify a path type.";
	private String helpMsgToggleOnOff    = ChatColor.GOLD      + "Use '/walkpath' without arguments to toggle path walking on or off.";
	private String helpMsgUnstuck        = ChatColor.YELLOW    + "Use '/walkpath unstuck' to free yourself if you get stuck in the ground.";
	
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
	
	private void sendHelpMsgToPlayer(Player p)
	{
		p.sendMessage(helpMsgSpecifyPaths);
		p.sendMessage(helpMsgAvailablePaths);
		p.sendMessage(helpMsgToggleOnOff);
		p.sendMessage(helpMsgUnstuck);
	}
	
	private void unstuckPlayerFromGround(Player p)
	{	
		if(!p.getLocation().add(0,2.3,0).getBlock().isEmpty())
			if((p.getHealth()+1) < (double)p.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())
				p.setHealth(p.getHealth()+1);
		
		p.teleport( p.getLocation().add(0,1,0) );
	}
	
	private void playerPathMaterial(Player p, Material m)
	{
		pathMaterial.put(p.getUniqueId().toString() + "_MATERIAL", m);
	}
	
	private Material playerPathMaterial(Player p)
	{
		return pathMaterial.get(p.getUniqueId().toString() + "_MATERIAL");
	}
	
	@EventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent event)
	{
		Player player = (Player)event.getPlayer();
		
		if( pathWalkers.get(player.getUniqueId()) == null )
			return;
		
		if( !(boolean)pathWalkers.get(player.getUniqueId()) )
			return;
		
		Location playerLoc = (Location) player.getLocation();
		Location pathLoc = playerLoc.add(0,-1,0);
		
		if(pathLoc.getBlock().isEmpty())
			return;
		
		if(pathLoc.getBlock().isLiquid())
			return;
		
		if(playerPathMaterial(player) == null)
			return;
		
		pathLoc.getBlock().setType(playerPathMaterial(player));
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
					if(args.length == 0)
					{
						if(playerPathMaterial(player) == null)
						{
							playerPathMaterial(player, Material.GRASS_PATH);
							player.sendMessage(ChatColor.YELLOW+"No path type specified, defaulting to '"+ChatColor.GOLD+"path"+ChatColor.YELLOW+"'.");
							sendHelpMsgToPlayer(player);
						}
						
						if(pathWalkers.get(player.getUniqueId()) == null)
							pathWalkers.put(player.getUniqueId(), true);
						else
							pathWalkers.put(player.getUniqueId(), !(boolean)pathWalkers.get(player.getUniqueId()));
						
						player.sendMessage("Path walking "+ ((boolean)pathWalkers.get(player.getUniqueId())?"enabled!":"disabled.") );
					}
					
					if((args.length == 1) && args[0].equalsIgnoreCase("help"))
						sendHelpMsgToPlayer(player);
					
					if((args.length == 1) && args[0].equalsIgnoreCase("unstuck"))
						unstuckPlayerFromGround(player);
					
					if((args.length == 1) && !args[0].equalsIgnoreCase("help") && !args[0].equalsIgnoreCase("unstuck"))
					{
						
						boolean enablePathWalking = true;
						
						switch(args[0])
						{
							case "path":
								playerPathMaterial(player, Material.GRASS_PATH);
								break;
							case "grass":
								playerPathMaterial(player, Material.GRASS);
								break;
							case "dirt":
								playerPathMaterial(player, Material.DIRT);
								break;
							case "stone":
								playerPathMaterial(player, Material.STONE);
								break;
							case "cobblestone":
								playerPathMaterial(player, Material.COBBLESTONE);
								break;
							case "glowstone":
								playerPathMaterial(player, Material.GLOWSTONE);
								break;
							case "stonebrick":
								playerPathMaterial(player, Material.SMOOTH_BRICK);
								break;
							default:
								enablePathWalking = false;
								player.sendMessage(ChatColor.RED+"Path type you specified was not found.");
								player.sendMessage(helpMsgAvailablePaths);
								break;
						}

						if(enablePathWalking)
							player.sendMessage("Path type set to "+ChatColor.GREEN+ args[0] +ChatColor.RESET+ ".");
						
						pathWalkers.put(player.getUniqueId(), enablePathWalking);	
						player.sendMessage("Path walking "+ (enablePathWalking ? "enabled!":ChatColor.RED+"disabled."));
					}
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
