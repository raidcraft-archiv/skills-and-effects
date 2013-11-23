package de.raidcraft.skillsandeffects.pvp.skills.passive;

import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.MaxHealthChangeTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Health Increase",
        description = "Erhöht die maximalen Leben des Zaubernden.",
        types = {EffectType.HELPFUL}
)
public class HealthIncrease extends AbstractSkill implements Triggered {

    private double increase;
    private int increaseAmount;

    public HealthIncrease(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        increase = ConfigUtil.getTotalValue(this, data.getConfigurationSection("health-increase"));
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.LOW)
    public void onMaxHealthChange(MaxHealthChangeTrigger trigger) {

        double value = trigger.getEvent().getValue();
        trigger.getEvent().setValue(value + (value * increase));
        increaseAmount += value * increase;
    }

    @Override
    public void apply() {

        increaseAmount = (int) (getHolder().getMaxHealth() * increase);
        getHolder().increaseMaxHealth(increaseAmount);
    }

    @Override
    public void remove() {

        getHolder().decreaseMaxHealth(increaseAmount);
    }
}
