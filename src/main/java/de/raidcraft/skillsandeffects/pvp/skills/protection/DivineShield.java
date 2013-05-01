package de.raidcraft.skillsandeffects.pvp.skills.protection;

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
import de.raidcraft.skillsandeffects.pvp.effects.buffs.protection.DivineShieldEffect;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Divine Shield",
        description = "Verhindert allen Schaden an dir, jedoch kannst du nicht angreifen.",
        types = {EffectType.PROTECTION, EffectType.HELPFUL}
)
public class DivineShield extends AbstractSkill implements CommandTriggered {

    public DivineShield(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        addEffect(getHolder(), DivineShieldEffect.class);
    }
}
