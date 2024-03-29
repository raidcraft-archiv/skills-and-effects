package de.raidcraft.skillsandeffects.pvp.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.ProjectileType;
import de.raidcraft.skills.api.combat.callback.RangedCallback;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.effects.Burn;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.util.LocationUtil;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Fireball",
        description = "Schießt einen Feuerball auf den Gegener",
        types = {EffectType.MAGICAL, EffectType.DAMAGING, EffectType.SILENCABLE, EffectType.HARMFUL},
        elements = {EffectElement.FIRE}
)
public class Fireball extends AbstractLevelableSkill implements CommandTriggered {

    public Fireball(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        rangedAttack(ProjectileType.FIREBALL, new RangedCallback() {
            @Override
            public void run(CharacterTemplate target) throws CombatException {

                if (LocationUtil.isSafeZone(getHolder().getPlayer(), target.getEntity().getLocation())) {
                    throw new CombatException(CombatException.Type.INVALID_TARGET);
                }
                Fireball.this.addEffect(target, Burn.class);
            }
        }).run();
    }
}
