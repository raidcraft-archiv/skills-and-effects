package de.raidcraft.skillsandeffects.pve.skills.tools;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.requirement.Requirement;
import de.raidcraft.api.requirement.RequirementManager;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.events.RCCombatEvent;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.items.Weapon;
import de.raidcraft.skills.items.WeaponType;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.trigger.CombatTrigger;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "tools",
        description = "Ermöglicht es mit der Zeit bestimmte Werkzeuge zu tragen.",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING, EffectType.DEFAULT_ATTACK}
)
public class ToolSkill extends AbstractLevelableSkill implements Triggered {

    private final Map<Integer, Integer> allowedWeapons = new HashMap<>();
    private final Set<WeaponType> ignoredWeapons = new HashSet<>();
    private List<Requirement> dualWieldingRequirements;
    private boolean allowDualWielding = false;
    private double expPerDamage;

    public ToolSkill(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        dualWieldingRequirements = RequirementManager.createRequirements(this, data.getConfigurationSection("dual-wielding"));
        expPerDamage = data.getDouble("exp-per-damage", 0.0);

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

        // also load all ignored weapon types
        for (String key : data.getStringList("ignored-weapons")) {
            WeaponType weaponType = WeaponType.fromString(key);
            if (weaponType != null) {
                ignoredWeapons.add(weaponType);
            } else {
                RaidCraft.LOGGER.warning("Unknown weapon type " + key + " in " + getName() + ".yml");
            }
        }
    }

    @Override
    public String getDescription() {

        if (allowedWeapons.size() < 1) {
            return super.getDescription();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.GRAY).append("Ermöglicht es dir folgende Waffen ab einem bestimmten ").append(getFriendlyName()).append(" Level zu tragen:");
        for (Map.Entry<Integer, Integer> entry : allowedWeapons.entrySet()) {
            sb.append(ChatColor.YELLOW).append("\n\t- ").append((entry.getValue() <= getAttachedLevel().getLevel()) ? ChatColor.GREEN : ChatColor.RED);
            sb.append(ItemUtils.getFriendlyName(entry.getKey())).append(ChatColor.YELLOW).append(": Level ");
            sb.append(ChatColor.AQUA).append(entry.getValue());
        }
        return sb.toString();
    }

    @Override
    public void onLevelGain() {

        super.onLevelGain();
        for (Map.Entry<Integer, Integer> entry : allowedWeapons.entrySet()) {

            if (entry.getValue() == getAttachedLevel().getLevel()) {
                getHero().sendMessage(ChatColor.GREEN + "Neue Waffe freigeschaltet: " +
                        ItemUtils.getFriendlyName(entry.getKey(), ItemUtils.Language.GERMAN));
            }
        }
        if (!allowDualWielding && dualWieldingRequirements.size() > 1) {
            boolean isMet = true;
            for (Requirement requirement : dualWieldingRequirements) {
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
            for (Requirement requirement : dualWieldingRequirements) {
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
        Weapon weapon = trigger.getSource().getWeapon(Weapon.Slot.MAIN_HAND);
        if (weapon == null || !allowedWeapons.containsKey(weapon.getItemId())) {
            return;
        }
        getAttachedLevel().addExp((int) (expPerDamage * trigger.getAttack().getDamage()));
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

    @TriggerHandler(ignoreCancelled = true)
    public void onCombat(CombatTrigger trigger) {

        if (trigger.getEvent().getType() != RCCombatEvent.Type.ENTER) {
            return;
        }
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
        // dont handle weapons that are ignored and may be handled by other skills
        if (ignoredWeapons.contains(WeaponType.fromItemId(item.getTypeId()))) {
            return;
        }
        // required level < skill level
        if (allowedWeapons.containsKey(item.getTypeId()) && allowedWeapons.get(item.getTypeId()) <= getAttachedLevel().getLevel()) {
            // lets first add the main weapon
            getHero().setWeapon(new Weapon(slot, item, weaponSlot));
            // and then check for offhand weapons
            if (allowDualWielding && weaponSlot != Weapon.Slot.OFF_HAND && slot < 9) {
                checkTaskbar(slot + 1, Weapon.Slot.OFF_HAND);
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
