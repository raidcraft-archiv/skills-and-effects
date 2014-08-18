package de.raidcraft.skillsandeffects.pve.skills;

import de.raidcraft.RaidCraft;
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
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pve.effects.tools.RecursiveBlockBreakEffect;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

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

    @Getter
    private final Set<Material> allowedTools = new HashSet<>();
    @Getter
    private final Set<Material> allowedBlocks = new HashSet<>();
    @Getter
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
                RaidCraft.LOGGER.warning("Wrong material in skill config of " + getName() + ": " + entry);
            }
        }
        for (String entry : data.getStringList("tools")) {
            Material material = Material.matchMaterial(entry);
            if (material != null) {
                allowedTools.add(material);
            } else {
                RaidCraft.LOGGER.warning("Wrong material in skill config of " + getName() + ": " + entry);
            }
        }
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractTrigger trigger) throws CombatException {

        final PlayerInteractEvent event = trigger.getEvent();
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !isValidTool(event)) {
            return;
        }
        checkUsage(new SkillAction(this));

        addEffect(QueuedInteract.class).addCallback(new Callback<PlayerInteractTrigger>() {
            @Override
            public void run(PlayerInteractTrigger trigger) throws CombatException {

                if (trigger.getEvent().getAction() != Action.LEFT_CLICK_BLOCK
                        || !isValidTool(event)) {
                    return;
                }

                addEffect(RecursiveBlockBreakEffect.class);
                substractUsageCost(new SkillAction(RecursiveBlockBreak.this));
            }
        }, Action.LEFT_CLICK_BLOCK);
    }

    public boolean isValidTool(PlayerInteractEvent event) {

        return event.getItem() != null
                && allowedTools.contains(event.getItem().getType());
    }

    public boolean isValidBlock(PlayerInteractEvent event) {

        return event.getClickedBlock() != null
                && allowedBlocks.contains(event.getClickedBlock().getType());
    }
}
