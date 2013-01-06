package de.raidcraft.skillsandeffects.skills.tools;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.callback.Callback;
import de.raidcraft.skills.api.effect.common.QueuedInteract;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.config.CustomConfig;
import de.raidcraft.skills.skills.ConfigurableSkillLevel;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.BlockBreakTrigger;
import de.raidcraft.skills.trigger.PlayerInteractTrigger;
import de.raidcraft.skillsandeffects.effects.tools.SpeedBlockBreak;
import de.raidcraft.util.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Author: Philip
 * Date: 28.12.12 - 01:25
 * Description:
 */
@SkillInformation(
        name = "Pickaxe",
        desc = "Macht das Nutzen einer Spitzhacke spannender und effektiver",
        types = {EffectType.UNBINDABLE}
)
public class Pickaxe extends AbstractLevelableSkill implements Triggered {

    private Map<Integer, KnownBlock> knownBlocks = new HashMap<>();
    private boolean plusVariant;
    private int toolId;
    private double doubleDropChancePerLevel;
    private double maxDoubleDropChance;

    public Pickaxe(Hero hero, SkillProperties skillData, Profession profession, THeroSkill database) {

        super(hero, skillData, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        attachLevel(new ConfigurableSkillLevel(this, database, data));

        this.plusVariant = data.getBoolean("plus-variant", false);
        this.toolId = data.getInt("tool-id", 270);
        this.doubleDropChancePerLevel = data.getDouble("double-drop-chance-per-level", 0.1);
        this.maxDoubleDropChance = data.getDouble("max-double-drop-chance", 50);

        CustomConfig blockConfig = CustomConfig.getConfig(data.getString("custom-block-config", "pickaxe-block-config.yml"));
        ConfigurationSection blocks = blockConfig.getConfigurationSection("blocks");
        if (blocks == null) {
            RaidCraft.LOGGER.warning("Missing blocks config section in " + blockConfig.getName());
            return;
        }
        for(String key : blocks.getKeys(false)) {
            Material material = ItemUtils.getItem(key);
            if(material == null) {
                RaidCraft.LOGGER.warning("Unknown material '" + key + "' in " + getClass().getSimpleName());
                continue;
            }
            ConfigurationSection blockSettings = blocks.getConfigurationSection(key);
            int exp = blockSettings.getInt("exp");
            List<SpecialDrop> specialDrops = new ArrayList<>();
            ConfigurationSection drops = blockSettings.getConfigurationSection("drops");
            if(drops != null) {
                for(String dropKey : drops.getKeys(false)) {
                    ConfigurationSection dropSection = drops.getConfigurationSection(dropKey);
                    Material dropMaterial = ItemUtils.getItem(dropKey);
                    int chance = dropSection.getInt("chance", 0);
                    int level = dropSection.getInt("min-level", 0);
                    int damageValue = dropSection.getInt("data", 0);
                    int dropExp = dropSection.getInt("exp", 0);
                    SpecialDrop specialDrop = new SpecialDrop(level, chance, new ItemStack(dropMaterial, 1, (short)damageValue), dropExp);
                    specialDrops.add(specialDrop);
                }
            }

            KnownBlock knownBlock = new KnownBlock(material, exp, specialDrops);
            knownBlocks.put(material.getId(), knownBlock);
        }
    }

    /*
     * Level increase and Double Drop
     */
    @TriggerHandler(checkUsage = false)
    public void blockBreak(BlockBreakTrigger trigger) throws CombatException {

        BlockBreakEvent event = trigger.getEvent();

        // ignore player placed block
        if (RaidCraft.isPlayerPlacedBlock(event.getBlock())) {
            return;
        }

        // check if correct tool
        if (event.getPlayer().getItemInHand() == null
                || event.getPlayer().getItemInHand().getTypeId() != toolId) {
            getHero().debug("Incorrect tool: " + event.getPlayer().getItemInHand().getType().name() + " (required: " + toolId + ")");
            return;
        }

        boolean superBreakerActive = (getHero().hasEffect(SpeedBlockBreak.class)
                && getHero().getEffect(SpeedBlockBreak.class).getSource().equals(this));

        // add exp based on mined block
        KnownBlock knownBlock = knownBlocks.get(event.getBlock().getTypeId());
        if(knownBlock == null) return;
        int exp = knownBlock.getExp();
        if (superBreakerActive) {
            exp *= 2;
            getHero().debug("Super Breaker enabled -> double exp: " + exp);
        }
        getLevel().addExp(exp);

        if(plusVariant) {
            // calculate double drop
            double chance = getLevel().getLevel() * doubleDropChancePerLevel;
            if(chance > maxDoubleDropChance) {
                chance = maxDoubleDropChance;
            }
            double random = Math.random() * 100.;
            if (chance > random) {
                for (ItemStack itemStack : event.getBlock().getDrops(event.getPlayer().getItemInHand())) {
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), itemStack);
                }
            }

            // special drop
            for(SpecialDrop drop : knownBlock.getDrops()) {
                event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), drop.getItem());
                getLevel().addExp(drop.getExp());
            }
        }
    }

    /*
     * Super Breaker
     */
    @TriggerHandler(checkUsage = false)
    public void interact(PlayerInteractTrigger trigger) throws CombatException {

        PlayerInteractEvent event = trigger.getEvent();

        // check if correct tool
        if (event.getItem() == null
                || event.getItem().getTypeId() != toolId) {
            return;
        }

        if(!plusVariant) {
            return;
        }

        // activate Super Breaker
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            // check usage costs and cooldown
            checkUsage();
            if (getHero().hasEffect(SpeedBlockBreak.class)
                    && getHero().getEffect(SpeedBlockBreak.class).getSource().equals(this)) {
                getHero().debug("Super Breaker already enabled!");
                return;
            }

            getHero().sendMessage(ChatColor.YELLOW + "Du hebst deine "
                    + ItemUtils.getFriendlyName(Material.getMaterial(toolId), ItemUtils.Language.GERMAN));
            addEffect(getHero(), QueuedInteract.class).addCallback(new Callback<PlayerInteractTrigger>() {
                @Override
                public void run(PlayerInteractTrigger trigger) throws CombatException {

                    addEffect(getHero(), SpeedBlockBreak.class);
                }
            }, Action.LEFT_CLICK_BLOCK);
        }
    }

    public class KnownBlock {

        private Material material;
        private int exp;
        private List<SpecialDrop> drops;

        public KnownBlock(Material material, int exp, List<SpecialDrop> drops) {
            this.material = material;
            this.exp = exp;
            this.drops = drops;
        }

        public Material getMaterial() {
            return material;
        }

        public int getExp() {
            return exp;
        }

        public List<SpecialDrop> getDrops() {
            List<SpecialDrop> itemDrops = new ArrayList<>();
            for(SpecialDrop drop : drops) {
                if(getLevel().getLevel() < drop.getLevel()) continue;
                double random = Math.random() * 100.;
                if (drop.getChance() > random) {
                    itemDrops.add(drop);
                }
            }
            return itemDrops;
        }
    }

    public class SpecialDrop {

        private int level;
        private int chance;
        private ItemStack item;
        private int exp;

        public SpecialDrop(int level, int chance, ItemStack item, int exp) {
            this.level = level;
            this.chance = chance;
            this.item = item;
            this.exp = exp;
        }

        public int getLevel() {
            return level;
        }

        public int getChance() {
            return chance;
        }

        public ItemStack getItem() {
            return item;
        }

        public int getExp() {
            return exp;
        }
    }
}
