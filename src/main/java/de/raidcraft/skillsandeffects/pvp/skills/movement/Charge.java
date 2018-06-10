package de.raidcraft.skillsandeffects.pvp.skills.movement;

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
import de.raidcraft.skills.effects.Slow;
import de.raidcraft.skills.effects.disabling.Stun;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skillsandeffects.pvp.effects.movement.Charging;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.util.Vector;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Charge",
        description = "Stürmt das Ziel an und betäubt es.",
        types = {EffectType.PHYSICAL, EffectType.HARMFUL, EffectType.MOVEMENT}
)
public class Charge extends AbstractSkill implements CommandTriggered {

    private boolean stun = true;
    private boolean slow = false;

    public Charge(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        this.stun = data.getBoolean("stun", true);
        this.slow = data.getBoolean("slow", false);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        Location playerLoc = getHolder().getPlayer().getLocation();
        final CharacterTemplate target = getTarget();
        Location targetLoc = target.getEntity().getLocation();

        double xDir = targetLoc.getX() - playerLoc.getX();
        double zDir = targetLoc.getZ() - playerLoc.getZ();
        Vector v = new Vector(xDir / 3.0D, 0.5D, zDir / 3.0D);
        getHolder().getPlayer().setVelocity(v);
        addEffect(Charging.class);
        playerLoc.getWorld().playSound(playerLoc, Sound.ENTITY_WITHER_AMBIENT, 10F, 100F);
        // lets add a stun effect if configured
        if (stun) {
            Charge.this.addEffect(target, Stun.class);
        }
        if (slow) {
            Charge.this.addEffect(target, Slow.class);
        }
        if (getTotalDamage() > 0) {
            attack(target).run();
        }
    }
}
