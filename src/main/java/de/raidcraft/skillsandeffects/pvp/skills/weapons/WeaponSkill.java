package de.raidcraft.skillsandeffects.pvp.skills.weapons;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.events.RCCombatEvent;
import de.raidcraft.skills.api.exceptions.CombatException;
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
import de.raidcraft.skills.trigger.CombatTrigger;
import de.raidcraft.skills.trigger.ItemHeldTrigger;
import de.raidcraft.skills.util.ItemUtil;
import de.raidcraft.util.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "weapons",
        desc = "Erhöht den Schaden deiner Waffen.",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING, EffectType.DEFAULT_ATTACK}
)
public class WeaponSkill extends AbstractLevelableSkill implements Triggered {

    private static final Map<String, Map<Material, Weapon>> allowedWeapons = new HashMap<>();
    private final Set<Material> myWeapons = new HashSet<>();

    public WeaponSkill(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        if (!allowedWeapons.containsKey(getHero().getName())) {
            allowedWeapons.put(getHero().getName(), new EnumMap<Material, Weapon>(Material.class));
        }
        ConfigurationSection weapons = data.getConfigurationSection("weapons");
        if (weapons == null) return;
        for (String key : weapons.getKeys(false)) {
            Material item = ItemUtils.getItem(key);
            if (item != null && ItemUtil.isWeapon(item)) {
                Weapon weapon = new Weapon(item, data.getConfigurationSection("weapons." + key));
                allowedWeapons.get(getHero().getName()).put(item, weapon);
                myWeapons.add(item);
                getHero().debug("Weapon loaded: " + weapon.getType() + ":L" + weapon.getRequiredLevel());
            } else {
                RaidCraft.LOGGER.warning("The item " + key + " in the skill config " + getName() + " is not an item.");
            }
        }
    }

    @Override
    public void onLevelGain() {

        super.onLevelGain();
        for (Weapon weapon : allowedWeapons.get(getHero().getName()).values()) {
            if (weapon.getRequiredLevel() == getLevel().getLevel()) {
                getHero().sendMessage(ChatColor.GREEN + "Neue Waffe freigeschaltet: " +
                        ItemUtils.getFriendlyName(weapon.getType(), ItemUtils.Language.GERMAN));
            }
        }
        checkTaskbar(getHero().getPlayer().getInventory().getHeldItemSlot());
    }

    @TriggerHandler
    public void onAttack(AttackTrigger trigger) throws CombatException {

        if (trigger.getAttack().isCancelled()) {
            return;
        }
        boolean movedItems = checkTaskbar(getHero().getPlayer().getInventory().getHeldItemSlot());

        ItemStack item = getHero().getPlayer().getItemInHand();
        if (item == null || item.getTypeId() == 0
                || !ItemUtil.isWeapon(item.getType())) {
            return;
        }

        if(!allowedWeapons.get(getHero().getName()).containsKey(item.getType()) || movedItems) {
            checkTaskbar(getHero().getPlayer().getInventory().getHeldItemSlot());
            trigger.setCancelled(true);
            trigger.getAttack().setCancelled(true);
            throw new CombatException(CombatException.Type.INVALID_WEAPON);
        }

        if (!myWeapons.contains(item.getType())) {
            return;
        }

        int oldDamage = trigger.getAttack().getDamage();
        Weapon weapon = allowedWeapons.get(getHero().getName()).get(item.getType());
        trigger.getAttack().setDamage(weapon.getTotalDamage(this));
        getHero().debug("damaged changed " + oldDamage + "->" + trigger.getAttack().getDamage());
        getLevel().addExp(weapon.getExpForUse());
    }

    @TriggerHandler
    public void onItemHeld(ItemHeldTrigger trigger) {

        checkTaskbar(trigger.getEvent().getNewSlot());
    }

    @TriggerHandler
    public void onCombat(CombatTrigger trigger) {

        if (trigger.getEvent().getType() == RCCombatEvent.Type.ENTER) {
            checkTaskbar(getHero().getPlayer().getInventory().getHeldItemSlot());
        }
    }

    private boolean checkTaskbar(int slot) {

        if (!getHero().isInCombat()) {
            return false;
        }

        PlayerInventory inventory = getHero().getPlayer().getInventory();
        boolean movedItem = false;

        // only check the slot he is currently holding
        ItemStack item = inventory.getItem(slot);
        if (item == null || item.getTypeId() == 0 || !ItemUtil.isWeapon(item.getType())) {
            return false;
        }
        Weapon weapon = allowedWeapons.get(getHero().getName()).get(item.getType());
        // this can be null at this point
        if (!allowedWeapons.get(getHero().getName()).containsKey(item.getType())
                || weapon.getRequiredLevel() > getLevel().getLevel()) {
            ItemUtil.moveItem(getHero(), slot, item);
            movedItem = true;
        }

        if (movedItem) {
            getHero().sendMessage(ChatColor.RED + "Du kannst diese Waffe nicht tragen. Sie wurde in dein Inventar gelegt.");
        }
        return movedItem;
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

        @Override
        public boolean equals(Object o) {

            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Weapon weapon = (Weapon) o;

            return type == weapon.type;

        }

        @Override
        public int hashCode() {

            return type.hashCode();
        }
    }
}
