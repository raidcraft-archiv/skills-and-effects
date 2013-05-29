package de.raidcraft.skillsandeffects.pvp.skills.buffs;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skillsandeffects.pvp.effects.armor.Shielded;
import de.raidcraft.skillsandeffects.pvp.effects.misc.WeakenSoul;
import de.raidcraft.util.EffectUtil;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Holy Shield",
        description = "Beschützt das Ziel durch einen heiligen Schild der Schaden absorbiert.",
        types = {EffectType.MAGICAL, EffectType.SILENCABLE, EffectType.HELPFUL, EffectType.ABSORBING},
        elements = {EffectElement.LIGHT}
)
public class HolyShield extends AbstractSkill implements CommandTriggered {

    private static final FireworkEffect FIREWORK_EFFECT = FireworkEffect.builder()
            .with(FireworkEffect.Type.BALL)
            .withColor(Color.YELLOW)
            .withFade(Color.WHITE)
            .withFlicker().build();

    private boolean selfCast = false;

    public HolyShield(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        selfCast = data.getBoolean("self-cast", false);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        CharacterTemplate target = getTarget(args, true, selfCast);
        if (target.hasEffect(WeakenSoul.class)) {
            throw new CombatException("Das Ziel ist von " + target.getEffect(WeakenSoul.class).getFriendlyName()
                    + " betroffen und kann kein Schild erhalten.");
        }
        addEffect(target, Shielded.class);
        addEffect(target, WeakenSoul.class);
        EffectUtil.playFirework(target.getEntity().getWorld(), target.getEntity().getLocation(), FIREWORK_EFFECT);
    }
}
