package de.raidcraft.skillsandeffects.pvp.skills.physical;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.api.items.EquipmentSlot;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.effect.Effect;
import de.raidcraft.skills.api.effect.common.QueuedAttack;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractLevelableSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.effects.disabling.KnockBack;
import de.raidcraft.skills.effects.disabling.Stun;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.util.CustomItemUtil;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * @author Silthus
 */
@SkillInformation(
        name = "Shield Bash",
        description = "Zieht deinem Ziel mit voller Wucht eins mit dem Schild drüber.",
        types = {EffectType.PHYSICAL, EffectType.HARMFUL, EffectType.DAMAGING},
        queuedAttack = true
)
public class ShieldBash extends AbstractLevelableSkill implements CommandTriggered {

    private boolean stun = false;
    private boolean knockback = false;
    private int purge = 0;

    public ShieldBash(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        stun = data.getBoolean("stun", false);
        knockback = data.getBoolean("knockback", false);
        purge = data.getInt("purge", 0);
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (!CustomItemUtil.isShield(getHolder().getArmor(EquipmentSlot.SHIELD_HAND)) && !CustomItemUtil.isShield(getHolder().getWeapon(EquipmentSlot.SHIELD_HAND))) {
            throw new CombatException("Du musst für diesen Skill einen Schild tragen.");
        }

        addEffect(QueuedAttack.class).addCallback(trigger -> {

            if (stun) ShieldBash.this.addEffect(trigger.getAttack().getTarget(), Stun.class);
            if (knockback) {
                ShieldBash.this.addEffect(
                        getHolder().getPlayer().getLocation(), trigger.getAttack().getTarget(), KnockBack.class);
            }
            if (purge > 0) {
                List<Effect> effects = trigger.getAttack().getTarget().getEffects(EffectType.PURGEABLE);
                int i = 0;
                for (Effect effect : effects) {
                    effect.remove();
                    i++;
                    if (i >= purge) {
                        break;
                    }
                }
            }
        });
    }
}
