package de.raidcraft.skillsandeffects.effects.movement;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.callback.Callback;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.ExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.DamageTrigger;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Charging",
        description = "Gerade dabei einen Gegener anzustürmen.",
        types = {EffectType.PHYSICAL, EffectType.HELPFUL, EffectType.MOVEMENT}
)
public class Charging extends ExpirableEffect<Skill> implements Triggered {

    private final PotionEffect speed;
    private Callback<DamageTrigger> callback;

    public Charging(Skill source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
        this.duration = 30;
        this.speed = new PotionEffect(PotionEffectType.SPEED, 30, 3, false);
    }


    public void addCallback(Callback<DamageTrigger> callback) {

        this.callback = callback;
    }

    public void onDamage(DamageTrigger trigger) throws CombatException {

        if (trigger.getCause() == EntityDamageEvent.DamageCause.FALL) {
            // lets cancel the damage
            trigger.getAttack().setCancelled(true);
            // call the callback if exists
            if (callback != null) {
                callback.run(trigger);
            }
            // and remove the effect since the player landed
            remove();
        }
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        target.getEntity().addPotionEffect(speed);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        target.getEntity().removePotionEffect(PotionEffectType.SPEED);
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

    }
}
