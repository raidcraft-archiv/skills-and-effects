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
import de.raidcraft.skills.util.ConfigUtil;
import de.raidcraft.skillsandeffects.pve.effects.OreFinderEffect;
import lombok.Getter;
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

    @Getter
    private HashSet<Material> matBlocks = new HashSet<>();
    @Getter
    private String findMessage;
    @Getter
    private int maxTime;

    public OreFinder(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        maxTime = (int) ConfigUtil.getTotalValue(this, data.getConfigurationSection("max-active-time"));
        if (maxTime > 3600) {
            maxTime = 3600;
        }
        findMessage = data.getString("find-message", "Es liegt ein Hauch von Metalldunst in der Luft...");
        for (String key : data.getStringList("blocks")) {
            Material material = Material.matchMaterial(key);
            if (material != null) {
                matBlocks.add(material);
            } else {
                RaidCraft.LOGGER.warning("Unknown material in skill config of: " + getName() + ".yml");
            }
        }
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (getHolder().hasEffect(OreFinderEffect.class)) {
            removeEffect(OreFinderEffect.class);
        } else {
            addEffect(OreFinderEffect.class);
        }
    }
}
