package de.raidcraft.skillsandeffects.pvp.skills.healing;

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
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Heal",
        description = "Heilt dein Ziel oder dich selbst",
        types = {EffectType.MAGICAL, EffectType.SILENCABLE, EffectType.HELPFUL, EffectType.HEALING},
        elements = {EffectElement.LIGHT}
)
public class Heal extends AbstractSkill implements CommandTriggered {

    private boolean damageMonster = false;
    private boolean damageHero = false;
    private boolean selfHeal = false;

    public Heal(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        damageMonster = data.getBoolean("damage-monster", false);
        damageHero = data.getBoolean("damage-hero", false);
        selfHeal = data.getBoolean("self-heal", false);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        CharacterTemplate target = getTarget(args, selfHeal);

        if (target.isFriendly(getHero())) {
            target.heal(getTotalDamage());
        } else if (target instanceof Hero) {
            if (damageHero) new MagicalAttack(getHero(), target, getTotalDamage()).run();
        } else if (damageMonster) {
            new MagicalAttack(getHero(), target, getTotalDamage()).run();
        }
    }
}
