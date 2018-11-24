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
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
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
    private int minItems = 0;
    private int maxItems = 1;

    public ConjureItem(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        minItems = data.getInt("min-items", 0);
        maxItems = data.getInt("max-items", 1);
        ConfigurationSection items = data.getConfigurationSection("items");
        if (items == null) return;
        for (String key : items.getKeys(false)) {
            Material item = ItemUtils.getItem(key);
            if (item == null) {
                RaidCraft.LOGGER.warning("Unknown item " + item + " in skill config of " + getName());
                continue;
            }
            ConfigurationSection section = data.getConfigurationSection("items." + key);
            ConjuredItem conjuredItem = new ConjuredItem(item, section.getInt("amount", 1));
            conjuredItem.setChance(section.getConfigurationSection("chance"));
            conjuredItem.setExp(section.getInt("exp", 1));
            conjuredItem.setRequiredLevel(section.getInt("level", 1));
            conjuredItems.add(conjuredItem);
        }
        // randomize it even more
        Collections.shuffle(conjuredItems);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        int conjuredItemsCount = 0;
        do {
            for (ConjuredItem item : conjuredItems) {
                if (item.getRequiredLevel() > getAttachedLevel().getLevel()) {
                    continue;
                }
                if (Math.random() < item.getChance(this)) {
                    ItemStack itemStack = new ItemStack(item.getMaterial(), item.getAmount());
                    Location location = getHolder().getEntity().getLocation();
                    location.getWorld().dropItemNaturally(location, itemStack);
                    getAttachedLevel().addExp(item.getExp());
                    conjuredItemsCount++;
                }
                if (conjuredItemsCount >= maxItems) {
                    break;
                }
            }
        } while (conjuredItemsCount < minItems);

    }

    @Data
    public static class ConjuredItem {

        private final Material material;
        private final int amount;
        private ConfigurationSection chance;
        private int requiredLevel;
        private int exp;

        public ConjuredItem(Material material, int amount) {

            this.material = material;
            this.amount = amount;
        }

        public double getChance(Skill skill) {

            return ConfigUtil.getTotalValue(skill, chance);
        }
    }
}
