package de.raidcraft.skillsandeffects.skills.physical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
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
        name = "Whirlwind",
        desc = "Fügt allen Gegenern im Umkreis physischen Schaden zu.",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING, EffectType.HARMFUL, EffectType.AREA}
)
public class Whirlwind extends AbstractSkill implements CommandTriggered {

    private int maxTargets = 4;

    public Whirlwind(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        maxTargets = data.getInt("max-targets", 4);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        int i = 0;
        for (CharacterTemplate target : getNearbyTargets()) {

            if (!(i < maxTargets)) {
                break;
            }
            attack(target);
            i++;
        }
    }
}
