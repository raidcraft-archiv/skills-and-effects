package de.raidcraft.skillsandeffects.pvp.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.RaidCraft;
import de.raidcraft.api.player.UnknownPlayerException;
import de.raidcraft.skills.SkillsPlugin;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.Effect;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.util.LocationUtil;
import de.raidcraft.util.MathUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Purge",
        description = "Entfernt einen positiven Effekt von einem Gegner oder einen negativen Effekt von einem Gruppenmitglied.",
        types = {EffectType.HELPFUL, EffectType.HARMFUL, EffectType.MAGICAL, EffectType.SILENCABLE}
)
public class Purge extends AbstractSkill implements CommandTriggered {

    private boolean selfCast = false;

    public Purge(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        selfCast = data.getBoolean("self-cast", false);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        CharacterTemplate target;
        if (selfCast || getHolder().getPlayer().isSneaking()) {
            target = getHolder();
        } else         if (args.argsLength() > 0) {
            try {
                Hero hero = RaidCraft.getComponent(SkillsPlugin.class).getCharacterManager().getHero(args.getString(0));
                if (!LocationUtil.isWithinRadius(getHolder().getPlayer().getLocation(), hero.getPlayer().getLocation(), getTotalRange())) {
                    throw new CombatException(CombatException.Type.OUT_OF_RANGE);
                }
                if (!hero.isFriendly(getHolder())) {
                    throw new CombatException(CombatException.Type.NO_GROUP);
                }
                target = hero;
            } catch (UnknownPlayerException e) {
                throw new CombatException(e.getMessage());
            }
        } else {
            target = getTarget();
        }

        List<Effect> effects;
        if (target.isFriendly(getHolder())) {
            effects = target.getEffects(EffectType.PURGEABLE, EffectType.DEBUFF);
        } else {
            effects = target.getEffects(EffectType.PURGEABLE, EffectType.BUFF);
        }
        if (!effects.isEmpty()) {
            Effect effect = effects.get(MathUtil.RANDOM.nextInt(effects.size()));
            target.removeEffect(effect);
        }
    }
}
