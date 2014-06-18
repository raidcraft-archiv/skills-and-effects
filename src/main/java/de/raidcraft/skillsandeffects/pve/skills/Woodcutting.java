package de.raidcraft.skillsandeffects.pve.skills;

import com.sk89q.worldedit.blocks.BlockID;
import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.SkillAction;
import de.raidcraft.skills.api.combat.callback.Callback;
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
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;

/**
 * @author IDragonfire, Silthus
 */
@SkillInformation(name = "Woodcutting", description = "Fällt direkt einen Baum.", types = {EffectType.HELPFUL})
public class Woodcutting extends AbstractLevelableSkill implements Triggered {

    private ConfigurationSection maxBlockAmountConfig;

    public Woodcutting(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        maxBlockAmountConfig = data.getConfigurationSection("max-block-amount");
    }

    // TODO: holz in der krone abbauen
    @TriggerHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractTrigger trigger) throws CombatException {

        final Block targetBlock = trigger.getEvent().getClickedBlock();
        if (trigger.getEvent().getAction() != Action.RIGHT_CLICK_BLOCK || !isTreeMaterial(targetBlock.getTypeId())) {
            return;
        }

        queueInteract(new Callback<PlayerInteractTrigger>() {
            @Override
            public void run(PlayerInteractTrigger trigger) throws CombatException {

                checkUsage(new SkillAction(Woodcutting.this));
                int amount = 0;
                // first walk down
                amount = walkInDirection(trigger, BlockFace.DOWN, targetBlock, amount);
                // then walk up
                amount = walkInDirection(trigger, BlockFace.UP, targetBlock.getRelative(BlockFace.UP), amount);
                // TODO: format message?
                getHolder().sendMessage(
                        ChatColor.YELLOW + "Du hast direkt " + ChatColor.RED + amount + ChatColor.YELLOW
                                + " Holz abgebaut."
                );
                substractUsageCost(new SkillAction(Woodcutting.this));
            }
        }, Action.LEFT_CLICK_BLOCK);
    }

    private boolean isTreeMaterial(int mat) {

        return mat == BlockID.LOG;
    }

    private int walkInDirection(PlayerInteractTrigger trigger, BlockFace direction, Block startBlock, int amount) {

        BlockBreakEvent event;
        while (amount < getMaxBlockAmount() && isTreeMaterial(startBlock.getTypeId())) {
            if (!RaidCraft.isPlayerPlacedBlock(startBlock)) {
                // check if blocked
                event = new BlockBreakEvent(startBlock, trigger.getEvent().getPlayer());
                RaidCraft.callEvent(event);
                if (!event.isCancelled()) {
                    startBlock.breakNaturally(trigger.getEvent().getPlayer().getItemInHand());
                    amount++;
                }
            }
            startBlock = startBlock.getRelative(direction);
        }
        return amount;
    }

    private int getMaxBlockAmount() {

        return (int) ConfigUtil.getTotalValue(this, maxBlockAmountConfig);
    }
}
