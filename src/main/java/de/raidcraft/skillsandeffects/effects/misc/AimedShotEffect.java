package de.raidcraft.skillsandeffects.effects.misc;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.ProjectileType;
import de.raidcraft.skills.api.combat.action.RangedAttack;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.BowFireTrigger;
import de.raidcraft.skillsandeffects.skills.bow.AimedShot;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Aimed Shot",
        description = "Spannt den Bogen ganz fest und verursacht erhöhten Schaden.",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING, EffectType.HARMFUL}
)
public class AimedShotEffect extends ExpirableEffect<AimedShot> implements Triggered {

    public AimedShotEffect(AimedShot source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @TriggerHandler
    public void onBowShoot(BowFireTrigger trigger) throws CombatException {

        // bow force of 1.0 is max
        if (trigger.getEvent().getForce() >= 1.0) {
            new RangedAttack<>(getSource().getHero(), ProjectileType.ARROW, getSource().getTotalDamage()).run();
            remove();
        }
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

    }
}
