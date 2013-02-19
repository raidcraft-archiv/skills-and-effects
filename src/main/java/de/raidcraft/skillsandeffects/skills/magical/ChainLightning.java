package de.raidcraft.skillsandeffects.skills.magical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.MagicalAttack;
import de.raidcraft.skills.api.combat.callback.RangedCallback;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.util.ConfigUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Chain Lightning",
        desc = "Lässt einen Blitz auf Nachbarn überspringen.",
        types = {EffectType.DAMAGING, EffectType.MAGICAL, EffectType.SILENCABLE},
        elements = {EffectElement.LIGHTNING}
)
public class ChainLightning extends AbstractSkill implements CommandTriggered {

    private Set<CharacterTemplate> hitList = new HashSet<>();
    private ConfigurationSection jumps;
    private ConfigurationSection reductionPerJump;

    public ChainLightning(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        jumps = data.getConfigurationSection("jumps");
        reductionPerJump = data.getConfigurationSection("reduction-per-jump");
    }

    private int getJumpCount() {

        return (int) ConfigUtil.getTotalValue(this, jumps);
    }

    private double getReductionPerJump() {

        return ConfigUtil.getTotalValue(this, reductionPerJump);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        int damage = getTotalDamage();
        CharacterTemplate target = getTarget();
        hitList.add(target);
        // damage the intial target
    }

    private void strikeLightning(CharacterTemplate target, int damage) {

        new MagicalAttack(getHero(), target, damage, new RangedCallback() {
            @Override
            public void run(CharacterTemplate trigger) throws CombatException {

                
            }
        });
    }
}
