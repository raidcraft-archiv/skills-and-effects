package de.raidcraft.skillsandeffects.pvp.skills.physical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.Attack;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.effects.disabling.KnockBack;
import de.raidcraft.skills.tables.THeroSkill;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Whirlwind",
        description = "Fügt allen Gegenern im Umkreis physischen Schaden zu.",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING, EffectType.HARMFUL, EffectType.AREA}
)
public class Whirlwind extends AbstractSkill implements CommandTriggered {

    private int maxTargets = 4;
    private boolean knockBack = false;

    public Whirlwind(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        maxTargets = data.getInt("max-targets", 4);
        knockBack = data.getBoolean("knockback", false);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        int i = 0;
        for (CharacterTemplate target : getNearbyTargets()) {

            if (target.equals(getHero())) {
                continue;
            }
            if (!(i < maxTargets)) {
                break;
            }
            Attack<CharacterTemplate, CharacterTemplate> attack = attack(target);
            if (!attack.isCancelled() && knockBack) {
                addEffect(getHero().getPlayer().getLocation(), target, KnockBack.class);
            }
            i++;
        }
    }
}
