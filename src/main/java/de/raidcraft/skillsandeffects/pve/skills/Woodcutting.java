package de.raidcraft.skillsandeffects.pve.skills;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.SkillAction;
import de.raidcraft.skills.api.combat.callback.Callback;
import de.raidcraft.skills.api.effect.common.QueuedInteract;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.PlayerInteractTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skills.util.ItemUtil;
import de.raidcraft.util.BlockUtil;
import de.raidcraft.util.ItemUtils;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Dragonfire, Silthus
 */
@SkillInformation(
        name = "Woodcutting",
        description = "Fällt direkt einen Baum.",
        types = {EffectType.HELPFUL})
public class Woodcutting extends AbstractLevelableSkill implements Triggered {

    @Getter
    private final Set<Material> allowedTools = new HashSet<>();
    @Getter
    private final Set<Material> matsTree = new HashSet<>();
    @Getter
    private final Set<Material> matsLeaves = new HashSet<>();
    @Getter
    private int maxAmount;
    @Getter
    private int leaveRadius;

    public Woodcutting(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        matsTree.addAll(ItemUtils.LOGS);

        matsLeaves.addAll(ItemUtils.LEAVES);

        for (String entry : data.getStringList("tools")) {
            Material material = Material.matchMaterial(entry);
            if (material != null) {
                allowedTools.add(material);
            } else {
                RaidCraft.LOGGER.warning("Wrong material in skill config of " + getName() + ": " + entry);
            }
        }
        this.maxAmount = (int) ConfigUtil.getTotalValue(this, data.getConfigurationSection("max-amount"));
        this.leaveRadius = (int) ConfigUtil.getTotalValue(this, data.getConfigurationSection("leave-radius"));
    }

    // TODO: holz in der krone abbauen
    @TriggerHandler(ignoreCancelled = true)
    public void onPlayerInteract(final PlayerInteractTrigger trigger) throws CombatException {

        if (trigger.getEvent().getAction() != Action.RIGHT_CLICK_BLOCK
                || !isValidTool(trigger.getEvent())) {
            return;
        }

        addEffect(QueuedInteract.class).addCallback(new Callback<PlayerInteractTrigger>() {
            @Override
            public void run(PlayerInteractTrigger trigger) throws CombatException {

                if (trigger.getEvent().getAction() != Action.LEFT_CLICK_BLOCK
                        || !isValidTool(trigger.getEvent())
                        || !isTreeMaterial(trigger.getEvent().getClickedBlock().getType())) {
                    return;
                }

                checkUsage(new SkillAction(Woodcutting.this));

                Block targetBlock = trigger.getEvent().getClickedBlock();
                // first walk down
                int amount = walkInDirection(trigger, BlockFace.DOWN, targetBlock, 0);
                // then walk up
                amount = walkInDirection(trigger, BlockFace.UP, targetBlock.getRelative(BlockFace.UP), amount);
                getHolder().sendMessage(
                        ChatColor.YELLOW + "Du hast direkt " + ChatColor.RED + amount + ChatColor.YELLOW
                                + " abgebaut."
                );
                substractUsageCost(new SkillAction(Woodcutting.this));
                remove();
            }
        }, Action.LEFT_CLICK_BLOCK);
    }

    private int walkInDirection(PlayerInteractTrigger trigger, BlockFace direction, Block startBlock, int amount) {

        BlockBreakEvent event;
        while (amount < getMaxAmount() && isTreeMaterial(startBlock.getType())) {
            if (!RaidCraft.isPlayerPlacedBlock(startBlock)) {
                // check if blocked
                event = new BlockBreakEvent(startBlock, trigger.getEvent().getPlayer());
                RaidCraft.callEvent(event);
                if (!event.isCancelled()) {
                    startBlock.breakNaturally(trigger.getEvent().getPlayer().getItemInHand());
                    amount++;
                }
                removeLeaves(startBlock, trigger.getEvent().getPlayer());
            }
            startBlock = startBlock.getRelative(direction);
        }
        return amount;
    }

    public void removeLeaves(Block startBlock, Player player) {

        Set<Block> blocks = BlockUtil.getBlocksFlat(startBlock, getLeaveRadius(), getMatsLeaves());
        for (Block block : blocks) {
            BlockBreakEvent event = new BlockBreakEvent(block, player);
            RaidCraft.callEvent(event);
            if (!event.isCancelled()) {
                block.breakNaturally(player.getItemInHand());
            }
        }
    }

    public boolean isTreeMaterial(Material mat) {

        return matsTree.contains(mat);
    }

    public boolean isLeaveMaterial(Material mat) {

        return matsLeaves.contains(mat);
    }

    public boolean isValidTool(PlayerInteractEvent event) {

        return event.getItem() != null
                && allowedTools.contains(event.getItem().getType());
    }
}
