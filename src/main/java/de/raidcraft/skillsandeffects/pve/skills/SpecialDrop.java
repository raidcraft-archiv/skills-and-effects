package de.raidcraft.skillsandeffects.pve.skills;

import com.sk89q.worldedit.blocks.BlockID;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.requirement.Requirement;
import de.raidcraft.api.requirement.RequirementManager;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.Skill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.config.CustomConfig;
import de.raidcraft.skills.items.ToolType;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.BlockBreakTrigger;
import de.raidcraft.skills.trigger.CraftTrigger;
import de.raidcraft.skills.trigger.PlayerFishTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.util.ItemUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Special Drop",
        description = "Ermöglicht es spezielle Items in abgebauten Blöcken zu finden."
)
public class SpecialDrop extends AbstractSkill implements Triggered {

    private static final Map<Integer, List<Drop>> specialBlockDrops = new HashMap<>();
    private static final Map<Integer, List<Drop>> specialFishingDrops = new HashMap<>();
    private static final Map<Integer, List<Drop>> specialCraftingDrops = new HashMap<>();
    private static final Map<Integer, ToolType> requiredTools = new HashMap<>();

    public SpecialDrop(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        CustomConfig config = CustomConfig.getConfig("special-drops");
        if (specialBlockDrops.isEmpty()) specialBlockDrops.putAll(parseConfig(config.getConfigurationSection("blocks")));
        if (specialFishingDrops.isEmpty()) specialFishingDrops.putAll(parseConfig(config.getConfigurationSection("fishing")));
        if (specialCraftingDrops.isEmpty()) specialCraftingDrops.putAll(parseConfig(config.getConfigurationSection("crafting")));
    }

    private Map<Integer, List<Drop>> parseConfig(ConfigurationSection config) {

        Map<Integer, List<Drop>> map = new HashMap<>();
        for (String key : config.getKeys(false)) {
            // each key is an item that has registered drops
            Material item = ItemUtils.getItem(key);
            if (item != null) {
                // also add the required tool to trigger drops
                String toolName = config.getString(key + ".tool", "none");
                if (!toolName.equalsIgnoreCase("none")) {
                    Material material = ItemUtils.getItem(toolName);
                    if (material != null) {
                        requiredTools.put(item.getId(), ToolType.fromItemId(material.getId()));
                    } else {
                        RaidCraft.LOGGER.warning("Wrong tool configured in custom config " + config.getName() + " section - " + key);
                    }
                }
                Set<String> drops = config.getConfigurationSection(key + ".drops").getKeys(false);
                for (String dropKey : drops) {
                    Material droppedConfigItem = ItemUtils.getItem(dropKey);
                    if (droppedConfigItem != null) {
                        ConfigurationSection section = config.getConfigurationSection(key + ".drops." + dropKey);
                        Drop drop = new Drop(droppedConfigItem.getId());
                        drop.setData((byte) ItemUtils.getItemData(dropKey));
                        drop.setRequirements(RequirementManager.createRequirements(drop, section.getConfigurationSection("requirements")));
                        drop.setMinAmount(section.getInt("min-amount", 1));
                        drop.setMinAmount(section.getInt("max-amount", 1));
                        drop.setChance(section.getConfigurationSection("chance"));
                        if (!map.containsKey(item.getId())) {
                            map.put(item.getId(), new ArrayList<Drop>());
                        }
                        // finally add the created dropping item to our list
                        map.get(item.getId()).add(drop);
                    } else {
                        RaidCraft.LOGGER.warning("Wrong item configured in custom config " + config.getName());
                    }
                }
            } else {
                RaidCraft.LOGGER.warning("Wrong item configured in custom config " + config.getName());
            }
        }
        return map;
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onBlockBreak(BlockBreakTrigger trigger) {

        Block block = trigger.getEvent().getBlock();
        int blockId = block.getTypeId();
        if (requiredTools.containsKey(blockId) && !requiredTools.get(blockId).isOfType(getHero().getItemTypeInHand())) {
            return;
        }
        if (!specialBlockDrops.containsKey(blockId)) {
            return;
        }
        dropItems(specialBlockDrops.get(blockId), block.getLocation());
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onPlayerFishTrigger(PlayerFishTrigger trigger) {

        if (!specialFishingDrops.containsKey(BlockID.WATER)) {
            return;
        }
        if (trigger.getEvent().getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        dropItems(specialFishingDrops.get(BlockID.WATER), trigger.getEvent().getPlayer().getLocation());
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onPlayerCraft(CraftTrigger trigger) {

        int itemId = trigger.getEvent().getRecipe().getResult().getTypeId();
        if (!specialCraftingDrops.containsKey(itemId)) {
            return;
        }
        dropItems(specialCraftingDrops.get(itemId), trigger.getEvent().getWhoClicked().getLocation());
    }

    private void dropItems(List<Drop> drops, Location location) {

        List<ItemStack> droppedItems = new ArrayList<>();
        for (Drop drop : drops) {
            ItemStack stack = drop.getDrops(this);
            if (stack != null) {
                droppedItems.add(stack);
            }
        }
        // TODO: maybe only drop a maximum of possible drops
        for (ItemStack itemStack : droppedItems) {
            location.getWorld().dropItemNaturally(location, itemStack);
        }
    }

    public static class Drop {

        private static final Random RANDOM = new Random();

        private final int itemId;
        private byte data;
        private int minAmount;
        private int maxAmount;
        private List<Requirement<Drop>> requirements;
        private ConfigurationSection chance;

        public Drop(int itemId) {

            this.itemId = itemId;
        }

        public int getItemId() {

            return itemId;
        }

        public int getMinAmount() {

            return minAmount;
        }

        public void setMinAmount(int minAmount) {

            this.minAmount = minAmount;
        }

        public int getMaxAmount() {

            return maxAmount;
        }

        public void setMaxAmount(int maxAmount) {

            this.maxAmount = maxAmount;
        }

        public int getAmount() {

            return RANDOM.nextInt(maxAmount - minAmount + 1) + minAmount;
        }

        public byte getData() {

            return data;
        }

        public void setData(byte data) {

            this.data = data;
        }

        public List<Requirement<Drop>> getRequirements() {

            return requirements;
        }

        public void setRequirements(List<Requirement<Drop>> requirements) {

            this.requirements = requirements;
        }

        public void setChance(ConfigurationSection chance) {

            this.chance = chance;
        }

        public double getChance(Skill skill) {

            return ConfigUtil.getTotalValue(skill, chance);
        }

        public ItemStack getDrops(Skill skill) {

            for (Requirement<Drop> requirement : getRequirements()) {
                if (!requirement.isMet()) {
                    return null;
                }
            }

            if (RANDOM.nextDouble() < getChance(skill)) {
                return new ItemStack(getItemId(), getAmount(), getData());
            }
            return null;
        }
    }
}
