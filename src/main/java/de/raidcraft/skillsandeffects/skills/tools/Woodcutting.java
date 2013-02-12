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
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.BlockBreakTrigger;
import de.raidcraft.skills.trigger.PlayerInteractTrigger;
import de.raidcraft.skillsandeffects.effects.tools.RecursiveBlockBreak;
import de.raidcraft.util.ItemUtils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Philip
 */
@SkillInformation(
        name = "Woodcutting",
        desc = "Macht das Holzfällen spannender und effektiver",
        types = {EffectType.UNBINDABLE}
)
public class Woodcutting extends AbstractLevelableSkill implements Triggered {

    private Map<Material, KnownBlock> knownBlocks = new HashMap<>();
    private boolean plusVariant;
    private int toolId;
    private double doubleDropChancePerLevel;
    private double maxDoubleDropChance;

    public Woodcutting(Hero hero, SkillProperties skillData, Profession profession, THeroSkill database) {

        super(hero, skillData, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        this.plusVariant = data.getBoolean("plus-variant", false);
        this.toolId = data.getInt("tool-id", 271);
        this.doubleDropChancePerLevel = data.getDouble("double-drop-chance-per-level", 0.1);
        this.maxDoubleDropChance = data.getDouble("max-double-drop-chance", 50);

        CustomConfig blockConfig = CustomConfig.getConfig(data.getString("custom-block-config", "axe-block-config.yml"));
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

            KnownBlock knownBlock = new KnownBlock(material, exp);
            knownBlocks.put(material, knownBlock);
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

        // add exp based on felled block
        KnownBlock knownBlock = knownBlocks.get(event.getBlock().getTypeId());
        if(knownBlock == null) return;
        int exp = knownBlock.getExp();
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

        // activate Treefeller
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            // check usage costs and cooldown
            checkUsage();
            if (getHero().hasEffect(RecursiveBlockBreak.class)
                    && getHero().getEffect(RecursiveBlockBreak.class).getSource().equals(this)) {
                getHero().debug("Treefeller already enabled!");
                return;
            }

            getHero().sendMessage(ChatColor.YELLOW + "Du hebst deine "
                    + ItemUtils.getFriendlyName(Material.getMaterial(toolId), ItemUtils.Language.GERMAN));
            addEffect(getHero(), QueuedInteract.class).addCallback(new Callback<PlayerInteractTrigger>() {
                @Override
                public void run(PlayerInteractTrigger trigger) throws CombatException {

                    addEffect(getHero(), RecursiveBlockBreak.class);
                }
            }, Action.LEFT_CLICK_BLOCK);
        }
    }

    public class KnownBlock {

        private Material material;
        private int exp;

        public KnownBlock(Material material, int exp) {
            this.material = material;
            this.exp = exp;
        }

        public Material getMaterial() {
            return material;
        }

        public int getExp() {
            return exp;
        }
    }
}
