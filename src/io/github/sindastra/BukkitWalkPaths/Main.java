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

import java.util.Arrays;
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

	private static Map<UUID,Boolean> pathWalkers = new HashMap<UUID,Boolean>();
	private static Map<String,Material> pathMaterial = new HashMap<String,Material>();
	private static Map<String,Boolean> placeUnderFeet = new HashMap<String,Boolean>();
	
	private static String helpMsgAvailablePaths = ChatColor.DARK_AQUA + "Available path types: path, grass, dirt, wood, log, stone, cobblestone, glowstone, stonebrick, snowblock, snow";
	private static String helpMsgSpecifyPaths   = ChatColor.GREEN     + "Use '/walkpath [path_type]' to specify a path type.";
	private static String helpMsgToggleOnOff    = ChatColor.GOLD      + "Use '/walkpath' without arguments to toggle path walking on or off.";
	private static String helpMsgUnstuck        = ChatColor.YELLOW    + "Use '/walkpath unstuck' to free yourself if you get stuck in the ground.";
	
	private static boolean debug = false;
	
	private static Material[] protectedMaterials = {
			
			// Players' personal blocks:
			Material.BED, Material.BED_BLOCK, Material.CHEST, Material.TRAPPED_CHEST, Material.FURNACE,
			Material.BURNING_FURNACE, Material.ENCHANTMENT_TABLE, Material.ENDER_CHEST, Material.ANVIL,
			Material.CAULDRON, Material.BREWING_STAND, Material.ARMOR_STAND, Material.SIGN, Material.SIGN_POST,
			Material.WALL_SIGN, Material.WORKBENCH, Material.DISPENSER, Material.DROPPER, Material.HOPPER,
			
			// Redstone:
			Material.DAYLIGHT_DETECTOR, Material.DAYLIGHT_DETECTOR_INVERTED, Material.REDSTONE_WIRE,
			Material.REDSTONE_COMPARATOR, Material.REDSTONE_COMPARATOR_OFF, Material.REDSTONE_COMPARATOR_ON,
			Material.REDSTONE_TORCH_OFF, Material.REDSTONE_TORCH_ON, Material.PISTON_BASE, Material.PISTON_STICKY_BASE,
			Material.PISTON_MOVING_PIECE, Material.PISTON_EXTENSION,
			
			// Rails:
			Material.RAILS, Material.ACTIVATOR_RAIL, Material.DETECTOR_RAIL, Material.POWERED_RAIL,
			
			// Misc:
			Material.LADDER, Material.STRING, Material.BEACON, Material.LEVER, Material.STONE_BUTTON, Material.WOOD_BUTTON,
			
			// Doors:
			Material.TRAP_DOOR, Material.IRON_TRAPDOOR, Material.ACACIA_DOOR, Material.BIRCH_DOOR,
			Material.DARK_OAK_DOOR, Material.IRON_DOOR, Material.JUNGLE_DOOR, Material.SPRUCE_DOOR,
			Material.WOOD_DOOR, Material.WOODEN_DOOR,
			
			// Fences:
			Material.FENCE, Material.FENCE_GATE, Material.ACACIA_FENCE, Material.ACACIA_FENCE_GATE,
			Material.BIRCH_FENCE, Material.BIRCH_FENCE_GATE, Material.DARK_OAK_FENCE, Material.DARK_OAK_FENCE_GATE,
			Material.IRON_FENCE, Material.JUNGLE_FENCE, Material.JUNGLE_FENCE_GATE, Material.NETHER_FENCE,
			Material.SPRUCE_FENCE, Material.SPRUCE_FENCE_GATE,
			
			// Plants/Food/Farm:
			Material.MELON_BLOCK, Material.MELON, Material.MELON_SEEDS, Material.MELON_STEM,
			Material.PUMPKIN_SEEDS, Material.PUMPKIN_STEM, Material.PUMPKIN, Material.CARROT, Material.POTATO,
			Material.NETHER_WARTS, Material.NETHER_WART_BLOCK, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM,
			Material.WATER_LILY, Material.SUGAR_CANE_BLOCK,
			
			// End:
			Material.DRAGON_EGG, Material.ENDER_PORTAL, Material.ENDER_PORTAL_FRAME,
			
			// Admin:
			Material.COMMAND, Material.MOB_SPAWNER, Material.BEDROCK, Material.STRUCTURE_VOID, Material.BARRIER,
			Material.SPONGE
	};
	
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
		playerPathMaterial(p, m, true);
	}
	
	private void playerPathMaterial(Player p, Material m, boolean placeUnder)
	{
		pathMaterial.put(p.getUniqueId().toString() + "_MATERIAL", m);
		placeUnderFeet.put(p.getUniqueId().toString()+"_PlaceUnderFeet", placeUnder);
	}
	
	private Material playerPathMaterial(Player p)
	{
		return pathMaterial.get(p.getUniqueId().toString() + "_MATERIAL");
	}
	
	private boolean shouldPlaceMaterialUnderFeet(Player p)
	{
		return placeUnderFeet.get(p.getUniqueId().toString()+"_PlaceUnderFeet");
	}
	
	private byte placeMaterialDirection(Player p)
	{
		return (byte)(shouldPlaceMaterialUnderFeet(p) ? -1 : 1);
	}
	
	@EventHandler
	public void onPlayerMoveEvent(PlayerMoveEvent event)
	{
		Player player = (Player)event.getPlayer();
		
		if( pathWalkers.get(player.getUniqueId()) == null )
			return;
		
		if( !(boolean)pathWalkers.get(player.getUniqueId()) )
			return;
		
		Location underFeetLoc = (Location)player.getLocation().add(0,-0.1,0);
		Location pathLoc = (Location)player.getLocation().add(0,0.2 * placeMaterialDirection(player),0);
		
		if(underFeetLoc.getBlock().isEmpty())
			return;
		
		if(underFeetLoc.getBlock().isLiquid())
			return;
		
		if(shouldPlaceMaterialUnderFeet(player) && !pathLoc.getBlock().getType().isSolid())
			return;
		
		if(pathLoc.getBlock().getType().equals(Material.BEDROCK))
			return;
		
		if(playerPathMaterial(player).equals(Material.SNOW) && underFeetLoc.getBlock().getType().equals(Material.SNOW))
			return;
		
		if(playerPathMaterial(player) == null)
			return;
		
		if( shouldPlaceMaterialUnderFeet(player) && Arrays.asList(protectedMaterials).contains(underFeetLoc.getBlock().getType()) )
		{
			if(debug)
				player.sendMessage("[DEBUG] The material "+underFeetLoc.getBlock().getType().toString()
						+" you are walking on is protected from path walking.");
			return;
		}
		
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
						
						switch(args[0].toLowerCase())
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
							case "wood":
								playerPathMaterial(player, Material.WOOD);
								break;
							case "log":
								playerPathMaterial(player, Material.LOG);
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
							case "snowblock":
								playerPathMaterial(player, Material.SNOW_BLOCK);
								break;
							case "snow":
								playerPathMaterial(player, Material.SNOW, false);
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
