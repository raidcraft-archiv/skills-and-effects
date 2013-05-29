package de.raidcraft.skillsandeffects.pvp.skills.healing;

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
import de.raidcraft.skillsandeffects.pvp.effects.buffs.healing.HealOverTimeEffect;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Heal over Time",
        description = "Heilt das Ziel über eine bestimmte Zeit.",
        types = {EffectType.HEALING, EffectType.HELPFUL, EffectType.MAGICAL, EffectType.SILENCABLE},
        elements = {EffectElement.LIGHT}
)
public class HealOverTime extends AbstractSkill implements CommandTriggered {

    private boolean selfHeal = false;

    public HealOverTime(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        selfHeal = data.getBoolean("self-heal", false);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        CharacterTemplate target = getTarget(args, true, selfHeal);
        if (!target.isFriendly(getHolder())) {
            throw new CombatException("Ziel kann nicht geheilt werden da es nicht in deiner Gruppe ist.");
        }
        // lets add the actual heal effect
        addEffect(target, HealOverTimeEffect.class);
    }
}
