package de.raidcraft.skillsandeffects.pvp.effects.resources;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.effect.EffectInformation;
import de.raidcraft.skills.api.effect.types.PeriodicEffect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.resource.Resource;
import de.raidcraft.skills.api.persistance.EffectData;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.skills.resources.Rage;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@EffectInformation(
        name = "Rage",
        description = "Regeneriert Wut wenn im Kampf"
)
public class RageEffect extends PeriodicEffect<Rage> implements Triggered {

    private ConfigurationSection rageAmount;
    private double ragePerAttackDamage = 0.1;
    private double ragePerDamage = 0.1;

    public RageEffect(Rage source, CharacterTemplate target, EffectData data) {

        super(source, target, data);
    }

    @Override
    public void load(ConfigurationSection data) {

        rageAmount = data.getConfigurationSection("rage-amount");
        ragePerAttackDamage = data.getDouble("rage-per-attack", 0.1);
        ragePerDamage = data.getDouble("rage-per-damage", 0.1);
    }

    public double getRagePerAttackDamage() {

        return ragePerAttackDamage;
    }

    public void setRagePerAttackDamage(double ragePerAttackDamage) {

        this.ragePerAttackDamage = ragePerAttackDamage;
    }

    public double getRagePerDamage() {

        return ragePerDamage;
    }

    public void setRagePerDamage(double ragePerDamage) {

        this.ragePerDamage = ragePerDamage;
    }

    private int getRageAmount() {

        // rage amount per interval tick
        return (int) ConfigUtil.getTotalValue(getSource(), rageAmount);
    }

    @TriggerHandler
    public void onAttack(AttackTrigger trigger) {

        Resource resource = getSource().getResource();
        resource.setCurrent((int) (resource.getCurrent() + trigger.getAttack().getDamage() * ragePerAttackDamage));
    }

    @TriggerHandler
    public void onDamage(DamageTrigger trigger) {

        Resource resource = getSource().getResource();
        resource.setCurrent((int) (resource.getCurrent() + trigger.getAttack().getDamage() * ragePerDamage));
    }

    @Override
    protected void tick(CharacterTemplate target) throws CombatException {

        Resource resource = getSource().getResource();
        resource.setCurrent(resource.getCurrent() + getRageAmount());
    }

    @Override
    protected void apply(CharacterTemplate target) throws CombatException {

        // lets disable the normal rage deregeneration
        getSource().getResource().setEnabled(false);
    }

    @Override
    protected void remove(CharacterTemplate target) throws CombatException {

        // reenable the rage deregeneration
        getSource().getResource().setEnabled(true);
    }

    @Override
    protected void renew(CharacterTemplate target) throws CombatException {

    }
}
