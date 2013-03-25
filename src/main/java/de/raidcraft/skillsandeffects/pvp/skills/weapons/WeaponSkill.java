package de.raidcraft.skillsandeffects.pvp.skills.weapons;

import de.raidcraft.RaidCraft;
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

import java.util.HashMap;
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
    private double expPerDamage;
    private int expPerAttack;

    public WeaponSkill(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

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

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onAttack(AttackTrigger trigger) {

        if (!trigger.getAttack().isOfAttackType(EffectType.DEFAULT_ATTACK)) {
            return;
        }
        getLevel().addExp(expPerAttack);
        getLevel().addExp((int) (expPerDamage * trigger.getAttack().getDamage()));
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
    }

    @TriggerHandler(ignoreCancelled = true)
    public void onItemHeld(ItemHeldTrigger trigger) {

        checkTaskbar(trigger.getEvent().getNewSlot());
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

        // the taskbar has 9 slots so we check all of them
        for (int i = 0; i < 9; i++) {
            checkTaskbar(i);
        }
    }

    private void checkTaskbar(int slot) {

        // only check the slot he is currently holding
        ItemStack item = getHero().getPlayer().getInventory().getItem(slot);
        if (item == null || item.getTypeId() == 0 || !ItemUtil.isWeapon(item.getType())) {
            // lets also remove the current weapon from the hero
            getHero().removeWeapon(Weapon.Slot.MAIN_HAND);
            return;
        }
        // required level < skill level
        if (allowedWeapons.containsKey(item.getTypeId()) && allowedWeapons.get(item.getTypeId()) < getLevel().getLevel()) {
            // lets add the item as a weapon if it is the current hold slot
            if (getHero().getPlayer().getInventory().getHeldItemSlot() == slot) {
                // in this skill we only add mainhand weapons
                getHero().setWeapon(new Weapon(slot, item, Weapon.Slot.MAIN_HAND));
            }
            return;
        }
        // all checks failed so we have to move the item
        getHero().removeWeapon(Weapon.Slot.MAIN_HAND);
        ItemUtil.moveItem(getHero(), slot, item);
        getHero().sendMessage(ChatColor.RED + "Du kannst diese " + ItemUtils.getFriendlyName(item.getTypeId()) + " nicht tragen. " +
                "Sie wurde in dein Inventar gelegt.");
    }
}
