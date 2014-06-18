package de.raidcraft.skillsandeffects.pvp.effects.misc;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.ProjectileType;
import de.raidcraft.skills.api.combat.action.RangedAttack;
import de.raidcraft.skills.api.combat.action.SkillAction;
import de.raidcraft.skills.api.combat.callback.ProjectileCallback;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.BowFireTrigger;
import de.raidcraft.skillsandeffects.pvp.skills.bow.AimedShot;
import org.bukkit.entity.Projectile;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Aimed Shot",
        description = "Spannt den Bogen ganz fest und verursacht erhöhten Schaden.",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING, EffectType.HELPFUL}
)
public class AimedShotEffect extends ExpirableEffect<AimedShot> implements Triggered {

    public AimedShotEffect(AimedShot source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @TriggerHandler
    public void onBowShoot(BowFireTrigger trigger) throws CombatException {

        if (getSource().getSkillProperties().getInformation().queuedAttack()) {
            getSource().substractUsageCost(new SkillAction(getSource()));
        }
        // bow force of 1.0 is max
        if (trigger.getEvent().getForce() >= 1.0) {
            RangedAttack<ProjectileCallback> attack = getSource().rangedAttack(
                    ProjectileType.ARROW,
                    getSource().getTotalDamage()
            );
            attack.setProjectile((Projectile) trigger.getEvent().getProjectile());
            attack.run();
            remove();
        }
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        info("Du sammelst all deine Kraft für einen gezielten Schuss.");
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

    }
}
