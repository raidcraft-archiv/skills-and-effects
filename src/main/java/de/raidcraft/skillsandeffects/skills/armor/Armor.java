package de.raidcraft.skillsandeffects.skills.armor;

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
import de.raidcraft.skills.trigger.InventoryCloseTrigger;
import de.raidcraft.skills.util.ItemUtil;
import de.raidcraft.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.PlayerInventory;

import java.util.*;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Armor",
        desc = "Verringert erlittenen Schaden."
)
public class Armor extends AbstractLevelableSkill implements Triggered {

    private final Map<ItemUtil.ArmorSlot, Set<ArmorPiece>> allowedArmor = new EnumMap<>(ItemUtil.ArmorSlot.class);

    public Armor(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        ConfigurationSection section;
        for (String key : data.getConfigurationSection("armor").getKeys(false)) {
            Material item = ItemUtils.getItem(key);
            if (item != null) {
                section = data.getConfigurationSection("armor." + key);
                ItemUtil.ArmorSlot slot = ItemUtil.ArmorSlot.fromMaterial(item);
                if (slot != null) {
                    if (!allowedArmor.containsKey(slot)) {
                        allowedArmor.put(slot, new HashSet<ArmorPiece>());
                    }
                    allowedArmor.get(slot).add(new ArmorPiece(item, slot, section));
                }
            }
        }
    }

    @Override
    public void apply() {
        //TODO: implement
    }

    @Override
    public void remove() {


    }

    @TriggerHandler
    public void onDamage(DamageTrigger trigger) {

        if (!trigger.getAttack().isOfAttackType(AttackType.PHYSICAL)) {
            return;
        }

    }

    @TriggerHandler
    public void onInventoryClose(InventoryCloseTrigger trigger) {

        checkArmor();
    }

    private boolean isAllowedItem(Material material) {

        Collection<Set<ArmorPiece>> values = allowedArmor.values();
        for (Set<ArmorPiece> entry : values) {
            for (ArmorPiece piece : entry) {
                if (piece.getType() == material) {
                    return true;
                }
            }
        }
        return false;
    }

    private void checkArmor() {

        Hero hero = getHero();
        PlayerInventory inventory = hero.getPlayer().getInventory();
        if (inventory.getHelmet() != null && inventory.getHelmet().getTypeId() != 0) {
            if (!isAllowedItem(inventory.getHelmet().getType())) {
                ItemUtil.moveItem(hero, -1, inventory.getHelmet());
                inventory.setHelmet(null);
            }
        }
        if (inventory.getChestplate() != null && inventory.getChestplate().getTypeId() != 0) {
            if (!isAllowedItem(inventory.getChestplate().getType())) {
                ItemUtil.moveItem(hero, -1, inventory.getChestplate());
                inventory.setChestplate(null);
            }
        }
        if (inventory.getLeggings() != null && inventory.getLeggings().getTypeId() != 0) {
            if (!isAllowedItem(inventory.getLeggings().getType())) {
                ItemUtil.moveItem(hero, -1, inventory.getLeggings());
                inventory.setLeggings(null);
            }
        }
        if (inventory.getBoots() != null && inventory.getBoots().getTypeId() != 0) {
            if (!isAllowedItem(inventory.getBoots().getType())) {
                ItemUtil.moveItem(hero, -1, inventory.getLeggings());
                inventory.setBoots(null);
            }
        }
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
    }
}
