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
import de.raidcraft.skills.items.ToolType;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.BlockBreakTrigger;
import de.raidcraft.skills.trigger.CraftTrigger;
import de.raidcraft.skills.trigger.FurnaceExtractTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.util.CustomItemUtil;
import de.raidcraft.util.ItemUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.List;
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

    private final Set<Material> blocks = new HashSet<>();
    private final Set<Material> craftedItems = new HashSet<>();
    private ConfigurationSection chanceConfig;
    private ToolType toolType;
    private boolean furnace;

    public DoubleDrop(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        toolType = ToolType.fromName(data.getString("tool"));
        chanceConfig = data.getConfigurationSection("chance");
        furnace = data.getBoolean("furnace", false);
        blocks.addAll(getItemList(data.getStringList("blocks")));
        craftedItems.addAll(getItemList(data.getStringList("crafting")));
    }

    private Set<Material> getItemList(List<String> strings) {

        HashSet<Material> set = new HashSet<>();
        for (String key : strings) {
            Material item = ItemUtils.getItem(key);
            if (item != null) {
                blocks.add(item);
            } else {
                RaidCraft.LOGGER.warning("Unknown item in skill config of: " + getName() + ".yml");
            }
        }
        return set;
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onBlockBreak(BlockBreakTrigger trigger) {

        if (toolType != null && ToolType.fromMaterial(getHolder().getItemTypeInHand()) != toolType) {
            return;
        }
        if (RaidCraft.isPlayerPlacedBlock(trigger.getEvent().getBlock())) {
            return;
        }
        Block block = trigger.getEvent().getBlock();
        boolean doubleDrop = false;
        if (blocks.contains(block.getType())) {
            // lets check for a double drop
            if (Math.random() < getChance()) {
                // lets drop all normal drops again
                for (ItemStack item : block.getDrops(trigger.getEvent().getPlayer().getItemInHand())) {
                    block.getWorld().dropItemNaturally(block.getLocation(), item);
                }
                doubleDrop = true;
            }
            // lets also get the globally defined exp and add them to the skill
            int exp = RaidCraft.getComponent(SkillsPlugin.class).getExperienceConfig().getBlockExperienceFor(block.getType());
            exp += getUseExp();
            getAttachedLevel().addExp((doubleDrop ? exp * 2 : exp));
        }
    }

    private double getChance() {

        return ConfigUtil.getTotalValue(this, chanceConfig);
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onItemMelt(FurnaceExtractTrigger trigger) {

        if (furnace) {
            int amount = trigger.getEvent().getItemAmount();
            if (Math.random() < getChance()) {
                Location location = getHolder().getEntity().getLocation();
                location.getWorld().dropItemNaturally(location, new ItemStack(trigger.getEvent().getItemType(), amount));
                getAttachedLevel().addExp(getUseExp() * amount);
            }
            getAttachedLevel().addExp(getUseExp() * amount);
        }
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onItemCraft(CraftTrigger trigger) {

        ItemStack result = trigger.getEvent().getRecipe().getResult();
        if (CustomItemUtil.isCustomItem(result)) return;
        if (craftedItems.contains(result.getType()) && Math.random() < getChance()) {
            // clone the item and add the double drop
            trigger.getEvent().getWhoClicked().getInventory().addItem(new ItemStack(result));
            int exp = RaidCraft.getComponent(SkillsPlugin.class).getExperienceConfig().getCraftingExperienceFor(result.getType());
            getAttachedLevel().addExp(result.getAmount() * exp);
        }
    }
}
