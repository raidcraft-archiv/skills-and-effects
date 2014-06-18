package de.raidcraft.skillsandeffects.pvp.skills.physical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.combat.callback.Callback;
import de.raidcraft.skills.api.effect.common.QueuedAttack;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.api.trigger.Triggered;
import de.raidcraft.skills.effects.Poison;
import de.raidcraft.skills.effects.disabling.Silence;
import de.raidcraft.skills.effects.Bleed;
import de.raidcraft.skills.effects.Burn;
import de.raidcraft.skills.effects.disabling.Disarm;
import de.raidcraft.skills.effects.disabling.Interrupt;
import de.raidcraft.skills.effects.disabling.KnockBack;
import de.raidcraft.skills.effects.disabling.Stun;
import de.raidcraft.skills.effects.Slow;
import de.raidcraft.skills.effects.Weakness;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.trigger.AttackTrigger;
import de.raidcraft.skills.api.effect.common.SunderingArmor;
import org.bukkit.configuration.ConfigurationSection;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "strike",
        description = "Schlägt das Ziel mit voller Wucht und stößt es zurück.",
        types = {EffectType.PHYSICAL, EffectType.DAMAGING, EffectType.HARMFUL},
        queuedAttack = true
)
public class Strike extends AbstractSkill implements CommandTriggered, Triggered {

    private boolean knockBack = false;
    private boolean bleed = false;
    private boolean stun = false;
    private boolean sunderArmor = false;
    private boolean disarm = false;
    private boolean ignoreArmor = false;
    private boolean slow = false;
    private boolean weaken = false;
    private boolean burn = false;
    private boolean interrupt = false;
    private boolean silence = false;
    private boolean poison = false;

    public Strike(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        knockBack = data.getBoolean("knockback", false);
        bleed = data.getBoolean("bleed", false);
        stun = data.getBoolean("stun", false);
        sunderArmor = data.getBoolean("sunder-armor", false);
        disarm = data.getBoolean("disarm", false);
        ignoreArmor = data.getBoolean("ignore-armor", false);
        slow = data.getBoolean("slow", false);
        weaken = data.getBoolean("weaken", false);
        burn = data.getBoolean("burn", false);
        interrupt = data.getBoolean("interrupt", false);
        silence = data.getBoolean("silence", false);
        poison = data.getBoolean("poison", false);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        addEffect(QueuedAttack.class).addCallback(new Callback<AttackTrigger>() {
            @Override
            public void run(AttackTrigger trigger) throws CombatException {

                if (knockBack) Strike.this.addEffect(getHolder().getEntity().getLocation(), trigger.getAttack().getTarget(), KnockBack.class);
                if (bleed) Strike.this.addEffect(trigger.getAttack().getTarget(), Bleed.class);
                if (stun) Strike.this.addEffect(trigger.getAttack().getTarget(), Stun.class);
                if (sunderArmor) Strike.this.addEffect(trigger.getAttack().getTarget(), SunderingArmor.class);
                if (disarm) Strike.this.addEffect(trigger.getAttack().getTarget(), Disarm.class);
                if (ignoreArmor) trigger.getAttack().addAttackTypes(EffectType.IGNORE_ARMOR);
                if (slow) Strike.this.addEffect(trigger.getAttack().getTarget(), Slow.class);
                if (weaken) Strike.this.addEffect(trigger.getAttack().getTarget(), Weakness.class);
                if (burn) Strike.this.addEffect(trigger.getSource().getTarget(), Burn.class);
                if (silence || interrupt) Strike.this.addEffect(trigger.getSource().getTarget(), Interrupt.class);
                if (silence) Strike.this.addEffect(trigger.getSource().getTarget(), Silence.class);
                if (poison) Strike.this.addEffect(trigger.getSource().getTarget(), Poison.class);
            }
        });
    }
}
