package de.raidcraft.skillsandeffects.skills.magical;

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
import de.raidcraft.skills.api.resource.Resource;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skillsandeffects.effects.debuff.WitherEffect;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Soul Leecher",
        desc = "Verschiesst Witherköpfe und sammelt Seelen von den Opfern",
        types = {EffectType.MAGICAL, EffectType.SILENCABLE, EffectType.DAMAGING, EffectType.HARMFUL, EffectType.DEBUFF},
        elements = {EffectElement.DARK}
)
public class Soulleecher extends AbstractSkill implements CommandTriggered {

    public Soulleecher(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        rangedAttack(ProjectileType.WITHER_SKULL, new RangedCallback() {
            @Override
            public void run(CharacterTemplate target) throws CombatException {

                addEffect(target, WitherEffect.class).setDeathCallback(new RangedCallback() {
                    @Override
                    public void run(CharacterTemplate target) throws CombatException {

                        Resource souls = getHero().getResource("souls");
                        souls.setCurrent(souls.getCurrent() + 1);
                    }
                });
            }
        }).run();
    }
}
