package de.raidcraft.skillsandeffects.pvp.skills.protection;

import de.raidcraft.skills.api.combat.AttackSource;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Evade",
        description = "Geringe Chance bei Angriffen auszuweichen.",
        types = {EffectType.PROTECTION, EffectType.HELPFUL, EffectType.PHYSICAL}
)
public class Evade extends AbstractSkill implements Triggered {

    private ConfigurationSection evadeChance;

    public Evade(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        this.evadeChance = data.getConfigurationSection("evade-chance");
    }

    private double getEvadeChance() {

        return ConfigUtil.getTotalValue(this, evadeChance);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.HIGHEST)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        if (!trigger.getAttack().isOfAttackType(EffectType.PHYSICAL)
                || trigger.getAttack().getAttackSource() == AttackSource.ENVIRONMENT
                || trigger.getAttack().getAttackSource() == AttackSource.EFFECT) {
            return;
        }

        if (Math.random() < getEvadeChance()) {
            throw new CombatException(CombatException.Type.EVADED);
        }
    }
}
