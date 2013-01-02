package de.raidcraft.skillsandeffects.skills.weapons;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.LevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.trigger.ItemHeldTrigger;
import de.raidcraft.skills.util.ItemUtil;
import de.raidcraft.util.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "weapons",
        desc = "Erhöht den Schaden deiner Waffen.",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING, EffectType.UNBINDABLE},
        triggerCombat = true
)
public class WeaponSkill extends AbstractLevelableSkill implements Triggered {

    private final Map<Material, Weapon> allowedWeapons = new EnumMap<>(Material.class);

    public WeaponSkill(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
        attachLevel(new WeaponLevel(this, database));
    }

    @Override
    public void load(ConfigurationSection data) {

        ConfigurationSection weapons = data.getConfigurationSection("weapons");
        if (weapons == null) return;
        for (String key : weapons.getKeys(false)) {
            Material item = ItemUtils.getItem(key);
            if (item != null) {
                Weapon weapon = new Weapon(item, data.getConfigurationSection("weapons." + key));
                allowedWeapons.put(item, weapon);
            } else {
                RaidCraft.LOGGER.warning("The item " + key + " in the skill config " + getName() + " is not an item.");
            }
        }
    }

    @Override
    public void apply() {

        checkTaskbar();
    }

    @Override
    public void remove() {

        allowedWeapons.clear();
        checkTaskbar();
    }

    @Override
    public void onLevelGain() {

        super.onLevelGain();
        for (Weapon weapon : allowedWeapons.values()) {
            if (weapon.getRequiredLevel() <= getLevel().getLevel()) {
                getHero().sendMessage(ChatColor.GREEN + "Neue Waffe freigeschaltet: " +
                        ItemUtils.getFriendlyName(weapon.getType(), ItemUtils.Language.GERMAN));
            }
        }
        checkTaskbar();
    }

    @TriggerHandler
    public void onAttack(AttackTrigger trigger) {

        checkTaskbar();
        if (trigger.getAttack().isCancelled()) {
            return;
        }
        ItemStack item = trigger.getHero().getPlayer().getItemInHand();
        if (item == null || item.getTypeId() == 0 || !allowedWeapons.containsKey(item.getType())) {
            trigger.getAttack().setCancelled(true);
            return;
        }
        int oldDamage = trigger.getAttack().getDamage();
        Weapon weapon = allowedWeapons.get(item.getType());
        trigger.getAttack().setDamage(weapon.getTotalDamage(this));
        getHero().debug("damaged changed " + oldDamage + "->" + trigger.getAttack().getDamage());
        getLevel().addExp(weapon.getExpForUse());
    }

    @TriggerHandler
    public void onItemHeld(ItemHeldTrigger trigger) {

        checkTaskbar();
    }

    private void checkTaskbar() {

        PlayerInventory inventory = getHero().getPlayer().getInventory();
        boolean movedItem = false;
        // 0-8 are the slot ids in the taskbar
        for (int i = 0; i < 9; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getTypeId() == 0) {
                continue;
            }
            if (!allowedWeapons.containsKey(item.getType())
                    || allowedWeapons.get(item.getType()).getRequiredLevel() > getLevel().getLevel()) {
                ItemUtil.moveItem(getHero(), i, item);
                movedItem = true;
            }
        }
        if (movedItem) {
            getHero().sendMessage(ChatColor.RED + "Du kannst diese Waffe nicht tragen. Sie wurde in dein Inventar gelegt.");
        }
    }

    public static class Weapon {

        private final Material type;
        private final ConfigurationSection config;

        public Weapon(Material type, ConfigurationSection config) {

            this.type = type;
            this.config = config;
        }

        public Material getType() {

            return type;
        }

        public int getTotalDamage(LevelableSkill skill) {

            return (int) (config.getInt("damage.base", 1)
                    + config.getDouble("damage.level-modifier", 0.0) * skill.getHero().getLevel().getLevel()
                    + config.getDouble("damage.prof-level-modifier", 0.0) * skill.getProfession().getLevel().getLevel()
                    + config.getDouble("damage.skill-level-modifier", 0.0) * skill.getLevel().getLevel());
        }

        public int getRequiredLevel() {

            return config.getInt("level", 1);
        }

        public int getExpForUse() {

            return config.getInt("exp", 2);
        }
    }
}
