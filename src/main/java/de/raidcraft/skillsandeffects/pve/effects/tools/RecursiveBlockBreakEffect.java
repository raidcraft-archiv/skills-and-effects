package de.raidcraft.skillsandeffects.pve.effects.tools;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.language.Translator;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.PlayerInteractTrigger;
import de.raidcraft.skillsandeffects.pve.skills.RecursiveBlockBreak;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 * @author Philip
 */
@EffectInformation(
        name = "Recursive Block Break",
        description = "Breaks the defined amount of blocks recursivly."
)
public class RecursiveBlockBreakEffect extends ExpirableEffect<RecursiveBlockBreak> implements Triggered {

    public RecursiveBlockBreakEffect(RecursiveBlockBreak source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
        // hard set the duration to 5s
        setDuration(5.0);
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractTrigger trigger) throws CombatException {

        PlayerInteractEvent event = trigger.getEvent();

        if (event.getAction() != Action.LEFT_CLICK_BLOCK
                || !getSource().isValidTool(event)
                || !getSource().isValidBlock(event)) {
            return;
        }

        int amount = breakRecursive(event.getPlayer(), event.getItem(), event.getClickedBlock(), 0, getSource().getMaxAmount());
        info(Translator.tr(SkillsPlugin.class, event.getPlayer(),
                "skills.recursive-block-break.break-amount", "You have destroyed {0} blocks with {1}.", amount, getFriendlyName()));
        remove();
    }

    public int breakRecursive(Player player, ItemStack tool, Block block, int currentAmount, int maxAmount) {

        if (currentAmount < maxAmount && getSource().getAllowedBlocks().contains(block.getType())) {
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

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

    }
}