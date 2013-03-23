package de.raidcraft.skillsandeffects.pve.skills;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.util.BlockUtil;
import org.bukkit.Material;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Flash Freeze",
        description = "Friert die Erde rund um den Spieler in Eis ein."
)
public class FlashFreeze extends AbstractSkill implements CommandTriggered {

    public FlashFreeze(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        BlockUtil.replaceNonSolidSurfaceBlocks(getHero().getEntity().getLocation().getBlock(),
                Material.SNOW,
                getTotalRange(),
                getTotalRange(),
                1,
                false
        );
    }
}
