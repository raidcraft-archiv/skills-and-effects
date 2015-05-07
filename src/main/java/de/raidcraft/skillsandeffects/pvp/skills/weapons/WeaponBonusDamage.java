package de.raidcraft.skillsandeffects.pvp.skills.weapons;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.CustomWeapon;
import de.raidcraft.api.items.WeaponType;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.WeaponAttack;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.util.CustomItemUtil;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Weapon Bonus",
        description = "Ermöglicht es mehr Schaden mit bestimmten Waffen zu verursachen.",
        types = {EffectType.PHYSICAL, EffectType.DEFAULT_ATTACK, EffectType.DAMAGING, EffectType.HELPFUL}
)
public class WeaponBonusDamage extends AbstractLevelableSkill implements Triggered {

    private final Map<WeaponType, ConfigurationSection> bonusDamage = new EnumMap<>(WeaponType.class);
    private boolean allAttacks = false;
    private double expPerDamage;

    public WeaponBonusDamage(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        allAttacks = data.getBoolean("all-attacks", false);
        expPerDamage = data.getDouble("exp-per-damage", 0.0);
        ConfigurationSection weapons = data.getConfigurationSection("weapons");
        if (weapons == null || weapons.getKeys(false) == null) return;
        for (String key : weapons.getKeys(false)) {
            WeaponType type = WeaponType.fromString(key);
            if (type != null) {
                bonusDamage.put(type, weapons.getConfigurationSection(key));
            } else {
                RaidCraft.LOGGER.warning("Unknown Weapon Type " + key + " in skill config: " + getName() + ".yml");
            }
        }
    }

    @Override
    public String getDescription() {

        if (bonusDamage.size() < 1) {
            return super.getDescription();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.GRAY).append("Erhöht den Schaden mit folgenden Waffen: ");
        for (WeaponType weaponType : bonusDamage.keySet()) {
            sb.append(ChatColor.YELLOW).append("\n\t- ").append(ChatColor.AQUA).append(weaponType.getGermanName()).append(ChatColor.RED);
            sb.append((int) getBonusDamage(weaponType) * 100).append("%");
        }
        return sb.toString();
    }

    public double getBonusDamage(WeaponType weaponType) {

        if (bonusDamage.containsKey(weaponType)) {
            return ConfigUtil.getTotalValue(this, bonusDamage.get(weaponType));
        }
        return 0;
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.LOWEST)
    public void onAttack(AttackTrigger trigger) {

        if (!allAttacks && !trigger.getAttack().isOfAttackType(EffectType.DEFAULT_ATTACK)) {
            return;
        }
        if (!(trigger.getAttack() instanceof WeaponAttack)) {
            return;
        }
        WeaponAttack attack = (WeaponAttack) trigger.getAttack();
        for (CustomItemStack customItemStack : attack.getWeapons()) {
            CustomWeapon weapon = CustomItemUtil.getWeapon(customItemStack);
            if (bonusDamage.containsKey(weapon.getWeaponType())) {
                double oldDamage = attack.getDamage();
                double bonusDamage = getBonusDamage(weapon.getWeaponType());
                double newDamage = oldDamage + oldDamage * bonusDamage;
                attack.combatLog(this, "Waffenschaden um " + (newDamage - oldDamage) + "(" + ((int) (bonusDamage * 100)) + "%) erhöht.");
                attack.setDamage(newDamage);
                getAttachedLevel().addExp((int) (newDamage * expPerDamage) + getUseExp());
            }
        }
    }
}