package de.raidcraft.skillsandeffects.utility;

import com.sk89q.minecraft.util.commands.CommandContext;
import de.raidcraft.api.commands.QueuedCommand;
import de.raidcraft.api.storage.InventoryStorage;
import de.raidcraft.api.storage.StorageException;
import de.raidcraft.skills.api.combat.EffectType;
import de.raidcraft.skills.api.exceptions.CombatException;
import de.raidcraft.skills.api.hero.Hero;
import de.raidcraft.skills.api.persistance.SkillProperties;
import de.raidcraft.skills.api.profession.Profession;
import de.raidcraft.skills.api.skill.AbstractSkill;
import de.raidcraft.skills.api.skill.SkillInformation;
import de.raidcraft.skills.api.trigger.CommandTriggered;
import de.raidcraft.skills.tables.THeroSkill;
import de.raidcraft.skills.tables.TSkillData;
import de.raidcraft.util.SerializationUtil;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    private static final String MAPBUILDER_DATA_KEY = "IS_MAPBUILDER";
    private static final String MAPBUILDER_LOCATION_DATA = "MAPBUILDER_LOC";
    private static final String MAPBUILDER_INVENTORY_STORAGE_ID = "MAPBUILDER_INV";
    private static final String MAPBUILDER_ARMOR_STORAGE_ID = "MAPBUILDER_ARMOR";
    private static final InventoryStorage INVENTORY_STORAGE = new InventoryStorage("mapbuilder-skill");

    private List<String> permissions = new ArrayList<>();

    public MapBuilder(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {

        super(hero, data, profession, database);
    }

    @Override
    public void load(ConfigurationSection data) {

        permissions = data.getStringList("permissions");
    }

    @Override
    public void runCommand(CommandContext args) throws CombatException {

        if (hasEffect(MapBuilderEffect.class)) {
            try {
                warn("Bist du dir sicher dass du den Map Builder Modus verlassen willst? " +
                        "Du wirst dabei an deine alte Position teleportiert.");
                new QueuedCommand(getHolder().getPlayer(), this, "removeMapBuilder");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        } else {
            try {
                warn("Bist du dir sicher dass du den Map Builder Modus betreten willst?");
                new QueuedCommand(getHolder().getPlayer(), this, "enterMapBuilder");
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> getPermissions() {

        return permissions;
    }

    private String getKey(String key) {

        return key + "_" + getHolder().getPlayer().getWorld().getName().toUpperCase();
    }

    public void enterMapBuilder() throws CombatException {

        if (hasEffect(MapBuilderEffect.class)) return;
        addEffect(MapBuilderEffect.class);

        Player player = getHolder().getPlayer();

        String locationByteStream = SerializationUtil.toByteStream(player.getLocation());
        setData(getKey(MAPBUILDER_LOCATION_DATA), locationByteStream);
        int storageId = INVENTORY_STORAGE.storeObject(player.getInventory().getContents());
        setData(getKey(MAPBUILDER_INVENTORY_STORAGE_ID), storageId + "");
        int armorStorageId = INVENTORY_STORAGE.storeObject(player.getInventory().getArmorContents());
        setData(getKey(MAPBUILDER_ARMOR_STORAGE_ID), armorStorageId + "");

        player.getInventory().clear();
        info("Map Builder Modus betreten.");
    }

    public void removeMapBuilder() throws CombatException {

        if (!hasEffect(MapBuilderEffect.class)) return;

        Player player = getHolder().getPlayer();
        removeEffect(MapBuilderEffect.class);

        player.getInventory().clear();

        Optional<TSkillData> location = removeData(getKey(MAPBUILDER_LOCATION_DATA));
        if (location.isPresent()) {
            player.teleport(((Location) SerializationUtil.fromByteStream(location.get().getDataValue())));
        }
        Optional<TSkillData> inventory = removeData(getKey(MAPBUILDER_INVENTORY_STORAGE_ID));
        if (inventory.isPresent()) {
            try {
                ItemStack[] itemStacks = INVENTORY_STORAGE.removeObject(Integer.parseInt(inventory.get().getDataValue()));
                player.getInventory().setContents(itemStacks);
            } catch (StorageException e) {
                e.printStackTrace();
            }
        }
        Optional<TSkillData> armor = removeData(getKey(MAPBUILDER_ARMOR_STORAGE_ID));
        if (armor.isPresent()) {
            try {
                ItemStack[] itemStacks = INVENTORY_STORAGE.removeObject(Integer.parseInt(armor.get().getDataValue()));
                player.getInventory().setArmorContents(itemStacks);
            } catch (StorageException e) {
                e.printStackTrace();
            }
        }

        removeData(getKey(MAPBUILDER_DATA_KEY));
        info("Der Map Builder Effekt wurde entfernt!");
    }

    @Override
    public void apply() {

        try {
            Optional<TSkillData> data = getData(getKey(MAPBUILDER_DATA_KEY));
            if (data.isPresent() && Boolean.parseBoolean(data.get().getDataValue())) {
                addEffect(MapBuilderEffect.class);
            }
        } catch (CombatException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void remove() {

        try {
            removeMapBuilder();
        } catch (CombatException e) {
            e.printStackTrace();
        }
    }
}
