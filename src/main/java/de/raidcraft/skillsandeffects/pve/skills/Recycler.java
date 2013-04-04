package de.raidcraft.skillsandeffects.pve.skills;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.PlayerItemBreakTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Recycler",
        description = "Es besteht die Chance Materialien aus kaputten Items zu gewinnen."
)
public class Recycler extends AbstractSkill implements Triggered {

    private final Map<Integer, SalvagedItem> salvagedItems = new HashMap<>();

    public Recycler(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        ConfigurationSection items = data.getConfigurationSection("items");
        if (items == null) return;
        for (String key : items.getKeys(false)) {
            Material item = ItemUtils.getItem(key);
            if (item == null) {
                RaidCraft.LOGGER.warning("Unknown item " + key + " in skill config " + getName());
                continue;
            }
            ConfigurationSection section = data.getConfigurationSection("items." + key);
            Material material = ItemUtils.getItem(section.getString("item"));
            if (material == null) {
                RaidCraft.LOGGER.warning("Unknown item " + key + " in skill config " + getName());
                continue;
            }
            SalvagedItem salvagedItem = new SalvagedItem(material.getId(), section.getInt("amount", 1));
            salvagedItem.setChance(section.getConfigurationSection("chance"));
            salvagedItems.put(item.getId(), salvagedItem);
        }
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onItemBreak(PlayerItemBreakTrigger trigger) {

        if (!salvagedItems.containsKey(trigger.getEvent().getBrokenItem().getTypeId())) {
            return;
        }
        SalvagedItem salvagedItem = salvagedItems.get(trigger.getEvent().getBrokenItem().getTypeId());
        if (Math.random() < salvagedItem.getChance(this)) {
            ItemStack itemStack = new ItemStack(salvagedItem.getItemId(), salvagedItem.getAmount());
            Player player = trigger.getEvent().getPlayer();
            player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
        }
    }

    public static class SalvagedItem {

        private final int itemId;
        private final int amount;
        private ConfigurationSection chance;

        public SalvagedItem(int itemId, int amount) {

            this.itemId = itemId;
            this.amount = amount;
        }

        public int getItemId() {

            return itemId;
        }

        public int getAmount() {

            return amount;
        }

        public void setChance(ConfigurationSection chance) {

            this.chance = chance;
        }

        public double getChance(Skill skill) {

            return ConfigUtil.getTotalValue(skill, chance);
        }
    }
}
