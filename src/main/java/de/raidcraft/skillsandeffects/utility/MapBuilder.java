package de.raidcraft.skillsandeffects.utility;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.api.commands.QueuedCommand;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;

/**
 * @author mdoering
 */
@SkillInformation(
        name = "Map Builder",
        description = "Allows the switching into Gamemode, prevents the dropping of items and clears the inventory.",
        types = EffectType.SYSTEM,
        triggerCombat = false
)
public class MapBuilder extends AbstractSkill implements CommandTriggered {

    public MapBuilder(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (hasEffect(MapBuilderEffect.class)) {
            removeEffect(MapBuilderEffect.class);
            info("Der Map Builder Effekt wurde entfernt!");
        } else {
            try {
                warn("Bist du dir sicher dass du den Map Builder Modus betreten willst? Dein Inventar wird dabei komplett gelöscht!");
                new QueuedCommand(getHolder().getPlayer(), this, "enterMapBuilder");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public void enterMapBuilder() throws CombatException {

        addEffect(MapBuilderEffect.class);
    }
}
