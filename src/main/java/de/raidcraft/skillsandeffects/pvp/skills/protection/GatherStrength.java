package de.raidcraft.skillsandeffects.pvp.skills.protection;

import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.healing.GatherStrengthEffect;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Gather Strength",
        description = "Wenn deine leben unter 35% fallen heilst du dich um einen Prozentsatz.",
        types = {EffectType.HELPFUL, EffectType.PROTECTION, EffectType.HEALING}
)
public class GatherStrength extends AbstractSkill implements Triggered {

    private double triggerPercent = 0.35;

    public GatherStrength(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        triggerPercent = data.getDouble("trigger-at");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        int damage = trigger.getAttack().getDamage();
        if (getHero().getHealth() - damage < getHero().getMaxHealth() * triggerPercent) {
            if (!getHero().hasEffect(GatherStrengthEffect.class) && canUseSkill()) {
                addEffect(getHero(), GatherStrengthEffect.class);
                substractUsageCost();
            }
        }
    }
}
