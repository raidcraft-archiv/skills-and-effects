package de.raidcraft.skillsandeffects.pvp.skills.armor;

import de.raidcraft.api.items.WeaponType;
import de.raidcraft.skills.api.combat.AttackSource;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.SkillAction;
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
import de.raidcraft.skillsandeffects.pvp.effects.misc.ParryEffect;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Parry",
        description = "Hat die Chance einen Angriff zu parrieren.",
        types = {EffectType.HELPFUL, EffectType.ABSORBING, EffectType.PHYSICAL},
        configUsage = {
            "weapon[string]: requires valid weapon type",
            "chance[baseSection]: chance to parry"
        },
        effects = {ParryEffect.class}
)
public class Parry extends AbstractLevelableSkill implements Triggered {

    private WeaponType weapon;
    private ConfigurationSection chance;

    public Parry(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        weapon = WeaponType.fromString(data.getString("weapon", "sword"));
        chance = data.getConfigurationSection("chance");
    }

    private double getParryChance() {

        return ConfigUtil.getTotalValue(this, chance);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.LOW)
    public void onDamage(DamageTrigger trigger) throws CombatException {

        if (!trigger.getAttack().isOfAttackType(EffectType.PHYSICAL)
                || trigger.getAttack().getAttackSource() == AttackSource.ENVIRONMENT
                || getHolder().hasEffect(ParryEffect.class)
                || !canUseAbility()) {
            return;
        }
        if (Math.random() < getParryChance()) {
            addEffect(ParryEffect.class);
            getHolder().combatLog(this, "Angriff von " + trigger.getAttack().getSource() + " wurde parriert.");
            getAttachedLevel().addExp(getUseExp());
            substractUsageCost(new SkillAction(this));
            throw new CombatException(CombatException.Type.PARRIED);
        }
    }
}