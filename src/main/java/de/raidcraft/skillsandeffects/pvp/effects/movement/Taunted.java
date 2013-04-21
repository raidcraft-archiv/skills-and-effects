package de.raidcraft.skillsandeffects.pvp.effects.movement;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.DiminishingReturnType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.EntityTargetTrigger;
import org.bukkit.entity.Creature;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Taunted",
        description = "Du kannst nur dein Ziel angreifen.",
        types = {EffectType.DEBUFF, EffectType.HARMFUL, EffectType.MAGICAL},
        priority = 1.0,
        diminishingReturn = DiminishingReturnType.TAUNT
)
public class Taunted extends ExpirableEffect<CharacterTemplate> implements Triggered {


    public Taunted(CharacterTemplate source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @TriggerHandler
    public void onTargetChange(EntityTargetTrigger trigger) {

        trigger.getEvent().setTarget(getSource().getEntity());
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        if (target.getEntity() instanceof Creature) {
            ((Creature) target.getEntity()).setTarget(getSource().getTarget().getEntity());
        } else if (target instanceof Hero) {
            // TODO: set player position so that he looks at the source
        }
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

    }
}
