package de.raidcraft.skillsandeffects.pve.skills;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.skills.api.combat.action.SkillAction;
import de.raidcraft.skills.api.combat.callback.Callback;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.TriggerHandler;
import de.raidcraft.skills.api.trigger.TriggerPriority;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.PlayerInteractTrigger;
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pve.effects.tools.SpeedBlockBreakEffect;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Recursive Block Break",
        description = "Breaks blocks recursivly.",
        queuedAttack = true
)
public class RecursiveBlockBreak extends AbstractSkill implements Triggered {

    private final Set<ItemStack> allowedTools = new HashSet<>();
    private final Set<Material> allowedBlocks = new HashSet<>();
    private int maxAmount;

    public RecursiveBlockBreak(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        this.maxAmount = (int) ConfigUtil.getTotalValue(this, data.getConfigurationSection("max-amount"));
        for (String entry : data.getStringList("allowed-blocks")) {
            Material material = Material.matchMaterial(entry);
            if (material != null) {
                allowedBlocks.add(material);
            } else {
                RaidCraft.LOGGER.warning("Wrong material in config of " + getName() + ": " + entry);
            }
        }
        for (String entry : data.getStringList("tools")) {
            try {
                ItemStack item = RaidCraft.getItem(entry);
                allowedTools.add(item);
            } catch (CustomItemException e) {
                warn(e);
            }
        }
    }

    public Set<Material> getAllowedBlocks() {

        return allowedBlocks;
    }

    public Set<ItemStack> getAllowedTools() {

        return allowedTools;
    }

    public int getMaxAmount() {

        return maxAmount;
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractTrigger trigger) throws CombatException {

        if (trigger.getEvent().getAction() != Action.RIGHT_CLICK_BLOCK || !isValid(trigger)) {
            return;
        }
        final SkillAction skillAction = new SkillAction(this);
        checkUsage(skillAction);

        queueInteract(new Callback<PlayerInteractTrigger>() {
            @Override
            public void run(PlayerInteractTrigger trigger) throws CombatException {

                if (trigger.getEvent().getAction() != Action.LEFT_CLICK_BLOCK || !isValid(trigger)) {
                    return;
                }

                addEffect(SpeedBlockBreakEffect.class);
                substractUsageCost(skillAction);
            }
        }, Action.LEFT_CLICK_BLOCK);
    }

    public boolean isValid(PlayerInteractTrigger trigger) {

        return allowedBlocks.contains(trigger.getEvent().getClickedBlock().getType())
                && trigger.getEvent().getItem() != null && isAllowedTool(trigger.getEvent().getItem());
    }

    public boolean isAllowedTool(ItemStack itemStack) {

        for (ItemStack tool : allowedTools) {
            if (tool.isSimilar(itemStack)) {
                return true;
            }
        }
        return false;
    }
}
