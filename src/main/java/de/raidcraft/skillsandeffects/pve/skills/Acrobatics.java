package de.raidcraft.skillsandeffects.pve.skills;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * @author Philip
 */
@SkillInformation(
        name = "Acrobatics",
        description = "Minimiert Fallschaden",
        types = {EffectType.REDUCING},
        triggerCombat = false
)
public class Acrobatics extends AbstractLevelableSkill implements Triggered {

    private double expPerHeight;
    private ConfigurationSection rollChance;

    public Acrobatics(Hero hero, SkillProperties skillData, Profession profession, THeroSkill database) {

        super(hero, skillData, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        this.expPerHeight = data.getDouble("exp-per-height", 1.0);
        this.rollChance = data.getConfigurationSection("roll-chance");
    }

    public double getRollChance() {

        return ConfigUtil.getTotalValue(this, rollChance);
    }

    public int getFallHeight(double takenDamage) {

        SkillsPlugin plugin = RaidCraft.getComponent(SkillsPlugin.class);
        // the minecraft formula for calculating fall damage is as follows - http://www.minecraftwiki.net/wiki/Fall_Damage#Fall_damage
        // FALL_DAMAGE = number of blocks - 3

        int height;
        // this is the damage modifier for the taken minecraft damage
        double damageModifier = plugin.getDamageManager().getEnvironmentalDamage(EntityDamageEvent.DamageCause.FALL);
        // if environment damage is calculated in percentage of player max health and multiplied by the height
        if (plugin.getCommonConfig().environment_damage_in_percent) {
            // our formula to calculate the height goes as follows
            // height = (takenDamage / maxHealth) / damageModifier + 3
            // the 3 extra block we receive no mc damage for
            height = (int) Math.round((takenDamage / getHero().getMaxHealth()) / damageModifier) + 3;
        } else {
            // we need to calculate the base minecraft damage and then the height
            // height = (takenDamage / damageModifier) + 3
            // the 3 extra block we receive no mc damage for
            height = (int) (Math.round(takenDamage / damageModifier) + 3);
        }
        return height;
    }

    /*
     * Level increase and damage reduction
     */
    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        if (trigger.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        int fallHeight = getFallHeight(trigger.getAttack().getDamage());
        // lets add the exp for the fallen block height
        getAttachedLevel().addExp((int) (expPerHeight * fallHeight));

        // calculate roll chance
        if (Math.random() < getRollChance()) {
            trigger.getAttack().setDamage(trigger.getAttack().getDamage() / 2); // half damage
            getHero().getPlayer().sendMessage(ChatColor.GREEN + "**abgerollt**");
        }
    }
}
