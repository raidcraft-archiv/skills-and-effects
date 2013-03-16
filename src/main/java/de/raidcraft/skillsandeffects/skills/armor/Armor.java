package de.raidcraft.skillsandeffects.skills.armor;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.Attack;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.level.Levelable;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.items.ArmorType;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.trigger.InventoryClickTrigger;
import de.raidcraft.skills.trigger.InventoryCloseTrigger;
import de.raidcraft.skills.trigger.ItemHeldTrigger;
import de.raidcraft.skills.util.ItemUtil;
import de.raidcraft.skillsandeffects.effects.armor.SunderingArmor;
import de.raidcraft.util.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.EnumMap;
import java.util.Map;

/**
 * Each item can have a configured armor value which are just points. The factor these points
 * are are converted into damage recution can be defined here. The points need to be given each
 * item in the metadata. Defaults can be provided in the base skill config.
 * <p/>
 * This means that other items can have armor values too. Like a shield (iron-door, etc.).
 *
 * @author Silthus
 */
@SkillInformation(
        name = "Armor",
        desc = "Verringert erlittenen Schaden.",
        types = {EffectType.HELPFUL, EffectType.REDUCING}
)
public class Armor extends AbstractLevelableSkill implements Triggered {

    private static final int EXP_PER_DAMAGE_POINT_REDUCED = 2;
    private Map<Material, ArmorPiece> allowedArmor;
    private Map<ArmorType, ArmorPiece> playerArmor;

    public Armor(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        allowedArmor = new EnumMap<>(Material.class);
        playerArmor = new EnumMap<>(ArmorType.class);
        if (data.getConfigurationSection("items") == null) return;
        // lets load all items that the class can wear
        for (String key : data.getConfigurationSection("items").getKeys(false)) {
            Material item = ItemUtils.getItem(key);
            if (item != null) {
                ArmorType armor = ArmorType.fromMaterial(item);
                if (armor != null) {
                    if (!allowedArmor.containsKey(item)) {
                        int defArmorValue = data.getInt("defaults." + item.getId(), 0);
                        if (defArmorValue == 0) defArmorValue = data.getInt("defaults." + item.name(), 0);
                        ArmorPiece armorPiece = new ArmorPiece(item, armor, defArmorValue, data.getInt("items." + key, 1));
                        allowedArmor.put(item, armorPiece);
                        getHero().debug("loaded armor: " + armorPiece.getType().name() +
                                ":L" + armorPiece.getLevel() +
                                ":D" + armorPiece.getArmorValue());
                    }
                } else {
                    RaidCraft.LOGGER.warning("Item " + key + " in der Skill Config: " + getName() + " ist kein Rüstungs Item.");
                }
            } else {
                RaidCraft.LOGGER.warning("Item " + key + " in der Skill Config: " + getName() + " wurde nicht erkannt.");
            }
        }
    }

    @Override
    public void onLevelGain() {

        super.onLevelGain();
        for (ArmorPiece armor : allowedArmor.values()) {
            if (armor.getLevel() == getLevel().getLevel()) {
                getHero().sendMessage(
                        ChatColor.GREEN + "Neue Rüstung freigeschaltet: " +
                                ItemUtils.getFriendlyName(armor.getType(), ItemUtils.Language.GERMAN));
            }
        }
        checkArmor();
    }

    @Override
    public void onLevelLoss() {

        super.onLevelLoss();
        checkArmor();
    }

    @Override
    public void apply() {

        checkArmor();
    }

    @Override
    public void remove() {

        allowedArmor.clear();
        checkArmor();
    }

    @TriggerHandler
    public void onDamage(DamageTrigger trigger) {

        if (!trigger.getAttack().isOfAttackType(EffectType.PHYSICAL)
                || trigger.getAttack().isOfAttackType(EffectType.IGNORE_ARMOR)) {
            return;
        }

        int totalArmor = 0;
        for (ArmorPiece armor : playerArmor.values()) {
            // lets add up the defence points and reduce the damage
            totalArmor += armor.getArmorValue();
        }
        // lets reduce the total armor if the sundering armor effect is active
        if (trigger.getAttack().getTarget().hasEffect(SunderingArmor.class)) {
            totalArmor -= (totalArmor * trigger.getAttack().getTarget().getEffect(SunderingArmor.class).getArmorReduction());
        }
        // in percent
        double damageReduction = getDamageReduction(trigger.getAttack(), totalArmor);
        int newDamage = (int) (trigger.getAttack().getDamage() - (trigger.getAttack().getDamage() * damageReduction));
        int damageReduced = trigger.getAttack().getDamage() - newDamage;

        if (damageReduced > 0) {
            trigger.getAttack().setDamage(newDamage);

            getHero().debug("damage reduced by " + damageReduced);
            getHero().combatLog(this, "Schaden um " + (int)(damageReduction * 100) + "% (" + (damageReduced) + ") reduziert.");
            // now lets add some exp to the player to unlock more armor
            getLevel().addExp(damageReduced * EXP_PER_DAMAGE_POINT_REDUCED);
        }
    }

    @TriggerHandler
    public void onInventoryClose(InventoryCloseTrigger trigger) {

        checkArmor();
    }

    @TriggerHandler
    public void onInventoryClick(InventoryClickTrigger trigger) {

        checkArmor();
    }

    @TriggerHandler
    public void onItemHeldChange(ItemHeldTrigger trigger) {

        checkArmor();
    }

    /**
     * This reduction formula is based on the WoW Armor Reduction formula for characters up to level 59.
     * %Reduction = (Armor / ([85 * Enemy_Level] + Armor + 400)) * 100
     * The reduction is always capped at 75% so nobdy can receive 0 damage from armor reduction.
     * <p/>
     * To make things easier we calculate with a enemy level of 60 at all times.
     * BUT you can change this when spawning your creature (e.g. boss).
     * <p/>
     * Since we have about half the armor items (4 opposed to 8) the formula is halfed.
     *
     * @param armor that reduces the damage
     *
     * @return damage reduction in percent
     */
    private double getDamageReduction(Attack attack, int armor) {

        // default the level to 60
        int level = 60;
        if (attack.getSource() instanceof Levelable && !(attack.getSource() instanceof Hero)) {
            level = ((Levelable) attack.getSource()).getLevel().getLevel();
        }
        double reduction = armor / ((45.0 * level) + armor + 200.0);
        // cap reduction at 75%
        if (reduction > 0.75) reduction = 0.75;
        return reduction;
    }

    private boolean isAllowedItem(Material material) {

        return allowedArmor.containsKey(material) && allowedArmor.get(material).getLevel() <= getLevel().getLevel();
    }

    private boolean isAllowedItem(ItemStack itemStack) {

        return itemStack != null && isAllowedItem(itemStack.getType());
    }

    private void checkArmor() {

        Hero hero = getHero();
        PlayerInventory inventory = hero.getPlayer().getInventory();
        boolean movedItem = false;
        if (checkArmorItem(hero, ArmorType.HEAD, inventory.getHelmet())) {
            inventory.setHelmet(null);
            movedItem = true;
        }
        if (checkArmorItem(hero, ArmorType.CHEST, inventory.getChestplate())) {
            inventory.setChestplate(null);
            movedItem = true;
        }
        if (checkArmorItem(hero, ArmorType.LEGS, inventory.getLeggings())) {
            inventory.setLeggings(null);
            movedItem = true;
        }
        if (checkArmorItem(hero, ArmorType.FEET, inventory.getBoots())) {
            inventory.setBoots(null);
            movedItem = true;
        }
        // lets check if we need to add the held item as a shield
        ArmorType shield = ArmorType.fromMaterial(inventory.getItemInHand().getType());
        if (shield != null && allowedArmor.containsKey(inventory.getItemInHand().getType())) {
            ArmorPiece armor = allowedArmor.get(inventory.getItemInHand().getType());
            if (armor.getLevel() <= getLevel().getLevel()) {
                playerArmor.put(armor.getSlot(), armor);
            } else {
                playerArmor.remove(ArmorType.SHIELD);
            }
        } else {
            playerArmor.remove(ArmorType.SHIELD);
        }
        if (movedItem) {
            // inform the player
            hero.sendMessage(ChatColor.RED + "Du kannst diese Rüstung nicht tragen. Sie wurde in dein Inventar gelegt.");
        }
    }

    private boolean checkArmorItem(Hero hero, ArmorType slot, ItemStack item) {

        if (item == null || item.getTypeId() == 0) {
            playerArmor.remove(slot);
            return false;
        }
        if (!isAllowedItem(item)) {
            ItemUtil.moveItem(hero, -1, item);
            playerArmor.remove(slot);
            return true;
        } else {
            playerArmor.put(slot, allowedArmor.get(item.getType()));
        }
        return false;
    }

    /**
     * The armor piece just holds the information about what slot the item is in
     * and what armor value the item provides.
     */
    public static class ArmorPiece {

        private final Material type;
        private final ArmorType slot;
        private final int armorValue;
        private final int level;

        public ArmorPiece(Material type, ArmorType slot, int armorValue, int level) {

            this.type = type;
            this.slot = slot;
            this.armorValue = armorValue;
            this.level = level;
        }

        public Material getType() {

            return type;
        }

        public ArmorType getSlot() {

            return slot;
        }

        public int getArmorValue() {

            return armorValue;
        }

        public int getLevel() {

            return level;
        }

        @Override
        public boolean equals(Object obj) {

            return obj instanceof ArmorPiece
                    && ((ArmorPiece) obj).getType() == getType()
                    && ((ArmorPiece) obj).getSlot() == getSlot();
        }

        @Override
        public String toString() {

            return slot.name() + ":" + type.name();
        }
    }
}
