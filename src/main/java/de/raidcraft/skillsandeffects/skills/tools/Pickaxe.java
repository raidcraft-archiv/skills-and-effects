package de.raidcraft.skillsandeffects.skills.tools;

import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.BlockBreakTrigger;
import de.raidcraft.skills.trigger.PlayerInteractTrigger;
import de.raidcraft.skillsandeffects.effects.tools.SpeedBlockBreak;
import de.raidcraft.util.MetaDataKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * Author: Philip
 * Date: 28.12.12 - 01:25
 * Description:
 */
@SkillInformation(
        name = "Pickaxe",
        desc = "Macht das Nutzen einer Spitzhacke spannender und effektiver",
        types = {EffectType.UNBINDABLE},
        triggerCombat = false
)
public class Pickaxe extends AbstractLevelableSkill implements Triggered {

    private Map<String, Object> data;
    private int toolId;
    private double doubleDropChance;

    public Pickaxe(Hero hero, SkillProperties skillData, Profession profession, THeroSkill database) {

        super(hero, skillData, profession, database);
        attachLevel(new ToolLevel(this, database));
    }

    @Override
    public void load(ConfigurationSection data) {
        this.toolId = data.getInt("tool-id", 270);
        this.data = data.getValues(false);
        this.doubleDropChance = data.getDouble("double-drop-chance-per-level", 0.1);
    }

    /*
     * Level increase and Double Drop
     */
    @TriggerHandler(checkUsage = false)
    public void blockBreak(BlockBreakTrigger trigger) throws CombatException {

        BlockBreakEvent event = trigger.getEvent();

        // ignore player placed block
        if (RaidCraft.getMetaData(event.getBlock(), MetaDataKey.PLAYER_PLACED_BLOCK, false)) {
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
        try {
            int exp = (Integer) data.get(String.valueOf(trigger.getEvent().getBlock().getTypeId()));
            if (superBreakerActive) {
                exp *= 2;
                getHero().debug("Super Breaker enabled -> double exp: " + exp);
            }
            getLevel().addExp(exp);

            // calculate double drop
            double chance = getLevel().getLevel() * doubleDropChance;
            double random = Math.random() * 100.;
            if(chance > random) {
                for(ItemStack itemStack : event.getBlock().getDrops(event.getPlayer().getItemInHand())) {
                    event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), itemStack);
                }
            }

        } catch (Exception ignored) {
        }
    }

    /*
     * Super Breaker
     */
    @TriggerHandler(checkUsage = false)
    public void interact(PlayerInteractTrigger trigger) throws CombatException {
        PlayerInteractEvent event = trigger.getEvent();

        // check if correct tool
        if(event.getItem() == null
                || event.getItem().getTypeId() != toolId) {
            return;
        }

        // activate Super Breaker
        if(event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_AIR) {
            // check usage costs and cooldown
            checkUsage();
            if(getHero().hasEffect(SpeedBlockBreak.class)
                    && getHero().getEffect(SpeedBlockBreak.class).getSource().equals(this)) {
                getHero().debug("Super Breaker already enabled!");
                return;
            }

            getHero().debug("Start Super Breaker!");
            addEffect(getHero(), SpeedBlockBreak.class);
        }
    }

    @Override
    public void apply() {
        //TODO: implement
    }

    @Override
    public void remove() {
        //TODO: implement
    }
}
