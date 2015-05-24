package de.raidcraft.skillsandeffects.pve.skills;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.language.Translator;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.api.combat.action.SkillAction;
import de.raidcraft.skills.api.effect.common.QueuedInteract;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.PlayerInteractTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Recursive Block Break",
        description = "Breaks blocks recursivly.",
        queuedAttack = true
)
public class RecursiveBlockBreak extends AbstractSkill implements Triggered {

    @Getter
    private final Set<Material> allowedTools = new HashSet<>();
    @Getter
    private final Set<Material> allowedBlocks = new HashSet<>();
    @Getter
    private int maxAmount;

    public RecursiveBlockBreak(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        this.maxAmount = (int) ConfigUtil.getTotalValue(this, data.getConfigurationSection("max-amount"));
        for (String entry : data.getStringList("allowed-blocks")) {
            Material material = Material.matchMaterial(entry);
            if (material != null) {
                allowedBlocks.add(material);
            } else {
                RaidCraft.LOGGER.warning("Wrong material in skill config of " + getName() + ": " + entry);
            }
        }
        for (String entry : data.getStringList("tools")) {
            Material material = Material.matchMaterial(entry);
            if (material != null) {
                allowedTools.add(material);
            } else {
                RaidCraft.LOGGER.warning("Wrong material in skill config of " + getName() + ": " + entry);
            }
        }
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractTrigger trigger) throws CombatException {

        final PlayerInteractEvent event = trigger.getEvent();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !isValidTool(event)) {
            return;
        }
        checkUsage(new SkillAction(this));

        addEffect(QueuedInteract.class).addCallback(callback -> {

            if (callback.getEvent().getAction() != Action.LEFT_CLICK_BLOCK
                    || !isValidTool(event)
                    || !isValidBlock(callback.getEvent())) {
                return;
            }

            substractUsageCost(new SkillAction(RecursiveBlockBreak.this));
            int amount = breakRecursive(event.getPlayer(), event.getItem(), event.getClickedBlock(), 0, getMaxAmount());
            info(Translator.tr(SkillsPlugin.class, event.getPlayer(),
                    "skills.recursive-block-break.break-amount", "You have destroyed {0} blocks with {1}.", amount, getFriendlyName()));
        }, Action.LEFT_CLICK_BLOCK);
    }

    public boolean isValidTool(PlayerInteractEvent event) {

        return event.getItem() != null
                && allowedTools.contains(event.getItem().getType());
    }

    public boolean isValidBlock(PlayerInteractEvent event) {

        return event.getClickedBlock() != null
                && allowedBlocks.contains(event.getClickedBlock().getType());
    }

    public int breakRecursive(Player player, ItemStack tool, Block block, int currentAmount, int maxAmount) {

        if (currentAmount < maxAmount && getAllowedBlocks().contains(block.getType())) {
            BlockBreakEvent event = new BlockBreakEvent(block, player);
            RaidCraft.callEvent(event);
            if (event.isCancelled()) {
                return currentAmount;
            }
            block.breakNaturally(tool);
            currentAmount = breakRecursive(player, tool, block.getRelative(BlockFace.DOWN), currentAmount + 1, maxAmount);
            currentAmount = breakRecursive(player, tool, block.getRelative(BlockFace.NORTH), currentAmount + 1, maxAmount);
            currentAmount = breakRecursive(player, tool, block.getRelative(BlockFace.NORTH_EAST), currentAmount + 1, maxAmount);
            currentAmount = breakRecursive(player, tool, block.getRelative(BlockFace.EAST), currentAmount + 1, maxAmount);
            currentAmount = breakRecursive(player, tool, block.getRelative(BlockFace.SOUTH_EAST), currentAmount + 1, maxAmount);
            currentAmount = breakRecursive(player, tool, block.getRelative(BlockFace.SOUTH), currentAmount + 1, maxAmount);
            currentAmount = breakRecursive(player, tool, block.getRelative(BlockFace.SOUTH_WEST), currentAmount + 1, maxAmount);
            currentAmount = breakRecursive(player, tool, block.getRelative(BlockFace.WEST), currentAmount + 1, maxAmount);
            currentAmount = breakRecursive(player, tool, block.getRelative(BlockFace.NORTH_WEST), currentAmount + 1, maxAmount);
            currentAmount = breakRecursive(player, tool, block.getRelative(BlockFace.UP), currentAmount + 1, maxAmount);
        }
        return currentAmount;
    }
}
