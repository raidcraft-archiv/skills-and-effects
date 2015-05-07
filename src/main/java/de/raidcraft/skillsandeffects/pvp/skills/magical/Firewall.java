package de.raidcraft.skillsandeffects.pvp.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import de.raidcraft.RaidCraft;
import de.raidcraft.skills.api.combat.EffectElement;
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
import de.raidcraft.util.BlockUtil;
import de.raidcraft.util.LocationUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Firewall",
        description = "Erstellt eine Feuerwand vor dir.",
        types = {EffectType.MAGICAL, EffectType.AREA, EffectType.DAMAGING, EffectType.SILENCABLE},
        elements = {EffectElement.FIRE}
)
public class Firewall extends AbstractLevelableSkill implements CommandTriggered {

    private WorldGuardPlugin wg = null;

    private ConfigurationSection width;

    public Firewall(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
        wg = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
    }

    @Override
    public void load(ConfigurationSection data) {

        width = data.getConfigurationSection("width");
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        Block sourceBlock = getTargetBlock().getBlock();
        BlockFace face = LocationUtil.rotateBlockFace(getFacing());

        if (!RaidCraft.isPvPAllowed(getHolder().getPlayer(), sourceBlock.getLocation())) {
            throw new CombatException("PvP ist hier nicht erlaubt");
        }
        BlockUtil.replaceNonSolidSurfaceBlocks(sourceBlock, Material.FIRE, face, getWidth());
    }

    private int getWidth() {

        return (int) ConfigUtil.getTotalValue(this, width);
    }
}
