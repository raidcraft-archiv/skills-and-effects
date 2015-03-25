package de.raidcraft.skillsandeffects.pvp.skills.weapons;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItemStack;
import de.raidcraft.api.items.CustomWeapon;
import de.raidcraft.api.items.WeaponType;
import de.raidcraft.skills.api.combat.EffectType;
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
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "weapons",
        description = "Erhöht den Schaden deiner Waffen.",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING, EffectType.DEFAULT_ATTACK}
)
public class WeaponSkill extends AbstractLevelableSkill implements Triggered {

    private WeaponType weaponType;

    public WeaponSkill(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        weaponType = WeaponType.fromString(data.getString("weapon-type"));
        if (weaponType == null) {
            RaidCraft.LOGGER.warning("Unknown weapon defined in config of " + getName());
        }
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onAttack(AttackTrigger trigger) {

        if (!trigger.getAttack().isOfAttackType(EffectType.DEFAULT_ATTACK)
                || weaponType == null) {
            return;
        }
        Collection<CustomItemStack> weapons = trigger.getSource().getWeapons();
        for (CustomItemStack weapon : weapons) {
            if (weapon.getItem() instanceof CustomWeapon) {
                if (((CustomWeapon) weapon.getItem()).getWeaponType() == weaponType) {
                    getAttachedLevel().addExp(getUseExp());
                }
            }
        }
    }
}
