package de.raidcraft.skillsandeffects.pvp.skills.healing;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.HealAction;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.util.EffectUtil;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Area Heal",
        description = "Heilt dich und deine Gruppe.",
        types = {EffectType.MAGICAL, EffectType.SILENCABLE, EffectType.HEALING, EffectType.HELPFUL, EffectType.AREA}
)
public class AreaHeal extends AbstractSkill implements CommandTriggered {

    private static final FireworkEffect FIREWORK_EFFECT = FireworkEffect.builder()
            .with(FireworkEffect.Type.BALL_LARGE)
            .withColor(Color.YELLOW)
            .withFlicker()
            .withFade(Color.WHITE)
            .withTrail().build();

    public AreaHeal(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        EffectUtil.playFirework(getHero().getEntity().getWorld(), getHero().getEntity().getLocation().subtract(0, 4, 0), FIREWORK_EFFECT);
        for (CharacterTemplate target : getNearbyTargets()) {
            if (target.isFriendly(getHero())) {
                new HealAction<>(this, target, getTotalDamage()).run();
            }
        }
        // also heal ourselves
        new HealAction<>(this, getHero(), getTotalDamage()).run();
    }
}
