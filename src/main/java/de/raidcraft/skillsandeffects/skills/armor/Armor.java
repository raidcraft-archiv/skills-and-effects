package de.raidcraft.skillsandeffects.skills.armor;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.combat.AttackSource;
import de.raidcraft.skills.api.combat.AttackType;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.level.Levelable;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.DamageTrigger;
import de.raidcraft.skills.trigger.InventoryClickTrigger;
import de.raidcraft.skills.trigger.InventoryCloseTrigger;
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
        name = "Armor",
        desc = "Verringert erlittenen Schaden."
)
public class Armor extends AbstractLevelableSkill implements Triggered {

    private Map<Material, ArmorPiece> allowedArmor;
    private Map<ItemUtil.ArmorSlot, ArmorPiece> playerArmor;

    public Armor(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        allowedArmor = new EnumMap<>(Material.class);
        playerArmor = new EnumMap<>(ItemUtil.ArmorSlot.class);
        ConfigurationSection section;
        for (String key : data.getConfigurationSection("armor").getKeys(false)) {
            Material item = ItemUtils.getItem(key);
            if (item != null) {
                section = data.getConfigurationSection("armor." + key);
                ItemUtil.ArmorSlot slot = ItemUtil.ArmorSlot.fromMaterial(item);
                if (slot != null) {
                    if (!allowedArmor.containsKey(item)) {
                        allowedArmor.put(item, new ArmorPiece(item, slot, section));
                    }
                }
            } else {
                RaidCraft.LOGGER.warning("Item " + key + " in der Skill Config: " + getName() + " wurde nicht erkannt.");
            }
        }
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

        if (!trigger.getAttack().isOfAttackType(AttackType.PHYSICAL)) {
            return;
        }
        if (trigger.getAttack().hasSource(AttackSource.ENVIRONMENT)) {
            return;
        }
        int damageReduction = 0;
        for (ArmorPiece armor : playerArmor.values()) {
            // lets add up the defence points and reduce the damage
            damageReduction += armor.getTotalDamageReduction(this);
        }
        if (damageReduction > 0) {
            trigger.getAttack().setDamage(trigger.getAttack().getDamage() - damageReduction);
            getHero().debug("damage reduced by " + damageReduction);
            getHero().combatLog("Schaden durch Rüstung um " + damageReduction + " reduziert.");
        }
    }

    @TriggerHandler
    public void onInventoryClose(InventoryCloseTrigger trigger) {

        checkArmor();
    }

    public void onInventoryClickTrigger(InventoryClickTrigger trigger) {

        checkArmor();
    }

    private boolean isAllowedItem(Material material) {

        return allowedArmor.containsKey(material);
    }

    private boolean isAllowedItem(ItemStack itemStack) {

        return itemStack != null && isAllowedItem(itemStack.getType());
    }

    private void checkArmor() {

        Hero hero = getHero();
        PlayerInventory inventory = hero.getPlayer().getInventory();
        boolean movedItem = false;
        if (checkArmorItem(hero, ItemUtil.ArmorSlot.HEAD, inventory.getHelmet())) {
            inventory.setHelmet(null);
            movedItem = true;
        }
        if (checkArmorItem(hero, ItemUtil.ArmorSlot.CHEST, inventory.getChestplate())) {
            inventory.setChestplate(null);
            movedItem = true;
        }
        if (checkArmorItem(hero, ItemUtil.ArmorSlot.LEGS, inventory.getLeggings())) {
            inventory.setLeggings(null);
            movedItem = true;
        }
        if (checkArmorItem(hero, ItemUtil.ArmorSlot.FEET, inventory.getBoots())) {
            inventory.setBoots(null);
            movedItem = true;
        }
        if (movedItem) {
            // inform the player
            hero.sendMessage(ChatColor.RED + "Du kannst diese Rüstung nicht tragen. Sie wurde in dein Inventar gelegt.");
        }
    }

    private boolean checkArmorItem(Hero hero, ItemUtil.ArmorSlot slot, ItemStack item) {

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

    public static class ArmorPiece {

        private final Material type;
        private final ItemUtil.ArmorSlot slot;
        private final ConfigurationSection section;

        public ArmorPiece(Material type, ItemUtil.ArmorSlot slot, ConfigurationSection section) {

            this.type = type;
            this.slot = slot;
            this.section = section;
        }

        public Material getType() {

            return type;
        }

        public ItemUtil.ArmorSlot getSlot() {

            return slot;
        }

        public double getTotalDamageReduction(Skill skill) {

            return getDamageReduction()
                    + getReductionLevelModifier() * skill.getHero().getLevel().getLevel()
                    + getReductionProfLevelModifier() * skill.getProfession().getLevel().getLevel()
                    + (skill instanceof Levelable ? getReductionSkillLevelModifier() * ((Levelable) skill).getLevel().getLevel() : 0);
        }

        public double getDamageReduction() {

            return section.getDouble("reduction.base", 0.0);
        }

        public double getReductionLevelModifier() {

            return section.getDouble("reduction.level-modifier", 0.0);
        }

        public double getReductionProfLevelModifier() {

            return section.getDouble("reduction.prof-level-modifier", 0.0);
        }

        public double getReductionSkillLevelModifier() {

            return section.getDouble("reduction.skill-level-modifier", 0.0);
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
