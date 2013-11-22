package de.raidcraft.skillsandeffects.pvp.effects.misc;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.common.Combat;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.events.RCCombatEvent;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.CombatTrigger;
import de.raidcraft.skillsandeffects.pvp.skills.movement.Retreat;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Retreat",
        description = "Retreats from combat and blocks incoming damge for 1s",
        types = {EffectType.BUFF, EffectType.HELPFUL, EffectType.MOVEMENT}
)
public class RetreatEffect extends ExpirableEffect<Retreat> implements Triggered {

    public RetreatEffect(Retreat source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
        duration = 20;
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onCombat(CombatTrigger trigger) {

        if (getSource().isRemovingCombat() && trigger.getEvent().getType() == RCCombatEvent.Type.ENTER) {
            trigger.getEvent().setCancelled(true);
        }
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        target.removeEffect(Combat.class);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        target.removeEffect(Combat.class);
    }
}
