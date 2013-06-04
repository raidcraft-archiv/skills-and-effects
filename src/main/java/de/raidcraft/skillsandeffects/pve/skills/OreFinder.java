package de.raidcraft.skillsandeffects.pve.skills;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skillsandeffects.pve.effects.OreFinderEffect;
import de.raidcraft.util.ItemUtils;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Ore Finder",
        description = "Dein Gespür verrät dir ob Erze in der Nähe zu finden sind.",
        types = {EffectType.HELPFUL}
)
public class OreFinder extends AbstractLevelableSkill implements CommandTriggered {

    private HashSet<Integer> blockIds = new HashSet<>();
    private String findMessage;

    public OreFinder(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        findMessage = data.getString("find-message", "Es liegt ein Hauch von Metalldunst in der Luft...");
        for (String key : data.getStringList("blocks")) {
            Material item = ItemUtils.getItem(key);
            if (item == null) {
                RaidCraft.LOGGER.warning("Unknwon item defined " + key + " in config " + getName());
                continue;
            }
            blockIds.add(item.getId());
        }
    }

    public String getFindMessage() {

        return findMessage;
    }

    public HashSet<Integer> getBlockIds() {

        return blockIds;
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (getHolder().hasEffect(OreFinderEffect.class)) {
            getHolder().removeEffect(OreFinderEffect.class);
        } else {
            addEffect(getHolder(), OreFinderEffect.class);
        }
    }
}
