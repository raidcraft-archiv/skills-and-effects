package de.raidcraft.skillsandeffects.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.MagicalAttack;
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
import org.bukkit.Sound;
import org.bukkit.block.Block;

import java.util.List;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Magic Bolt",
        desc = "Schiesst ein magisches Geschoss auf deinen Gegner.",
        types = {EffectType.DAMAGING, EffectType.MAGICAL, EffectType.SILENCABLE},
        elements = {EffectElement.DARK}
)
public class MagicBolt extends AbstractSkill implements CommandTriggered {

    public MagicBolt(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        CharacterTemplate target = getTarget();
        List<Block> lineOfSight = getHero().getPlayer().getLineOfSight(null, getTotalRange());
        EffectUtil.playSound(getHero().getPlayer().getLocation(), Sound.GHAST_FIREBALL, 5F, 1F);
        for (Block block : lineOfSight) {
            EffectUtil.playEffect(block.getLocation(), Effect.SMOKE, 1);
        }
        new MagicalAttack(getHero(), target, getTotalDamage()).run();
    }
}
