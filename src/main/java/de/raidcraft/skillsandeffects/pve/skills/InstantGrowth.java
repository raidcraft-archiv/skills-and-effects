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
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Instant Growth",
        description = "Lässt Pflanzen um den Benutzer sofort voll auswachsen."
)
public class InstantGrowth extends AbstractSkill implements CommandTriggered {

    public InstantGrowth(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        int radius = getTotalRange();
        Block sourceBlock = getHolder().getEntity().getLocation().getBlock();
        Block block;
        for (int x = 0; x < radius; x++) {
            for (int z = 0; z < radius; z++) {
                for (int y = 0; y < 2; y++) {
                    block = sourceBlock.getRelative(x, y, z);
                    if (block.getBlockData() instanceof Ageable) {
                        ((Ageable) block.getBlockData()).setAge(((Ageable) block.getBlockData()).getMaximumAge());
                    }
                }
            }
        }
    }
}
