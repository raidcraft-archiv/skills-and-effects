package de.raidcraft.skillsandeffects.pve.skills;

import de.raidcraft.RaidCraft;
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
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.material.Tree;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
    private boolean treeFellerReachedThreshold = false;

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
            processTreeFeller(callback.getEvent().getClickedBlock().getState());
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

    /**
     * Begins Tree Feller
     *
     * @param blockState Block being broken
     */
    public void processTreeFeller(BlockState blockState) {

        Player player = getHolder().getPlayer();
        Set<BlockState> treeFellerBlocks = new HashSet<BlockState>();

        treeFellerReachedThreshold = false;

        processTree(blockState, treeFellerBlocks);

        // If the player is trying to break too many blocks
        if (treeFellerReachedThreshold) {
            treeFellerReachedThreshold = false;

            warn("Du kannst maximal " + getMaxAmount() + " Blöcke abbauen (" + treeFellerBlocks.size() + ")!");
            return;
        }

        dropBlocks(treeFellerBlocks);
        treeFellerReachedThreshold = false; // Reset the value after we're done with Tree Feller each time.
    }

    /**
     * Handles the dropping of blocks
     *
     * @param treeFellerBlocks List of blocks to be dropped
     */
    private void dropBlocks(Set<BlockState> treeFellerBlocks) {

        Player player = getHolder().getPlayer();
        int xp = 0;

        for (BlockState blockState : treeFellerBlocks) {
            Block block = blockState.getBlock();

            BlockBreakEvent event = new BlockBreakEvent(block, player);
            RaidCraft.callEvent(event);
            if (event.isCancelled()) {
                continue;
            }

            Material material = blockState.getType();

            if (blockState.getData() instanceof Tree) {
                Tree tree = (Tree) blockState.getData();
                tree.setDirection(BlockFace.UP);
            }

            switch (material) {
                case LOG:
                case LOG_2:
                    block.breakNaturally(player.getItemInHand());
                    break;
            }

            blockState.setType(Material.AIR);
            blockState.update(true);
        }
    }

    /**
     * The x/y differences to the blocks in a flat cylinder around the center
     * block, which is excluded.
     */
    private static final int[][] directions = {
            new int[]{-2, -1}, new int[]{-2, 0}, new int[]{-2, 1},
            new int[]{-1, -2}, new int[]{-1, -1}, new int[]{-1, 0}, new int[]{-1, 1}, new int[]{-1, 2},
            new int[]{0, -2}, new int[]{0, -1}, new int[]{0, 1}, new int[]{0, 2},
            new int[]{1, -2}, new int[]{1, -1}, new int[]{1, 0}, new int[]{1, 1}, new int[]{1, 2},
            new int[]{2, -1}, new int[]{2, 0}, new int[]{2, 1},
    };

    /**
     * Processes Tree Feller in a recursive manner
     *
     * @param blockState Block being checked
     * @param treeFellerBlocks List of blocks to be removed
     */
    /*
     * Algorithm: An int[][] of X/Z directions is created on static class
     * initialization, representing a cylinder with radius of about 2 - the
     * (0,0) center and all (+-2, +-2) corners are omitted.
     *
     * handleBlock() returns a boolean, which is used for the sole purpose of
     * switching between these two behaviors:
     *
     * (Call blockState "this log" for the below explanation.)
     *
     *  [A] There is another log above this log (TRUNK)
     *    Only the flat cylinder in the directions array is searched.
     *  [B] There is not another log above this log (BRANCH AND TOP)
     *    The cylinder in the directions array is extended up and down by 1
     *    block in the Y-axis, and the block below this log is checked as
     *    well. Due to the fact that the directions array will catch all
     *    blocks on a red mushroom, the special method for it is eliminated.
     *
     * This algorithm has been shown to achieve a performance of 2-5
     * milliseconds on regular trees and 10-15 milliseconds on jungle trees
     * once the JIT has optimized the function (use the ability about 4 times
     * before taking measurements).
     */
    protected void processTree(BlockState blockState, Set<BlockState> treeFellerBlocks) {

        List<BlockState> futureCenterBlocks = new ArrayList<BlockState>();

        // Check the block up and take different behavior (smaller search) if it's a log
        if (handleBlock(blockState.getBlock().getRelative(BlockFace.UP).getState(), futureCenterBlocks, treeFellerBlocks)) {
            for (int[] dir : directions) {
                handleBlock(blockState.getBlock().getRelative(dir[0], 0, dir[1]).getState(), futureCenterBlocks, treeFellerBlocks);

                if (treeFellerReachedThreshold) {
                    return;
                }
            }
        } else {
            // Cover DOWN
            handleBlock(blockState.getBlock().getRelative(BlockFace.DOWN).getState(), futureCenterBlocks, treeFellerBlocks);
            // Search in a cube
            for (int y = -1; y <= 1; y++) {
                for (int[] dir : directions) {
                    handleBlock(blockState.getBlock().getRelative(dir[0], y, dir[1]).getState(), futureCenterBlocks, treeFellerBlocks);

                    if (treeFellerReachedThreshold) {
                        return;
                    }
                }
            }
        }

        // Recursive call for each log found
        for (BlockState futureCenterBlock : futureCenterBlocks) {
            if (treeFellerReachedThreshold) {
                return;
            }

            processTree(futureCenterBlock, treeFellerBlocks);
        }
    }

    /**
     * Handle a block addition to the list of blocks to be removed and to the
     * list of blocks used for future recursive calls of
     * 'processTree()'
     *
     * @param blockState         Block to be added
     * @param futureCenterBlocks List of blocks that will be used to call
     *                           'processTree()'
     * @param treeFellerBlocks   List of blocks to be removed
     *
     * @return true if and only if the given blockState was a Log not already
     * in treeFellerBlocks.
     */
    private boolean handleBlock(BlockState blockState, List<BlockState> futureCenterBlocks, Set<BlockState> treeFellerBlocks) {

        // Without this check Tree Feller propagates through leaves until the threshold is hit
        if (treeFellerBlocks.size() > getMaxAmount()) {
            treeFellerReachedThreshold = true;
        }

        if (isLog(blockState)) {
            treeFellerBlocks.add(blockState);
            futureCenterBlocks.add(blockState);
            return true;
        } else if (isLeaves(blockState)) {
            treeFellerBlocks.add(blockState);
            return false;
        }
        return false;
    }

    /**
     * Check if a given block is a log
     *
     * @param blockState The {@link BlockState} of the block to check
     *
     * @return true if the block is a log, false otherwise
     */
    public boolean isLog(BlockState blockState) {

        switch (blockState.getType()) {
            case LOG:
            case LOG_2:
            case HUGE_MUSHROOM_1:
            case HUGE_MUSHROOM_2:
                return true;
        }
        return false;
    }

    /**
     * Check if a given block is a leaf
     *
     * @param blockState The {@link BlockState} of the block to check
     *
     * @return true if the block is a leaf, false otherwise
     */
    public boolean isLeaves(BlockState blockState) {

        switch (blockState.getType()) {
            case LEAVES:
            case LEAVES_2:
                return true;
        }
        return false;
    }
}
