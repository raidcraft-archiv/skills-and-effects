package de.raidcraft.skillsandeffects.pvp.skills.armor;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.items.ArmorPiece;
import de.raidcraft.skills.items.ArmorType;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.InventoryCloseTrigger;
import de.raidcraft.skills.util.ItemUtil;
import de.raidcraft.util.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
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
        description = "Erlaubt es spezielle Rüstungen zu tragen.",
        types = {EffectType.HELPFUL, EffectType.REDUCING}
)
public class ArmorSkill extends AbstractSkill implements Triggered {

    private final Map<Integer, Integer> allowedArmor = new HashMap<>();

    public ArmorSkill(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        allowedArmor.clear();
        if (data.getConfigurationSection("armor") == null) return;
        // lets load all items that the class can wear
        for (String key : data.getConfigurationSection("armor").getKeys(false)) {
            Material item = ItemUtils.getItem(key);
            if (item != null) {
                allowedArmor.put(item.getId(), data.getInt("armor." + key, 1));
            } else {
                RaidCraft.LOGGER.warning("Item " + key + " in der Skill Config: " + getName() + " wurde nicht erkannt.");
            }
        }
    }

    @Override
    public void apply() {

        super.apply();
        checkArmor();
    }

    @Override
    public void remove() {

        super.remove();
        allowedArmor.clear();
        checkArmor();
    }


    @TriggerHandler
    public void onInventoryClose(InventoryCloseTrigger trigger) {

        checkArmor();
        // now we need to set the armor of the player
        EntityEquipment equipment = trigger.getEvent().getPlayer().getEquipment();
        getHero().clearArmor();
        for (ItemStack itemStack : equipment.getArmorContents()) {
            if (itemStack == null || itemStack.getTypeId() == 0) {
                continue;
            }
            ArmorPiece armor = new ArmorPiece(itemStack);
            getHero().setArmor(armor);
        }
    }

    private void checkArmor() {

        Hero hero = getHero();
        boolean movedItem = false;
        EntityEquipment equipment = hero.getEntity().getEquipment();
        for (ItemStack item : equipment.getArmorContents()) {
            if (item == null || item.getTypeId() == 0) {
                continue;
            }
            boolean bool = checkItem(hero, item);
            movedItem = (movedItem || bool);
        }
        if (movedItem) {
            // inform the player
            hero.sendMessage(ChatColor.RED + "Du kannst diese Rüstung nicht tragen. Sie wurde in dein Inventar gelegt.");
        }
    }

    private boolean checkItem(Hero hero, ItemStack item) {

        EntityEquipment equipment = hero.getEntity().getEquipment();
        if (item != null && item.getTypeId() != 0 && !isAllowedItem(item.getTypeId())) {
            int slot = -1;
            switch (ArmorType.fromItemId(item.getTypeId())) {
                case HEAD:
                    equipment.setHelmet(null);
                    break;
                case CHEST:
                    equipment.setChestplate(null);
                    break;
                case LEGS:
                    equipment.setLeggings(null);
                    break;
                case FEET:
                    equipment.setBoots(null);
                    break;
            }
            ItemUtil.moveItem(hero, slot, item);
            return true;
        }
        return false;
    }

    private boolean isAllowedItem(int id) {

        return allowedArmor.containsKey(id) && allowedArmor.get(id) <= getProfession().getAttachedLevel().getLevel();
    }
}
