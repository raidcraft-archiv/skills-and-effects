package de.raidcraft.skillsandeffects.pve.skills;

import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.PlayerDirection;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.world.World;
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
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.LinkedList;
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

    private static final int RANGE_SQUARED = 30 * 30;

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
            Block block = callback.getEvent().getClickedBlock();
            int amount = 0;
            int maxAmount = getMaxAmount();
            try {
                org.bukkit.World world = event.getPlayer().getWorld();
                Set<Vector> blocks = bfs(new BukkitWorld(world), new Vector(block.getX(), block.getY(), block.getZ()));
                for (Vector vector : blocks) {
                    if (amount >= maxAmount) break;
                    Block blockAt = world.getBlockAt(vector.getBlockX(), vector.getBlockY(), vector.getBlockZ());
                    blockAt.breakNaturally(callback.getEvent().getItem());
                    amount++;
                }
                info("Du hast " + amount + "/" + maxAmount + " Blöcke zerstört.");
            } catch (MaxChangedBlocksException e) {
                warn(e.getMessage());
            }
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

    Vector[] recurseDirections = {
            PlayerDirection.NORTH.vector(),
            PlayerDirection.EAST.vector(),
            PlayerDirection.SOUTH.vector(),
            PlayerDirection.WEST.vector(),
            PlayerDirection.UP.vector(),
            PlayerDirection.DOWN.vector(),
    };

    /**
     * Helper method.
     *
     * @param world  the world that contains the tree
     * @param origin any point contained in the floating tree
     *
     * @return a set containing all blocks in the tree/shroom or null if this is not a floating tree/shroom.
     */
    private Set<Vector> bfs(World world, Vector origin) throws MaxChangedBlocksException {

        final Set<Vector> visited = new HashSet<Vector>();
        final LinkedList<Vector> queue = new LinkedList<Vector>();

        queue.addLast(origin);
        visited.add(origin);

        while (!queue.isEmpty()) {
            final Vector current = queue.removeFirst();
            for (Vector recurseDirection : recurseDirections) {
                final Vector next = current.add(recurseDirection);
                if (origin.distanceSq(next) > RANGE_SQUARED) {
                    // Maximum range exceeded => stop walking
                    continue;
                }

                if (visited.add(next)) {
                    switch (world.getBlockType(next)) {
                        case BlockID.AIR:
                        case BlockID.SNOW:
                            // we hit air or snow => stop walking this route
                            continue;

                        case BlockID.LOG:
                        case BlockID.LOG2:
                        case BlockID.LEAVES:
                        case BlockID.LEAVES2:
                        case BlockID.BROWN_MUSHROOM_CAP:
                        case BlockID.RED_MUSHROOM_CAP:
                        case BlockID.VINE:
                            // queue next point
                            queue.addLast(next);
                            break;

                        default:
                            // we hit something solid => stop walking this route
                            continue;
                    } // switch
                } // if
            } // for
        } // while

        return visited;
    } // bfs
}
