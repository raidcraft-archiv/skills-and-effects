package de.raidcraft.skillsandeffects.pve.skills;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.action.requirement.Requirement;
import de.raidcraft.api.action.requirement.RequirementException;
import de.raidcraft.api.action.RequirementFactory;
import de.raidcraft.api.action.requirement.RequirementResolver;
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
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.EnumMap;
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

    private final Map<Material, List<Drop>> specialBlockDrops = new EnumMap<>(Material.class);
    private final Map<Material, List<Drop>> specialFishingDrops = new EnumMap<>(Material.class);
    private final Map<Material, List<Drop>> specialCraftingDrops = new EnumMap<>(Material.class);
    private final Map<Material, ToolType> requiredTools = new EnumMap<>(Material.class);

    public SpecialDrop(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        if (data.getConfigurationSection("blocks") != null) specialBlockDrops.putAll(parseConfig(data.getConfigurationSection("blocks")));
        if (data.getConfigurationSection("fishing") != null) {
            specialFishingDrops.putAll(parseConfig(data.getConfigurationSection("fishing")));
        }
        if (data.getConfigurationSection("crafting") != null) {
            specialCraftingDrops.putAll(parseConfig(data.getConfigurationSection("crafting")));
        }
    }

    private Map<Material, List<Drop>> parseConfig(ConfigurationSection config) {

        Map<Material, List<Drop>> map = new HashMap<>();
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
                        requiredTools.put(item, toolType);
                    } else {
                        RaidCraft.LOGGER.warning("Wrong tool configured in custom config " + config.getName() + " section - " + key);
                    }
                }
                Set<String> drops = config.getConfigurationSection(key + ".drops").getKeys(false);
                for (String dropKey : drops) {
                    Material droppedConfigItem = ItemUtils.getItem(dropKey);
                    if (droppedConfigItem != null) {
                        ConfigurationSection section = config.getConfigurationSection(key + ".drops." + dropKey);
                        Drop drop = new Drop(getHolder(), droppedConfigItem);
                        drop.setData((byte) ItemUtils.getItemData(dropKey));
                        drop.setRequirements(config.getConfigurationSection("requirements"));
                        drop.setMinAmount(section.getConfigurationSection("min-amount"));
                        drop.setMinAmount(section.getConfigurationSection("max-amount"));
                        drop.setChance(section.getConfigurationSection("chance"));
                        drop.setExp(section.getInt("exp", 0));
                        if (!map.containsKey(item)) {
                            map.put(item, new ArrayList<Drop>());
                        }
                        // finally add the created dropping item to our list
                        map.get(item).add(drop);
                    } else {
                        RaidCraft.LOGGER.warning("Wrong item configured in custom config " + config.getName() + "." + item + "." + dropKey);
                    }
                }
            } else {
                RaidCraft.LOGGER.warning("Wrong item type configured in custom config " + config.getName() + "." + key);
            }
        }
        return map;
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onBlockBreak(BlockBreakTrigger trigger) {

        Block block = trigger.getEvent().getBlock();
        Material blockType = block.getType();
        if (requiredTools.containsKey(blockType) && !requiredTools.get(blockType).isOfType(getHolder().getItemTypeInHand())) {
            return;
        }
        if (!specialBlockDrops.containsKey(blockType) || RaidCraft.isPlayerPlacedBlock(block)) {
            return;
        }
        dropItems(specialBlockDrops.get(blockType), block.getLocation());
    }

    private void dropItems(List<Drop> drops, Location location) {

        List<ItemStack> droppedItems = new ArrayList<>();
        for (Drop drop : drops) {
            if (drop == null) continue;
            ItemStack stack = drop.getDrops(this);
            if (stack != null) {
                droppedItems.add(stack);
            }
        }
        for (ItemStack itemStack : droppedItems) {
            location.getWorld().dropItemNaturally(location, itemStack);
        }
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onPlayerFishTrigger(PlayerFishTrigger trigger) {

        if (!specialFishingDrops.containsKey(Material.WATER)) {
            return;
        }
        if (trigger.getEvent().getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            return;
        }
        dropItems(specialFishingDrops.get(Material.WATER), trigger.getEvent().getPlayer().getLocation());
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onPlayerCraft(CraftTrigger trigger) {

        Material itemType = trigger.getEvent().getRecipe().getResult().getType();
        if (!specialCraftingDrops.containsKey(itemType)) {
            return;
        }
        dropItems(specialCraftingDrops.get(itemType), trigger.getEvent().getWhoClicked().getLocation());
    }

    public class Drop implements RequirementResolver<Player> {

        private final Hero hero;
        private final Material material;
        private byte data;
        private int exp;
        private ConfigurationSection minAmount;
        private ConfigurationSection maxAmount;
        private List<Requirement<Player>> requirements = new ArrayList<>();
        private ConfigurationSection chance;

        public Drop(Hero hero, Material material) {

            this.hero = hero;
            this.material = material;
        }

        public int getExp() {

            return exp;
        }

        public void setExp(int exp) {

            this.exp = exp;
        }

        public void setChance(ConfigurationSection chance) {

            this.chance = chance;
        }

        public ItemStack getDrops(Skill skill) {

            for (Requirement<Player> requirement : getRequirements()) {
                if (!requirement.test(skill.getHolder().getPlayer())) {
                    return null;
                }
            }

            if (MathUtil.RANDOM.nextDouble() < getChance(skill)) {
                return new ItemStack(getMaterial(), getAmount(), getData());
            }
            return null;
        }

        public double getChance(Skill skill) {

            return ConfigUtil.getTotalValue(skill, chance);
        }

        public Material getMaterial() {

            return material;
        }

        public int getAmount() {

            int max = getMaxAmount();
            int min = getMinAmount();
            int n = max - min + 1;
            if (n < 1) {
                return 0;
            }
            return MathUtil.RANDOM.nextInt(n) + min;
        }

        public byte getData() {

            return data;
        }

        public int getMaxAmount() {

            return (int) ConfigUtil.getTotalValue(SpecialDrop.this, maxAmount);
        }

        public int getMinAmount() {

            return (int) ConfigUtil.getTotalValue(SpecialDrop.this, minAmount);
        }

        public void setMinAmount(ConfigurationSection minAmount) {

            this.minAmount = minAmount;
        }

        public void setMaxAmount(ConfigurationSection maxAmount) {

            this.maxAmount = maxAmount;
        }

        public void setData(byte data) {

            this.data = data;
        }

        @Override
        public List<Requirement<Player>> getRequirements() {

            return requirements;
        }

        public void setRequirements(ConfigurationSection config) {

            try {
                this.requirements.clear();
                this.requirements.addAll(RequirementFactory.getInstance().createRequirements(getName(), config, Player.class));
            } catch (RequirementException e) {
                RaidCraft.LOGGER.warning(e.getMessage() + " in " + de.raidcraft.util.ConfigUtil.getFileName(config));
            }
        }
    }
}