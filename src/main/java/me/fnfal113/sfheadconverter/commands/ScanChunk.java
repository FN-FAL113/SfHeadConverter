package me.fnfal113.sfheadconverter.commands;

import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.protection.Interaction;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerHead;
import io.github.thebusybiscuit.slimefun4.libraries.dough.skins.PlayerSkin;
import io.github.thebusybiscuit.slimefun4.utils.HeadTexture;

import me.fnfal113.sfheadconverter.Utils.Utils;
import me.mrCookieSlime.Slimefun.api.BlockStorage;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Map.entry;

public class ScanChunk implements TabExecutor {

    private final Map<String, Integer> convertedBlocksCountMap = new HashMap<>();
    
    private final String[] headIdArr = {"ENERGY_CONNECTOR",
        "CARGO_NODE",
        "CARGO_NODE_INPUT",
        "CARGO_NODE_OUPUT",
        "CARGO_NODE_OUPUT_ADVANCED"
    };

    private final Map<String, PlayerSkin> materialToHeadSkinMap = Map.ofEntries(
        entry("ENERGY_CONNECTOR", HeadTexture.ENERGY_CONNECTOR.getAsSkin()),
        entry("CARGO_NODE", HeadTexture.CARGO_CONNECTOR_NODE.getAsSkin()),
        entry("CARGO_NODE_INPUT", HeadTexture.CARGO_INPUT_NODE.getAsSkin()),
        entry("CARGO_NODE_OUPUT", HeadTexture.CARGO_OUTPUT_NODE.getAsSkin()),
        entry("CARGO_NODE_OUPUT_ADVANCED", HeadTexture.CARGO_OUTPUT_NODE.getAsSkin())
    );

    private List<String> materialNameArgs = new ArrayList<>();

    private List<String> headsToConvertArgs = new ArrayList<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) { 
            Player player = (Player) sender;

            if(args.length == 2) {
                if(player.hasPermission("sfheadconverter")) {
                    Chunk chunk = player.getLocation().getChunk();

                    getAmount(chunk, player, args);
                } else {
                    Utils.sendMessage(player, "You don't have permission to use this command (perm mode: sfheadconverter)");
                }
            }
        }

        return true;
    }

    public void getAmount(Chunk chunk, Player player, String[] args) {
        if (!Slimefun.getProtectionManager().hasPermission(Bukkit.getOfflinePlayer(player.getUniqueId()), player.getLocation(), Interaction.PLACE_BLOCK)) {
            Utils.sendMessage(player, "You don't have the permission to convert blocks on this chunk (Grief Protected), ask for permission or override using the protection plugin command");

            return;
        }

        // if invalid arguments were encountered on scan and convert
        if(!scanChunkAndConvert(chunk, player, args)) return;

        player.sendMessage(ChatColor.GOLD + "# of Slimefun heads converted on this chunk:", "");

        if (convertedBlocksCountMap.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "No Slimefun heads to be converted on this chunk", "");

            return;
        }

        convertedBlocksCountMap.entrySet()
            .stream()
            .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
            .forEachOrdered(e -> player.sendMessage(e.getKey() + ": " + ChatColor.GOLD + e.getValue(), ""));

        convertedBlocksCountMap.clear();
    }

    public boolean scanChunkAndConvert(Chunk chunk, Player player, String[] args) {
        if (!headsToConvertArgs.contains(args[0].toUpperCase()) || !materialNameArgs.contains(args[1].toUpperCase())) {
            Utils.sendMessage(player, "Invalid parameters, please select from suggested parameters!");

            return false;
        }

        for (int y = chunk.getWorld().getMinHeight(); y <= chunk.getWorld().getMaxHeight() - 1; y++) {
            for(int x = 0; x <= 15; x++) {
                for(int z = 0; z <= 15; z++) {
                    Block block = chunk.getBlock(x, y, z);

                    if(block.getType() == Material.AIR) {
                        continue;
                    }

                    if(BlockStorage.check(block) != null) {
                        SlimefunItem sfItem = BlockStorage.check(block);
                        String sfId = sfItem.getId();

                        // match head argument with block sf id
                        if(!args[0].equals(sfId)) continue;

                        String sfBlockName = Objects.requireNonNull(sfItem).getItemName();

                        if(Material.getMaterial(args[1]) != null) {
                            block.setType(Material.getMaterial(args[1]));

                            convertedBlocksCountMap.put(sfBlockName, convertedBlocksCountMap.getOrDefault(sfBlockName, 0) + 1);
                        } else if(args[1].toUpperCase().equals("RESET")) {
                            block.setType(Material.PLAYER_HEAD);

                            PlayerHead.setSkin(block, materialToHeadSkinMap.get(sfId), true);

                            convertedBlocksCountMap.put(sfBlockName, convertedBlocksCountMap.getOrDefault(sfBlockName, 0) + 1);
                        }

                    }
                }
            }           
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) { // show sf head ids that can be converted as first arg
            for(String headId : headIdArr) {
                headsToConvertArgs.add(headId);
            }

            if(args[0].length() > 0) {
                headsToConvertArgs = headsToConvertArgs.stream().filter(m -> m.toLowerCase().contains(args[0].toLowerCase())).collect(Collectors.toList());
            }

            return headsToConvertArgs;
        } else if(args.length == 2) { // show conversion material names as second arg
            materialNameArgs.add("RESET");
            
            Set<Material> materials = new HashSet<>(Tag.WOOL.getValues());

            materials.addAll(Stream.of(Material.values()).filter(m -> m.name().contains("_STAINED_GLASS")).collect(Collectors.toSet()));
            
            for (Material material : materials) {
                materialNameArgs.add(material.name());
            }

            // filter out tab suggestions based on input
            if (args[1].length() > 0) {
                materialNameArgs = materialNameArgs.stream().filter(m -> m.toLowerCase().contains(args[1].toLowerCase())).collect(Collectors.toList());
            }

            return materialNameArgs;
        }

        return null;
    }
}