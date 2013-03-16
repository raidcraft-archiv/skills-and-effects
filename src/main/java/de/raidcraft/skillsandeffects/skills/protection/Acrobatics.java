package de.raidcraft.skillsandeffects.skills.protection;

import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.DamageTrigger;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Map;

/**
 * @author Philip
 */
@SkillInformation(
        name = "Acrobatics",
        desc = "Minimiert Fallschaden",
        types = {EffectType.REDUCING},
        triggerCombat = false
)
public class Acrobatics extends AbstractLevelableSkill implements Triggered {

    private double expPerDamage;
    private double rollChancePerLevel;
    private Map<String, Object> data;

    public Acrobatics(Hero hero, SkillProperties skillData, Profession profession, THeroSkill database) {

        super(hero, skillData, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        this.data = data.getValues(false);
        this.expPerDamage = data.getDouble("exp-per-damage", 1.0);
        this.rollChancePerLevel = data.getDouble("roll-chance-per-level", 0.1);
    }

    /*
     * Level increase and damage reduction
     */
    @TriggerHandler(checkUsage = false)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        if (trigger.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        getHero().debug("Fall damage trigger called");

        getLevel().addExp((int) (expPerDamage * (double) trigger.getAttack().getDamage()));

        // calculate roll chance
        double chance = getLevel().getLevel() * rollChancePerLevel;
        double random = Math.random() * 100.;
        if (chance > random) {
            trigger.getAttack().setDamage(trigger.getAttack().getDamage() / 2); // half damage
            getHero().getPlayer().sendMessage(ChatColor.GREEN + "**roll**");
        }
    }
}
