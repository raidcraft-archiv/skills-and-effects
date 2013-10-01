package de.raidcraft.skillsandeffects.pvp.skills.passive;

import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.SkillAction;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Bonus Strike",
        description = "Chance auf einen extra Angriff",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING, EffectType.HELPFUL}
)
public class BonusStrike extends AbstractSkill implements Triggered {

    private ConfigurationSection chance;

    public BonusStrike(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        chance = data.getConfigurationSection("chance");
    }

    public double getChance() {

        return ConfigUtil.getTotalValue(this, chance);
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        if (!trigger.getAttack().isOfAttackType(EffectType.PHYSICAL)) {
            return;
        }
        if (!(trigger.getAttack().getSource() instanceof CharacterTemplate)) {
            return;
        }
        if (!canUseAbility()) {
            return;
        }
        if (Math.random() < getChance()) {
            attack((CharacterTemplate) trigger.getAttack().getSource());
            substractUsageCost(new SkillAction(this));
        }
    }
}
