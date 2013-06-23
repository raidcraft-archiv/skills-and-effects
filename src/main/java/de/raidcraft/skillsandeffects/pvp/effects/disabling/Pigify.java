package de.raidcraft.skillsandeffects.pvp.effects.disabling;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.DiminishingReturnType;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.PeriodicExpirableEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
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
public class Pigify extends PeriodicExpirableEffect<Skill> implements Triggered {

    private double healthRegain;
    private boolean healthInPercent;
    private double damageTreshhold = 0.05;
    private int minimumSheepTime = 20;
    private int totalDamage = 0;
    private LivingEntity pig;

    public Pigify(Skill source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
        if (interval == 0) interval = 20;
    }

    @Override
    public void load(ConfigurationSection data) {

        healthRegain = ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("health-regain"));
        healthInPercent = data.getBoolean("health-regain.percent", false);
        damageTreshhold = data.getDouble("damage-cap", 0.05);
        minimumSheepTime = (int) (ConfigUtil.getTotalValue(getSource(), data.getConfigurationSection("min-time")) * 20);
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onAttack(AttackTrigger trigger) throws CombatException {

        if (trigger.getAttack().getTarget().getEntity().equals(pig)) {
            return;
        }
        trigger.setCancelled(true);
        trigger.getAttack().setCancelled(true);
        throw new CombatException("Du kannst nicht angreifen während du auf einem Schwein reitest.");
    }

    @TriggerHandler(ignoreCancelled = true, filterTargets = false)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        CharacterTemplate target = trigger.getAttack().getTarget();
        if (!target.equals(getTarget()) || target.getEntity().equals(pig)) {
            return;
        }
        if (getRemainingTicks() > getDuration() - minimumSheepTime) {
            trigger.setCancelled(true);
            return;
        }
        totalDamage += trigger.getAttack().getDamage();
        if (totalDamage > getTarget().getMaxHealth() * damageTreshhold) {
            remove();
        } else {
            trigger.getAttack().setCancelled(true);
            trigger.setCancelled(true);
        }
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        pig = target.getEntity().getWorld().spawn(target.getEntity().getLocation().add(0, 1, 0), Pig.class);
        renew(target);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        pig.getPassenger().leaveVehicle();
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

        pig.setMaxHealth(9999);
        pig.setHealth(pig.getMaxHealth());
        pig.setPassenger(target.getEntity());
    }
}
