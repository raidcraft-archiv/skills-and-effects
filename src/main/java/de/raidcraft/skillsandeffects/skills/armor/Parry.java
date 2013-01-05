package de.raidcraft.skillsandeffects.skills.armor;

import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.skills.ConfigurableSkillLevel;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Parry",
        desc = "Hat die Chance einen Angriff zu parrieren.",
        types = {EffectType.HELPFUL, EffectType.ABSORBING, EffectType.PHYSICAL}
)
public class Parry extends AbstractLevelableSkill implements Triggered {

    private Material weapon;
    private ConfigurationSection chance;
    private int exp;

    public Parry(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        attachLevel(new ConfigurableSkillLevel(this, database, data));
        weapon = ItemUtils.getItem(data.getString("weapon"));
        chance = data.getConfigurationSection("chance");
        exp = data.getInt("exp");
    }

    private double getParyChance() {

        return ConfigUtil.getTotalValue(this, chance);
    }

    @TriggerHandler
    public void onDamage(DamageTrigger trigger) throws CombatException {

        if (!trigger.getAttack().isOfAttackType(EffectType.PHYSICAL)
                || getHero().getItemTypeInHand() != weapon) {
            return;
        }
        if (Math.random() < getParyChance()) {
            getHero().combatLog("Angriff von " + trigger.getAttack().getSource() + " wurde parriert.");
            getLevel().addExp(exp);
            throw new CombatException(CombatException.Type.PARRIED);
        }
    }

    @Override
    public void apply() {

    }

    @Override
    public void remove() {

    }
}
