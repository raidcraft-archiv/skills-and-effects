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
import de.raidcraft.skills.items.ToolType;
import de.raidcraft.skills.requirement.SkillRequirementResolver;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.BlockBreakTrigger;
import de.raidcraft.skills.trigger.CraftTrigger;
import de.raidcraft.skills.trigger.PlayerFishTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.util.ItemUtils;
import de.raidcraft.util.MathUtil;
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
import java.util.Set;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Special Drop",
        description = "Ermöglicht es spezielle Items in abgebauten Blöcken zu finden."
)
public class SpecialDrop extends AbstractSkill implements Triggered {

    private final Map<Integer, List<Drop>> specialBlockDrops = new HashMap<>();
    private final Map<Integer, List<Drop>> specialFishingDrops = new HashMap<>();
    private final Map<Integer, List<Drop>> specialCraftingDrops = new HashMap<>();
    private final Map<Integer, ToolType> requiredTools = new HashMap<>();

    public SpecialDrop(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        if (data.getConfigurationSection("blocks") != null) specialBlockDrops.putAll(parseConfig(data.getConfigurationSection("blocks")));
        if (data.getConfigurationSection("fishing") != null) specialFishingDrops.putAll(parseConfig(data.getConfigurationSection("fishing")));
        if (data.getConfigurationSection("crafting") != null) specialCraftingDrops.putAll(parseConfig(data.getConfigurationSection("crafting")));
    }

    private Map<Integer, List<Drop>> parseConfig(ConfigurationSection config) {

        Map<Integer, List<Drop>> map = new HashMap<>();
        if (config == null) return map;
        Set<String> keys = config.getKeys(false);
        if (keys == null) return map;
        for (String key : keys) {
            // each key is an item that has registered drops
            Material item = ItemUtils.getItem(key);
            if (item != null) {
                // also add the required tool to trigger drops
                String toolName = config.getString(key + ".tool", "none");
                if (!toolName.equalsIgnoreCase("none")) {
                    ToolType toolType = ToolType.fromName(toolName);
                    if (toolType != null) {
                        requiredTools.put(item.getId(), toolType);
                    } else {
                        RaidCraft.LOGGER.warning("Wrong tool configured in custom config " + config.getName() + " section - " + key);
                    }
                }
                Set<String> drops = config.getConfigurationSection(key + ".drops").getKeys(false);
                for (String dropKey : drops) {
                    Material droppedConfigItem = ItemUtils.getItem(dropKey);
                    if (droppedConfigItem != null) {
                        ConfigurationSection section = config.getConfigurationSection(key + ".drops." + dropKey);
                        Drop drop = new Drop(getHero(), droppedConfigItem.getId());
                        drop.setData((byte) ItemUtils.getItemData(dropKey));
                        drop.setRequirements(RequirementManager.createRequirements(drop, section.getConfigurationSection("requirements")));
                        drop.setMinAmount(section.getConfigurationSection("min-amount"));
                        drop.setMinAmount(section.getConfigurationSection("max-amount"));
                        drop.setChance(section.getConfigurationSection("chance"));
                        drop.setExp(section.getInt("exp", 0));
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
        if (!specialBlockDrops.containsKey(blockId) || RaidCraft.isPlayerPlacedBlock(block)) {
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
        for (ItemStack itemStack : droppedItems) {
            location.getWorld().dropItemNaturally(location, itemStack);
        }
    }

    public class Drop implements SkillRequirementResolver {

        private final Hero hero;
        private final int itemId;
        private byte data;
        private int exp;
        private ConfigurationSection minAmount;
        private ConfigurationSection maxAmount;
        private List<Requirement> requirements = new ArrayList<>();
        private ConfigurationSection chance;

        public Drop(Hero hero, int itemId) {

            this.hero = hero;
            this.itemId = itemId;
        }

        public int getItemId() {

            return itemId;
        }

        public int getMinAmount() {

            return (int) ConfigUtil.getTotalValue(SpecialDrop.this, minAmount);
        }

        public void setMinAmount(ConfigurationSection minAmount) {

            this.minAmount = minAmount;
        }

        public int getMaxAmount() {

            return (int) ConfigUtil.getTotalValue(SpecialDrop.this, maxAmount);
        }

        public void setMaxAmount(ConfigurationSection maxAmount) {

            this.maxAmount = maxAmount;
        }

        public int getExp() {

            return exp;
        }

        public void setExp(int exp) {

            this.exp = exp;
        }

        public int getAmount() {

            int max = getMaxAmount();
            int min = getMinAmount();
            return MathUtil.RANDOM.nextInt(max - min + 1) + min;
        }

        public byte getData() {

            return data;
        }

        public void setData(byte data) {

            this.data = data;
        }

        public void setRequirements(List<Requirement> requirements) {

            this.requirements.clear();
            this.requirements.addAll(requirements);
        }

        public void setChance(ConfigurationSection chance) {

            this.chance = chance;
        }

        public double getChance(Skill skill) {

            return ConfigUtil.getTotalValue(skill, chance);
        }

        public ItemStack getDrops(Skill skill) {

            for (Requirement requirement : getRequirements()) {
                if (!requirement.isMet()) {
                    return null;
                }
            }

            if (MathUtil.RANDOM.nextDouble() < getChance(skill)) {
                return new ItemStack(getItemId(), getAmount(), getData());
            }
            return null;
        }

        @Override
        public Hero getHero() {

            return hero;
        }

        @Override
        public List<Requirement> getRequirements() {

            return requirements;
        }

        @Override
        public boolean isMeetingAllRequirements() {

            for (Requirement requirement : requirements) {
                if (!requirement.isMet()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public String getResolveReason() {

            for (Requirement requirement : requirements) {
                if (!requirement.isMet()) {
                    return requirement.getLongReason();
                }
            }
            return "Alle Vorraussetzungen sind erfüllt.";
        }
    }
}
