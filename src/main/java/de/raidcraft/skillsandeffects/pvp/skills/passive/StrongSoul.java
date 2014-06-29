package de.raidcraft.skillsandeffects.pvp.skills.passive;

import de.raidcraft.skills.api.combat.EffectElement;
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
import de.raidcraft.skills.trigger.HealTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pvp.effects.misc.WeakenSoul;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Strong Soul",
        description = "Verringert die Abklingzeit von Schwächender Seele auf dem Ziel.",
        types = {EffectType.MAGICAL, EffectType.HELPFUL, EffectType.BUFF},
        elements = {EffectElement.HOLY}
)
public class StrongSoul extends AbstractSkill implements Triggered {

    private ConfigurationSection cooldownReduction;

    public StrongSoul(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        cooldownReduction = data.getConfigurationSection("cooldown-reduction");
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR, filterTargets = false)
    public void onHeal(HealTrigger trigger) {

        if (!trigger.getSource().equals(getHolder()) || !trigger.getTarget().hasEffect(WeakenSoul.class)) {
            return;
        }
        trigger.getTarget().getEffects(WeakenSoul.class)
                .forEach(effect -> effect.setDuration(effect.getRemainingDuration() - getReduction()));
    }

    public double getReduction() {

        return ConfigUtil.getTotalValue(this, cooldownReduction);
    }
}
