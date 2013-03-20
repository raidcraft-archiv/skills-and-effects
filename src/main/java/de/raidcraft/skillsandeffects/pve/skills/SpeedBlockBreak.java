package de.raidcraft.skillsandeffects.pve.skills;

import de.raidcraft.RaidCraft;
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
import de.raidcraft.skills.items.ToolType;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.PlayerInteractTrigger;
import de.raidcraft.skillsandeffects.pve.effects.tools.SpeedBlockBreakEffect;
import de.raidcraft.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.block.Action;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Speed Block Break",
        desc = "Baut Blöcke instant ab."
)
public class SpeedBlockBreak extends AbstractSkill implements Triggered {

    private ToolType toolType;
    private final Set<Integer> blockIds = new HashSet<>();

    public SpeedBlockBreak(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        toolType = ToolType.fromMaterial(ItemUtils.getItem(data.getString("tool-type")));
        for (String key : data.getStringList("blocks")) {
            Material item = ItemUtils.getItem(key);
            if (item != null) {
                blockIds.add(item.getId());
            } else {
                RaidCraft.LOGGER.warning("Unknown item in skill config of: " + getName() + ".yml");
            }
        }
    }

    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractTrigger trigger) throws CombatException {

        if (trigger.getEvent().getAction() != Action.RIGHT_CLICK_BLOCK || !isValid(trigger)) {
            return;
        }

        addEffect(getHero(), QueuedInteract.class).addCallback(new Callback<PlayerInteractTrigger>() {
            @Override
            public void run(PlayerInteractTrigger trigger) throws CombatException {

                if (trigger.getEvent().getAction() != Action.LEFT_CLICK_BLOCK || !isValid(trigger)) {
                    return;
                }

                addEffect(getHero(), SpeedBlockBreakEffect.class);
            }
        }, Action.LEFT_CLICK_BLOCK);
    }

    public boolean isValid(PlayerInteractTrigger trigger) {

        return blockIds.contains(trigger.getEvent().getClickedBlock().getTypeId())
                && !(toolType == null || !toolType.isOfType(getHero().getItemTypeInHand()));
    }
}
