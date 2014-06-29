package de.raidcraft.skillsandeffects.pvp.skills.bow;

import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.BowFireTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.util.Vector;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Bow Mastery",
        description = "Erhöht die Spannkraft des Bogens",
        types = {EffectType.PHYSICAL, EffectType.HELPFUL},
        configUsage = {"minimal-force[baseSection]: minimal force arrows are shot with"}
)
public class BowMastery extends AbstractSkill implements Triggered {

    private ConfigurationSection minimalForce;

    public BowMastery(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        minimalForce = data.getConfigurationSection("minimal-force");
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onBowFire(BowFireTrigger trigger) {

        float minimalForce = getMinimalForce();
        if (trigger.getEvent().getForce() < minimalForce) {
            Entity projectile = trigger.getEvent().getProjectile();
            Vector velocity = projectile.getVelocity();
            velocity.multiply(minimalForce + 1);
            projectile.setVelocity(velocity);
        }
    }

    public float getMinimalForce() {

        return (float) ConfigUtil.getTotalValue(this, minimalForce);
    }
}