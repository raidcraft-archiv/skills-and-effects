package de.raidcraft.skillsandeffects.pve.skills;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.BlockBreakTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Double Drop",
        description = "Ermöglicht es von abgebauten Blöcken Doppel Drops zu bekommen.",
        triggerCombat = false
)
public class DoubleDrop extends AbstractLevelableSkill implements Triggered {

    private final Set<Integer> blockIds = new HashSet<>();
    private ConfigurationSection chanceConfig;
    private double dropChance;

    public DoubleDrop(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        chanceConfig = data.getConfigurationSection("chance");
        for (String key : data.getStringList("blocks")) {
            Material item = ItemUtils.getItem(key);
            if (item != null) {
                blockIds.add(item.getId());
            } else {
                RaidCraft.LOGGER.warning("Unknown item in skill config of: " + getName() + ".yml");
            }
        }
    }

    private double getChance() {

        if (dropChance == 0.0) {
            dropChance = ConfigUtil.getTotalValue(this, chanceConfig);
        }
        return dropChance;
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onBlockBreak(BlockBreakTrigger trigger) {

        Block block = trigger.getEvent().getBlock();
        boolean doubleDrop = false;
        if (blockIds.contains(block.getTypeId())) {
            // lets check for a double drop
            if (Math.random() < getChance()) {
                // lets drop all normal drops again
                for (ItemStack item : block.getDrops(trigger.getEvent().getPlayer().getItemInHand())) {
                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                }
                doubleDrop = true;
            }
            // lets also get the globally defined exp and add them to the skill
            int exp = RaidCraft.getComponent(SkillsPlugin.class).getExperienceConfig().getBlockExperienceFor(block.getTypeId());
            getLevel().addExp((doubleDrop ? exp * 2 : exp));
        }
    }
}
