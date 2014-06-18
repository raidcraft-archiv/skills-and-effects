package de.raidcraft.skillsandeffects.pve.effects.tools;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.PlayerInteractTrigger;
import de.raidcraft.skillsandeffects.pve.skills.SpeedBlockBreak;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * @author Philip
 */
@EffectInformation(
        name = "Speed Block Break",
        description = "Blitzschnelles abbauen von Blöcken"
)
public class SpeedBlockBreakEffect extends ExpirableEffect<SpeedBlockBreak> implements Triggered {


    public SpeedBlockBreakEffect(SpeedBlockBreak source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

    }

    @Override
    public void load(ConfigurationSection data) {

    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

    }

    @TriggerHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractTrigger trigger) throws CombatException {

        PlayerInteractEvent event = trigger.getEvent();
        if (event.getAction() != Action.LEFT_CLICK_BLOCK || !getSource().isValid(trigger)) {
            return;
        }

        BlockBreakEvent blockBreakEvent = new BlockBreakEvent(event.getClickedBlock(), event.getPlayer());
        RaidCraft.callEvent(blockBreakEvent);

        if (!blockBreakEvent.isCancelled()) {
            blockBreakEvent.getBlock().breakNaturally(blockBreakEvent.getPlayer().getItemInHand());
            event.setCancelled(true);
        }
    }
}
