package de.raidcraft.skillsandeffects.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
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
import de.raidcraft.util.LocationUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Firewall",
        desc = "Erstellt eine Feuerwand vor dir.",
        types = {EffectType.MAGICAL, EffectType.AREA, EffectType.DAMAGING, EffectType.SILENCABLE},
        elements = {EffectElement.FIRE}
)
public class Firewall extends AbstractLevelableSkill implements CommandTriggered {

    private ConfigurationSection width;

    public Firewall(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        width = data.getConfigurationSection("width");
    }

    private int getWidth() {

        return (int) ConfigUtil.getTotalValue(this, width);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        Location target = getBlockTarget();
        Block sourceBlock = target.getBlock().getRelative(0, 1, 0);
        sourceBlock.setType(Material.FIRE);
        int width = getWidth();
        BlockFace face = LocationUtil.rotateBlockFace(getFacing());
        if (width > 0) {
            for (int i = 1; i <= width; i++) {
                sourceBlock.getRelative(face, i).setType(Material.FIRE);
                sourceBlock.getRelative(LocationUtil.flipBlockFace(face), i).setType(Material.FIRE);
            }
        }
    }
}
