package de.raidcraft.skillsandeffects.skills.movement;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.util.EffectUtil;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.List;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Blink",
        desc = "Teleportiert dich nach vorne und entfernt alle Stun Effekte.",
        types = {EffectType.MOVEMENT, EffectType.HELPFUL}
)
public class Blink extends AbstractSkill implements CommandTriggered {

    public Blink(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        Location loc = getBlockTarget().add(0, 1, 0);
        Location oldLoc = getHero().getEntity().getLocation();
        List<Block> lineOfSight = getHero().getEntity().getLineOfSight(null, getTotalRange());

        getHero().removeEffectTypes(EffectType.DISABLEING);
        getHero().getEntity().teleport(loc);

        EffectUtil.playEffect(oldLoc, Effect.ENDER_SIGNAL, 1);
        EffectUtil.playEffect(loc, Effect.ENDER_SIGNAL, 2);
        for (Block block : lineOfSight) {
            EffectUtil.playEffect(block.getLocation(), Effect.SMOKE, 1);
        }
    }
}
