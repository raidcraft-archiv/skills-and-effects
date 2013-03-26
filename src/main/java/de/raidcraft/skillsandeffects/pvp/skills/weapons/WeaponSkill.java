package de.raidcraft.skillsandeffects.pvp.skills.weapons;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.requirement.Requirement;
import de.raidcraft.api.requirement.RequirementManager;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.items.Weapon;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.trigger.InventoryCloseTrigger;
import de.raidcraft.skills.trigger.ItemHeldTrigger;
import de.raidcraft.skills.trigger.ItemPickupTrigger;
import de.raidcraft.skills.util.ItemUtil;
import de.raidcraft.util.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "weapons",
        description = "Erhöht den Schaden deiner Waffen.",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING, EffectType.DEFAULT_ATTACK}
)
public class WeaponSkill extends AbstractLevelableSkill implements Triggered {

    private final Map<Integer, Integer> allowedWeapons = new HashMap<>();
    private List<Requirement<WeaponSkill>> dualWielding;
    private boolean allowDualWielding = false;
    private double expPerDamage;
    private int expPerAttack;

    public WeaponSkill(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        dualWielding = RequirementManager.createRequirements(this, data.getConfigurationSection("dual-wielding"));
        expPerDamage = data.getDouble("exp-per-damage", 0.0);
        expPerAttack = data.getInt("exp-per-attack", 0);

        ConfigurationSection weapons = data.getConfigurationSection("weapons");
        if (weapons == null) return;
        for (String key : weapons.getKeys(false)) {
            Material item = ItemUtils.getItem(key);
            if (item != null && ItemUtil.isWeapon(item)) {
                allowedWeapons.put(item.getId(), weapons.getInt(key));
            } else {
                RaidCraft.LOGGER.warning("The item " + key + " in the skill config " + getName() + " is not an item.");
            }
        }
    }

    @Override
    public void onLevelGain() {

        super.onLevelGain();
        for (Map.Entry<Integer, Integer> entry : allowedWeapons.entrySet()) {

            if (entry.getValue() == getLevel().getLevel()) {
                getHero().sendMessage(ChatColor.GREEN + "Neue Waffe freigeschaltet: " +
                        ItemUtils.getFriendlyName(entry.getKey(), ItemUtils.Language.GERMAN));
            }
        }
        if (!allowDualWielding) {
            boolean isMet = true;
            for (Requirement<WeaponSkill> requirement : dualWielding) {
                if (!requirement.isMet()) {
                    isMet = false;
                }
            }
            if (isMet) {
                allowDualWielding = true;
                getHero().sendMessage(ChatColor.GREEN + "Du hast Beidhändigkeit erlernt und kannst nun mit zwei Waffen gleichzeitig angreifen.");
            }
        }
    }

    @Override
    public void onLevelLoss() {

        if (allowDualWielding) {
            boolean isMet = true;
            for (Requirement<WeaponSkill> requirement : dualWielding) {
                if (!requirement.isMet()) {
                    isMet = false;
                }
            }
            if (!isMet) {
                allowDualWielding = false;
                getHero().sendMessage(ChatColor.RED + "Du hast Beidhändigkeit verlernt.");
            }
        }
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onAttack(AttackTrigger trigger) {

        if (!trigger.getAttack().isOfAttackType(EffectType.DEFAULT_ATTACK)) {
            return;
        }
        getLevel().addExp(expPerAttack);
        getLevel().addExp((int) (expPerDamage * trigger.getAttack().getDamage()));
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onItemHeld(ItemHeldTrigger trigger) {

        checkTaskbar(trigger.getEvent().getNewSlot(), Weapon.Slot.MAIN_HAND);
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseTrigger trigger) {

        checkTaskbar();
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onItemPickup(ItemPickupTrigger trigger) {

        checkTaskbar();
    }

    private void checkTaskbar() {

        checkTaskbar(getHero().getPlayer().getInventory().getHeldItemSlot(), Weapon.Slot.MAIN_HAND);
    }

    private void checkTaskbar(int slot, Weapon.Slot weaponSlot) {

        // only check the slot he is currently holding
        PlayerInventory inventory = getHero().getPlayer().getInventory();
        ItemStack item = inventory.getItem(slot);
        if (item == null || item.getTypeId() == 0 || !ItemUtil.isWeapon(item.getType())) {
            // lets also remove the current weapon from the hero
            getHero().removeWeapon(Weapon.Slot.MAIN_HAND);
            getHero().removeWeapon(Weapon.Slot.OFF_HAND);
            return;
        }
        // required level < skill level
        if (allowedWeapons.containsKey(item.getTypeId()) && allowedWeapons.get(item.getTypeId()) < getLevel().getLevel()) {
            // lets add the item as a weapon if it is the current hold slot
            if (inventory.getHeldItemSlot() == slot) {
                // lets first add the main weapon
                getHero().setWeapon(new Weapon(slot, item, weaponSlot));
                // and then check for offhand weapons
                if (allowDualWielding && weaponSlot != Weapon.Slot.OFF_HAND && slot < 9) {
                    checkTaskbar(slot + 1, Weapon.Slot.OFF_HAND);
                }
            }
            return;
        }
        // all checks failed so we have to move the item
        getHero().removeWeapon(Weapon.Slot.MAIN_HAND);
        getHero().removeWeapon(Weapon.Slot.OFF_HAND);
        ItemUtil.moveItem(getHero(), slot, item);
        getHero().sendMessage(ChatColor.RED + "Du kannst diese " + ItemUtils.getFriendlyName(item.getTypeId()) + " nicht tragen. " +
                "Sie wurde in dein Inventar gelegt.");
    }
}
