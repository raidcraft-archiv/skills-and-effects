package de.raidcraft.skillsandeffects.pvp.skills.armor;

import de.raidcraft.api.items.ArmorType;
import de.raidcraft.api.items.CustomArmor;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.EquipmentSlot;
import de.raidcraft.skills.api.combat.AttackSource;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.SkillAction;
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
import de.raidcraft.util.MathUtil;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Shield Block",
        description = "Absorbiert Schaden durch das Tragen eines Schildes.",
        types = {EffectType.HELPFUL, EffectType.ABSORBING, EffectType.PHYSICAL},
        configUsage = {
                "block-value[baseSection]: damage value that is blocked",
                "block-chance[baseSection]: chance to block"
        }
)
public class ShieldBlock extends AbstractLevelableSkill implements Triggered {

    private ConfigurationSection blockValue;
    private ConfigurationSection blockChance;

    public ShieldBlock(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        blockValue = data.getConfigurationSection("block-value");
        blockChance = data.getConfigurationSection("block-chance");
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.LOWEST)
    public void onDamage(DamageTrigger trigger) {

        if (trigger.getAttack().hasSource(AttackSource.ENVIRONMENT)
                || !trigger.getAttack().isOfAttackType(EffectType.PHYSICAL)
                || !canUseAbility()) {
            return;
        }
        // check if the player is wearing a shield
        CustomItemStack armor = getHolder().getArmor(EquipmentSlot.SHIELD_HAND);
        if (armor == null || !(armor.getItem() instanceof CustomArmor) || ((CustomArmor) armor.getItem()).getArmorType() != ArmorType.SHIELD) {
            return;
        }
        if (Math.random() < getBlockChance()) {
            double oldDamage = trigger.getAttack().getDamage();
            double blockValue = getBlockValue();
            double newDamage = oldDamage - oldDamage * blockValue;
            trigger.getAttack().setDamage(newDamage);
            getAttachedLevel().addExp(getUseExp());
            substractUsageCost(new SkillAction(this));
            getHolder().combatLog(this, "Schaden um " + (oldDamage - newDamage) + "(" + MathUtil.toPercent(blockValue) + "%) verringert.");
        }
    }

    private double getBlockChance() {

        return ConfigUtil.getTotalValue(this, blockChance);
    }

    private double getBlockValue() {

        return ConfigUtil.getTotalValue(this, blockValue);
    }
}