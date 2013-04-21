package de.raidcraft.skillsandeffects.pvp.effects.disabling;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.DiminishingReturnType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.PeriodicEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Pig;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Pigify",
        description = "Lässt das Ziel auf einem Schwein reiten und verhindert alle Angriffe.",
        types = {EffectType.DISABLEING, EffectType.DEBUFF},
        diminishingReturn = DiminishingReturnType.DISORIENT
)
public class Pigify extends PeriodicEffect<Skill> implements Triggered {

    private double healthRegain;
    private boolean healthInPercent;
    private double damageTreshhold = 0.05;
    private int totalDamage = 0;
    private Pig pig;

    public Pigify(Skill source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
        if (interval == 0) interval = 20;
    }

    @Override
    public void load(ConfigurationSection data) {

        healthRegain = ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("health-regain"));
        healthInPercent = data.getBoolean("health-regain.percent", false);
        damageTreshhold = data.getDouble("damage-cap", 0.05);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.LOWEST)
    public void onAttack(AttackTrigger trigger) throws CombatException {

        if (trigger.getAttack().getTarget().getEntity().equals(pig)) {
            return;
        }
        trigger.setCancelled(true);
        trigger.getAttack().setCancelled(true);
        throw new CombatException("Du kannst nicht angreifen während du auf einem Schwein reitest.");
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        totalDamage += trigger.getAttack().getDamage();
        if (totalDamage > getTarget().getMaxHealth() * damageTreshhold) {
            remove();
        }
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        pig = target.getEntity().getWorld().spawn(target.getEntity().getLocation(), Pig.class);
        pig.setPassenger(target.getEntity());
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        pig.setPassenger(null);
        pig.remove();
    }

    @Override
    protected void tick(CharacterTemplate target) throws CombatException {

        if (healthRegain > 0) {
            if (healthInPercent) {
                target.setHealth((int) (target.getHealth() + target.getMaxHealth() * healthRegain));
            } else {
                target.setHealth((int) (target.getHealth() + healthRegain));
            }
        }
        if (pig.isDead()) {
            remove();
        }
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

        pig.setPassenger(target.getEntity());
    }
}
