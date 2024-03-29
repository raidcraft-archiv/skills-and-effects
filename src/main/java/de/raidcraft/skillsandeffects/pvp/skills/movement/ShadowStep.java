package de.raidcraft.skillsandeffects.pvp.skills.movement;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skillsandeffects.pvp.effects.buffs.generic.DamageBuff;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Shadow Step",
        description = "Nutzte die Schatten um dich rasend schnell hinter dein Ziel zu bewegen.",
        types = {EffectType.MOVEMENT, EffectType.HELPFUL}
)
public class ShadowStep extends AbstractLevelableSkill implements CommandTriggered {

    private boolean damageBonus = false;

    public ShadowStep(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        damageBonus = data.getBoolean("bonus-damage", false);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        shadowStep(getHolder(), getTarget());

        if (damageBonus) {
            addEffect(DamageBuff.class);
        }
    }

    public static void shadowStep(CharacterTemplate source, CharacterTemplate target) throws CombatException {

        // lets get the position behind the target by multiplying the vector
        Location origin = target.getEntity().getLocation();
        Vector behindTarget = origin.getDirection().multiply(2);
        Location teleportLocation = origin.subtract(behindTarget).add(0, 1, 0);
        origin.getWorld().playEffect(origin, Effect.ENDER_SIGNAL, 1);
        source.getEntity().teleport(teleportLocation);
        teleportLocation.getWorld().playEffect(teleportLocation, Effect.ENDER_SIGNAL, 1);
    }
}
