package de.raidcraft.skillsandeffects.pve.effects.tools;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.config.CustomConfig;
import de.raidcraft.skills.trigger.PlayerInteractTrigger;
import de.raidcraft.util.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Philip
 */
@EffectInformation(
        name = "SpeedBlockBreak",
        description = "Blitzschnelles abbauen von Blöcken",
        types = {EffectType.BUFF},
        elements = {EffectElement.LIGHT}
)
public class RecursiveBlockBreak extends ExpirableEffect<Skill> implements Triggered {

    private List<Material> knownBlocks = new ArrayList<>();
    private String activateMsg;
    private String deactivateMsg;
    private int toolId;
    private Player player = null;

    public RecursiveBlockBreak(Skill source, CharacterTemplate target, EffectData data) {
        super(source, target, data);
        if(target instanceof Hero) {
            this.player = ((Hero) target).getPlayer();
        }
    }

    @Override
    public void load(ConfigurationSection data) {

        this.activateMsg = data.getString("activate-message", "BlastFeller aktiviert!");
        this.deactivateMsg = data.getString("deactivate-message", "BlastFeller abgelaufen!");
        this.toolId = data.getInt("tool-id");

        CustomConfig blockConfig = CustomConfig.getConfig(data.getString("custom-block-config", "recursiveblockbreak-block-config.yml"));
        ConfigurationSection blocks = blockConfig.getConfigurationSection("blocks");
        if (blocks == null) return;
        for(String key : blocks.getKeys(false)) {
            Material material = ItemUtils.getItem(key);
            if(material == null) {
                RaidCraft.LOGGER.warning("Unknown material '" + key + "' in " + getClass().getSimpleName());
                continue;
            }
            knownBlocks.add(material);
        }
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        if (!(target.getEntity() instanceof Player)) {
            return;
        }

        ((Player) target.getEntity()).sendMessage(ChatColor.GREEN + activateMsg);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        if (!(target.getEntity() instanceof Player)) {
            return;
        }

        ((Player) target.getEntity()).sendMessage(ChatColor.RED + deactivateMsg);
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {
    }

    @TriggerHandler
    public void onInteract(PlayerInteractTrigger trigger) throws CombatException {

        PlayerInteractEvent event = trigger.getEvent();

        if (event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        // check if correct tool in use
        if (event.getItem() == null || event.getItem().getTypeId() != toolId) {
            getSource().getHolder().debug("Incorrect tool: " + event.getPlayer().getItemInHand().getType().name() + " (required: " + toolId +
                    ")");
            return;
        }

        if(player != null) {
            breakRecursive(event.getClickedBlock().getLocation());
        }
    }

    private void breakRecursive(Location blockLocation) {

        Block block = blockLocation.getWorld().getBlockAt(blockLocation.getBlockX(), blockLocation.getBlockY(), blockLocation.getBlockZ());
        // check if clicked block is in list
        if (block == null || !knownBlocks.contains(block.getType())) {
            return;
        }

        BlockBreakEvent fakeBreakEvent = new BlockBreakEvent(block, player);
        RaidCraft.callEvent(fakeBreakEvent);
        if (!fakeBreakEvent.isCancelled()) {
           block.breakNaturally(player.getItemInHand());
            block.setType(Material.AIR);
        }

        breakRecursive(blockLocation.add(0, 1, 0));
        breakRecursive(blockLocation.add(1, 0, 0));
        breakRecursive(blockLocation.add(0, 0, 1));
        breakRecursive(blockLocation.add(-1, 0, 0));
        breakRecursive(blockLocation.add(0, 0, -1));
        breakRecursive(blockLocation.add(0, -1, 0));
    }
}
