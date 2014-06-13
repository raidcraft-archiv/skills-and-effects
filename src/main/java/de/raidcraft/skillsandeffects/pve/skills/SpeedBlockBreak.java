package de.raidcraft.skillsandeffects.pve.skills;

import de.raidcraft.RaidCraft;
import de.raidcraft.api.items.CustomItemException;
import de.raidcraft.skills.api.combat.action.SkillAction;
import de.raidcraft.skills.api.combat.callback.Callback;
import de.raidcraft.skills.api.effect.common.QueuedInteract;
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
        name = "Speed Block Break",
        description = "Baut Blöcke instant ab.",
        queuedAttack = true
)
public class SpeedBlockBreak extends AbstractSkill implements Triggered {

    private final Set<ItemStack> allowedTools = new HashSet<>();
    private final Set<Material> allowedBlocks = new HashSet<>();

    public SpeedBlockBreak(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        for (String key : data.getStringList("blocks")) {
            Material material = Material.matchMaterial(key);
            if (material != null) {
                allowedBlocks.add(material);
            } else {
                RaidCraft.LOGGER.warning("Unknown material in skill config of: " + getName() + ".yml");
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

    public boolean isAllowedTool(ItemStack itemStack) {

        for (ItemStack tool : allowedTools) {
            if (tool.isSimilar(itemStack)) {
                return true;
            }
        }
        return false;
    }

    public Set<ItemStack> getAllowedTools() {

        return allowedTools;
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractTrigger trigger) throws CombatException {

        if (trigger.getEvent().getAction() != Action.RIGHT_CLICK_BLOCK || !isValid(trigger)) {
            return;
        }
        checkUsage(new SkillAction(this));

        addEffect(QueuedInteract.class).addCallback(new Callback<PlayerInteractTrigger>() {
            @Override
            public void run(PlayerInteractTrigger trigger) throws CombatException {

                if (trigger.getEvent().getAction() != Action.LEFT_CLICK_BLOCK || !isValid(trigger)) {
                    return;
                }

                addEffect(SpeedBlockBreakEffect.class);
                substractUsageCost(new SkillAction(SpeedBlockBreak.this));
            }
        }, Action.LEFT_CLICK_BLOCK);
    }

    public boolean isValid(PlayerInteractTrigger trigger) {

        return allowedBlocks.contains(trigger.getEvent().getClickedBlock().getType())
                && trigger.getEvent().getItem() != null && isAllowedTool(trigger.getEvent().getItem());
    }
}
