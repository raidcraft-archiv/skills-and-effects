package de.raidcraft.skillsandeffects.pvp.skills.healing;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.character.CharacterTemplate;
import de.raidcraft.skills.api.combat.EffectElement;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.action.HealAction;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.resource.Resource;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skillsandeffects.pvp.effects.debuff.LayhandsEffect;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Layhands",
        description = "Heilt dich oder das Ziel um 100%, kostet allerdings 100% deiner Resourcen.",
        types = {EffectType.MAGICAL, EffectType.HEALING, EffectType.HELPFUL},
        elements = {EffectElement.HOLY}
)
public class Layhands extends AbstractSkill implements CommandTriggered {

    private boolean selfHeal = false;

    public Layhands(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        selfHeal = data.getBoolean("sealf-heal", false);
    }


    @Override
    public void runCommand(CommandContext args) throws CombatException {

        CharacterTemplate target = getTarget(args, true, selfHeal);

        if (target.hasEffect(LayhandsEffect.class)) {
            throw new CombatException(CombatException.Type.IMMUNE);
        }
        new HealAction<>(this, target, target.getMaxHealth()).run();
        addEffect(this, target, LayhandsEffect.class);
        for (Resource resource : getHolder().getResources()) {
            resource.setCurrent(resource.getDefault());
        }
    }
}
