package de.raidcraft.skillsandeffects.pve.skills;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.util.ItemUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Conjure",
        description = "Materialisiert ein Item aus dem Nichts."
)
public class ConjureItem extends AbstractLevelableSkill implements CommandTriggered {

    private final List<ConjuredItem> conjuredItems = new ArrayList<>();

    public ConjureItem(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        ConfigurationSection items = data.getConfigurationSection("items");
        if (items == null) return;
        for (String key : items.getKeys(false)) {
            Material item = ItemUtils.getItem(key);
            if (item == null) {
                RaidCraft.LOGGER.warning("Unknown item " + item + " in skill config of " + getName());
                continue;
            }
            short itemData = ItemUtils.getItemData(key);
            ConfigurationSection section = data.getConfigurationSection("items." + key);
            ConjuredItem conjuredItem = new ConjuredItem(item.getId(), itemData, section.getInt("amount", 1));
            conjuredItem.setChance(section.getConfigurationSection("chance"));
            conjuredItem.setExp(section.getInt("exp", 1));
            conjuredItem.setRequiredLevel(section.getInt("level", 1));
            conjuredItems.add(conjuredItem);
        }
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        for (ConjuredItem item : conjuredItems) {
            if (item.getRequiredLevel() > getAttachedLevel().getLevel()) {
                continue;
            }
            if (Math.random() < item.getChance(this)) {
                ItemStack itemStack = new ItemStack(item.getItemId(), item.getAmount(), item.getItemData());
                Location location = getHero().getEntity().getLocation();
                location.getWorld().dropItemNaturally(location, itemStack);
                getAttachedLevel().addExp(item.getExp());
            }
        }
    }

    public static class ConjuredItem {

        private final int itemId;
        private final int amount;
        private final short itemData;
        private ConfigurationSection chance;
        private int requiredLevel;
        private int exp;

        public ConjuredItem(int itemId, short itemData, int amount) {

            this.itemId = itemId;
            this.itemData = itemData;
            this.amount = amount;
        }

        public int getItemId() {

            return itemId;
        }

        public short getItemData() {

            return itemData;
        }

        public int getAmount() {

            return amount;
        }

        public int getRequiredLevel() {

            return requiredLevel;
        }

        public void setRequiredLevel(int requiredLevel) {

            this.requiredLevel = requiredLevel;
        }

        public int getExp() {

            return exp;
        }

        public void setExp(int exp) {

            this.exp = exp;
        }

        public double getChance(Skill skill) {

            return ConfigUtil.getTotalValue(skill, chance);
        }

        public void setChance(ConfigurationSection chance) {

            this.chance = chance;
        }
    }
}
