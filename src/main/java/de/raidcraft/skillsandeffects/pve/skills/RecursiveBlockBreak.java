//package de.raidcraft.skillsandeffects.pve.skills;
//
//import de.raidcraft.RaidCraft;
//import de.raidcraft.skills.api.combat.action.SkillAction;
//import de.raidcraft.skills.api.effect.common.QueuedInteract;
//import de.raidcraft.skills.api.exceptions.CombatException;
//import de.raidcraft.skills.api.hero.Hero;
//import de.raidcraft.skills.api.persistance.SkillProperties;
//import de.raidcraft.skills.api.profession.Profession;
//import de.raidcraft.skills.api.skill.AbstractSkill;
//import de.raidcraft.skills.api.skill.SkillInformation;
//import de.raidcraft.skills.api.trigger.TriggerHandler;
//import de.raidcraft.skills.api.trigger.TriggerPriority;
//import de.raidcraft.skills.api.trigger.Triggered;
//import de.raidcraft.skills.tables.THeroSkill;
//import de.raidcraft.skills.trigger.PlayerInteractTrigger;
//import de.raidcraft.skills.util.ConfigUtil;
//import de.raidcraft.skills.util.ItemUtil;
//import de.raidcraft.util.ItemUtils;
//import lombok.Getter;
//import org.bukkit.Material;
//import org.bukkit.World;
//import org.bukkit.block.Block;
//import org.bukkit.block.BlockFace;
//import org.bukkit.configuration.ConfigurationSection;
//import org.bukkit.entity.Player;
//import org.bukkit.event.block.Action;
//import org.bukkit.event.block.BlockBreakEvent;
//import org.bukkit.event.player.PlayerInteractEvent;
//import org.bukkit.inventory.ItemStack;
//
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Map;
//import java.util.Set;
//
///**
// * @author Silthus
// */
//@SkillInformation(
//        name = "Recursive Block Break",
//        description = "Breaks blocks recursivly.",
//        queuedAttack = true
//)
//public class RecursiveBlockBreak extends AbstractSkill implements Triggered {
//
//    private static final int LEAVE_RADIUS = 7;
//    private static final Map<Player, Block[]> TREES = new HashMap<>();
//
//    @Getter
//    private final Set<Material> allowedTools = new HashSet<>();
//    @Getter
//    private final Set<Material> allowedBlocks = new HashSet<>();
//    @Getter
//    private int maxAmount;
//    private boolean treeFellerReachedThreshold = false;
//
//    public RecursiveBlockBreak(Hero hero, SkillProperties data, Profession profession, THeroSkill database) {
//
//        super(hero, data, profession, database);
//    }
//
//    @Override
//    public void load(ConfigurationSection data) {
//
//        this.maxAmount = (int) ConfigUtil.getTotalValue(this, data.getConfigurationSection("max-amount"));
//        for (String entry : data.getStringList("allowed-blocks")) {
//            Material material = Material.matchMaterial(entry);
//            if (material != null) {
//                allowedBlocks.add(material);
//            } else {
//                RaidCraft.LOGGER.warning("Wrong material in skill config of " + getName() + ": " + entry);
//            }
//        }
//        for (String entry : data.getStringList("tools")) {
//            Material material = Material.matchMaterial(entry);
//            if (material != null) {
//                allowedTools.add(material);
//            } else {
//                RaidCraft.LOGGER.warning("Wrong material in skill config of " + getName() + ": " + entry);
//            }
//        }
//    }
//
//    @TriggerHandler(ignoreCancelled = true, priority = TriggerPriority.MONITOR)
//    public void onPlayerInteract(PlayerInteractTrigger trigger) throws CombatException {
//
//        final PlayerInteractEvent event = trigger.getEvent();
//        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || !isValidTool(event)) {
//            return;
//        }
//        checkUsage(new SkillAction(this));
//
//        addEffect(QueuedInteract.class).addCallback(callback -> {
//
//            if (callback.getEvent().getAction() != Action.LEFT_CLICK_BLOCK
//                    || !isValidTool(event)
//                    || !isValidBlock(callback.getEvent())) {
//                return;
//            }
//
//            substractUsageCost(new SkillAction(RecursiveBlockBreak.this));
//            int amount = chopTree(callback.getEvent().getClickedBlock(), getHolder().getPlayer());
//            info(amount + "/" + getMaxAmount() + " Blöcke sofort abgebaut.");
//        }, Action.LEFT_CLICK_BLOCK);
//    }
//
//    public boolean isValidTool(PlayerInteractEvent event) {
//
//        return event.getItem() != null
//                && allowedTools.contains(event.getItem().getType());
//    }
//
//    public boolean isValidBlock(PlayerInteractEvent event) {
//
//        return event.getClickedBlock() != null
//                && allowedBlocks.contains(event.getClickedBlock().getType());
//    }
//
//    public int chopTree(Block block, Player player) {
//
//        List<Block> blocks = new LinkedList<>();
//        return popLogs(getBlocksToChop(block, getHighestLog(block), blocks), player);
//    }
//
//    public List<Block> getBlocksToChop(Block block, Block highest, List<Block> blocks) {
//
//        while (block.getY() <= highest.getY()) {
//            if (!blocks.contains(block)) {
//                blocks.add(block);
//            }
//            getBranches(block, blocks, block.getRelative(BlockFace.NORTH));
//            getBranches(block, blocks, block.getRelative(BlockFace.NORTH_EAST));
//            getBranches(block, blocks, block.getRelative(BlockFace.EAST));
//            getBranches(block, blocks, block.getRelative(BlockFace.SOUTH_EAST));
//            getBranches(block, blocks, block.getRelative(BlockFace.SOUTH));
//            getBranches(block, blocks, block.getRelative(BlockFace.SOUTH_WEST));
//            getBranches(block, blocks, block.getRelative(BlockFace.WEST));
//            getBranches(block, blocks, block.getRelative(BlockFace.NORTH_WEST));
//            if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH))) {
//                getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH));
//            }
//            if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_EAST))) {
//                getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_EAST));
//            }
//            if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.EAST))) {
//                getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.EAST));
//            }
//            if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_EAST))) {
//                getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_EAST));
//            }
//            if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH))) {
//                getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH));
//            }
//            if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_WEST))) {
//                getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_WEST));
//            }
//            if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.WEST))) {
//                getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.WEST));
//            }
//            if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_WEST))) {
//                getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_WEST));
//            }
//            if ((block.getData() == 3) || (block.getData() == 7) || (block.getData() == 11) || (block.getData() == 15)) {
//                if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH, 2))) {
//                    getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH, 2));
//                }
//                if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_EAST, 2))) {
//                    getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_EAST, 2));
//                }
//                if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.EAST, 2))) {
//                    getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.EAST, 2));
//                }
//                if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_EAST, 2))) {
//                    getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_EAST, 2));
//                }
//                if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH, 2))) {
//                    getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH, 2));
//                }
//                if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_WEST, 2))) {
//                    getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_WEST, 2));
//                }
//                if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.WEST, 2))) {
//                    getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.WEST, 2));
//                }
//                if (!blocks.contains(block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_WEST, 2))) {
//                    getBranches(block, blocks, block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_WEST, 2));
//                }
//            }
//            if (((blocks.contains(block.getRelative(BlockFace.UP))) || (!ItemUtils.isLog(block.getRelative(BlockFace.UP).getType())))) {
//                break;
//            }
//            block = block.getRelative(BlockFace.UP);
//        }
//        return blocks;
//    }
//
//    public void getBranches(Block block, List<Block> blocks, Block other) {
//
//        if ((!blocks.contains(other)) && (!ItemUtils.isLog(other.getType()))) {
//            getBlocksToChop(other, getHighestLog(other), blocks);
//        }
//    }
//
//    public Block getHighestLog(Block block) {
//
//        boolean isLog = true;
//        while (isLog) {
//            if ((ItemUtils.isLog(block.getRelative(BlockFace.UP).getType()) || (ItemUtils.isLog(block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH).getType())) || (ItemUtils.isLog(block.getRelative(BlockFace.UP).getRelative(BlockFace.EAST).getType())) || (ItemUtils.isLog(block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH).getType())) || (block.getRelative(BlockFace.UP).getRelative(BlockFace.WEST).getType().equals(Material.LOG)) || (block.getRelative(BlockFace.UP).getRelative(BlockFace.WEST).getType().equals(Material.LOG_2)) || (block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_EAST).getType().equals(Material.LOG)) || (block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_EAST).getType().equals(Material.LOG_2)) || (block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_WEST).getType().equals(Material.LOG)) || (block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_WEST).getType().equals(Material.LOG_2)) || (block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_EAST).getType().equals(Material.LOG)) || (block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_EAST).getType().equals(Material.LOG_2)) || (block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_WEST).getType().equals(Material.LOG)) || (block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_WEST).getType().equals(Material.LOG_2))) {
//                if ((block.getRelative(BlockFace.UP).getType().equals(Material.LOG)) || (block.getRelative(BlockFace.UP).getType().equals(Material.LOG_2)))
//                    block = block.getRelative(BlockFace.UP);
//                else if ((block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH).getType().equals(Material.LOG)) || (block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH).getType().equals(Material.LOG_2)))
//                    block = block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH);
//                else if ((block.getRelative(BlockFace.UP).getRelative(BlockFace.EAST).getType().equals(Material.LOG)) || (block.getRelative(BlockFace.UP).getRelative(BlockFace.EAST).getType().equals(Material.LOG_2)))
//                    block = block.getRelative(BlockFace.UP).getRelative(BlockFace.EAST);
//                else if ((block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH).getType().equals(Material.LOG)) || (block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH).getType().equals(Material.LOG_2)))
//                    block = block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH);
//                else if ((block.getRelative(BlockFace.UP).getRelative(BlockFace.WEST).getType().equals(Material.LOG)) || (block.getRelative(BlockFace.UP).getRelative(BlockFace.WEST).getType().equals(Material.LOG_2)))
//                    block = block.getRelative(BlockFace.UP).getRelative(BlockFace.WEST);
//                else if ((block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_EAST).getType().equals(Material.LOG)) || (block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_EAST).getType().equals(Material.LOG_2)))
//                    block = block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_EAST);
//                else if ((block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_WEST).getType().equals(Material.LOG)) || (block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_WEST).getType().equals(Material.LOG_2)))
//                    block = block.getRelative(BlockFace.UP).getRelative(BlockFace.NORTH_WEST);
//                else if ((block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_EAST).getType().equals(Material.LOG)) || (block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_EAST).getType().equals(Material.LOG_2)))
//                    block = block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_EAST);
//                else if ((block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_WEST).getType().equals(Material.LOG)) || (block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_WEST).getType().equals(Material.LOG_2))) {
//                    block = block.getRelative(BlockFace.UP).getRelative(BlockFace.SOUTH_WEST);
//                }
//            } else {
//                isLog = false;
//            }
//        }
//        return block;
//    }
//
//    public int popLogs(List<Block> blocks, Player player) {
//
//        ItemStack item = new ItemStack(Material.AIR);
//        item.setAmount(1);
//        int maxAmount = getMaxAmount();
//        int amount = 0;
//        for (Block block : blocks) {
//            BlockBreakEvent event = new BlockBreakEvent(block, player);
//            RaidCraft.callEvent(event);
//            if (!event.isCancelled()) {
//                block.breakNaturally(player.getItemInHand());
//                // popLeaves(block1);
//                amount++;
//            }
//            if (amount >= maxAmount) {
//                return amount;
//            }
//        }
//        return 0;
//    }
//
//    public void popLeaves(Block block) {
//
//        for (int y = -LEAVE_RADIUS; y < LEAVE_RADIUS + 1; y++)
//            for (int x = -LEAVE_RADIUS; x < LEAVE_RADIUS + 1; x++)
//                for (int z = -LEAVE_RADIUS; z < LEAVE_RADIUS + 1; z++) {
//                    Block target = block.getRelative(x, y, z);
//                    if ((target.getType().equals(Material.LEAVES)) || (target.getType().equals(Material.LEAVES_2)))
//                        target.breakNaturally();
//                }
//    }
//
//    public void moveDownLogs(Block block, List<Block> blocks, World world, Player player) {
//
//        ItemStack item = new ItemStack(Material.AIR);
//        item.setAmount(1);
//
//        List downs = new LinkedList();
//        for (int counter = 0; counter < blocks.size(); counter++) {
//            block = (Block) blocks.get(counter);
//            Block down = block.getRelative(BlockFace.DOWN);
//            if ((down.getType() == Material.AIR) || (down.getType() == Material.LEAVES) || (down.getType() == Material.LEAVES_2)) {
//                down.setType(block.getType());
//                down.setData(block.getData());
//                block.setType(Material.AIR);
//                downs.add(down);
//            } else {
//                item.setType(block.getType());
//                item.setDurability((short) block.getData());
//                block.setType(Material.AIR);
//                world.dropItem(block.getLocation(), item);
//                player.getInventory().clear(player.getInventory().getHeldItemSlot());
//            }
//        }
//
//        for (int counter = 0; counter < downs.size(); counter++) {
//            block = (Block) downs.get(counter);
//            if (!isLoneLog(block))
//                continue;
//            downs.remove(block);
//            block.breakNaturally();
//            player.getInventory().clear(player.getInventory().getHeldItemSlot());
//        }
//
//        //moveLeavesDown(blocks);
//        if (TREES.containsKey(player)) {
//            TREES.remove(player);
//        }
//        if (downs.isEmpty()) {
//            return;
//        }
//        Block[] blockarray = new Block[downs.size()];
//        for (int counter = 0; counter < downs.size(); counter++) {
//            blockarray[counter] = ((Block) downs.get(counter));
//        }
//        TREES.put(player, blockarray);
//    }
//
//    public void moveLeavesDown(List<Block> blocks) {
//
//        List<Block> leaves = new LinkedList<>();
//        for (Block block : blocks) {
//            for (int y = -LEAVE_RADIUS; y < LEAVE_RADIUS + 1; y++) {
//                for (int x = -LEAVE_RADIUS; x < LEAVE_RADIUS + 1; x++) {
//                    for (int z = -LEAVE_RADIUS; z < LEAVE_RADIUS + 1; z++) {
//                        if (((block.getRelative(x, y, z).getType().equals(Material.LEAVES)) || (block.getRelative(x, y, z).getType().equals(Material.LEAVES_2))) && (!leaves.contains(block.getRelative(x, y, z)))) {
//                            leaves.add(block.getRelative(x, y, z));
//                        }
//                    }
//                }
//            }
//        }
//        for (Block block : leaves)
//            if (((block.getRelative(BlockFace.DOWN).getType().equals(Material.AIR)) || (block.getRelative(BlockFace.DOWN).getType().equals(Material.LEAVES)) || (block.getRelative(BlockFace.DOWN).getType().equals(Material.LEAVES_2))) && ((block.getRelative(BlockFace.DOWN, 2).getType().equals(Material.AIR)) || (block.getRelative(BlockFace.DOWN, 2).getType().equals(Material.LEAVES)) || (block.getRelative(BlockFace.DOWN, 2).getType().equals(Material.LEAVES_2)) || (block.getRelative(BlockFace.DOWN, 2).getType().equals(Material.LOG)) || (block.getRelative(BlockFace.DOWN, 2).getType().equals(Material.LOG_2))) && ((block.getRelative(BlockFace.DOWN, 3).getType().equals(Material.AIR)) || (block.getRelative(BlockFace.DOWN, 3).getType().equals(Material.LEAVES)) || (block.getRelative(BlockFace.DOWN, 3).getType().equals(Material.LEAVES_2)) || (block.getRelative(BlockFace.DOWN, 3).getType().equals(Material.LOG)) || (block.getRelative(BlockFace.DOWN, 3).getType().equals(Material.LOG_2)))) {
//                block.getRelative(BlockFace.DOWN).setTypeIdAndData(block.getTypeId(), block.getData(), true);
//                block.setType(Material.AIR);
//            } else {
//                block.breakNaturally();
//            }
//    }
//
//    public boolean breaksTool(Player player, ItemStack item) {
//
//        if ((item != null) &&
//                (isTool(item.getTypeId()))) {
//            short damage = item.getDurability();
//            if (isAxe(item.getTypeId()))
//                damage = (short) (damage + 1);
//            else {
//                damage = (short) (damage + 2);
//            }
//            if (damage >= item.getType().getMaxDurability()) {
//                return true;
//            }
//            item.setDurability(damage);
//        }
//        return false;
//    }
//
//    public boolean isTool(int ID) {
//
//        return (ID == 256) || (ID == 257) || (ID == 258) || (ID == 267) || (ID == 268) || (ID == 269) || (ID == 270) || (ID == 271) || (ID == 272) || (ID == 273) || (ID == 274) || (ID == 275) || (ID == 276) || (ID == 277) || (ID == 278) || (ID == 279) || (ID == 283) || (ID == 284) || (ID == 285) || (ID == 286);
//    }
//
//    public boolean isAxe(int ID) {
//
//        return (ID == 258) || (ID == 271) || (ID == 275) || (ID == 278) || (ID == 286);
//    }
//
//    public boolean isLoneLog(Block block) {
//
//        if ((block.getRelative(BlockFace.UP).getType() == Material.LOG) || (block.getRelative(BlockFace.UP).getType() == Material.LOG_2)) {
//            return false;
//        }
//        if (block.getRelative(BlockFace.DOWN).getType() != Material.AIR) {
//            return false;
//        }
//        if (hasHorizontalCompany(block)) {
//            return false;
//        }
//        if (hasHorizontalCompany(block.getRelative(BlockFace.UP))) {
//            return false;
//        }
//
//        return !hasHorizontalCompany(block.getRelative(BlockFace.DOWN));
//    }
//
//    public boolean hasHorizontalCompany(Block block) {
//
//        if ((block.getRelative(BlockFace.NORTH).getType() == Material.LOG) || (block.getRelative(BlockFace.NORTH).getType() == Material.LOG_2)) {
//            return true;
//        }
//        if ((block.getRelative(BlockFace.NORTH_EAST).getType() == Material.LOG) || (block.getRelative(BlockFace.NORTH_EAST).getType() == Material.LOG_2)) {
//            return true;
//        }
//        if ((block.getRelative(BlockFace.EAST).getType() == Material.LOG) || (block.getRelative(BlockFace.EAST).getType() == Material.LOG_2)) {
//            return true;
//        }
//        if ((block.getRelative(BlockFace.SOUTH_EAST).getType() == Material.LOG) || (block.getRelative(BlockFace.SOUTH_EAST).getType() == Material.LOG_2)) {
//            return true;
//        }
//        if ((block.getRelative(BlockFace.SOUTH).getType() == Material.LOG) || (block.getRelative(BlockFace.SOUTH).getType() == Material.LOG_2)) {
//            return true;
//        }
//        if ((block.getRelative(BlockFace.SOUTH_WEST).getType() == Material.LOG) || (block.getRelative(BlockFace.SOUTH_WEST).getType() == Material.LOG_2)) {
//            return true;
//        }
//        if ((block.getRelative(BlockFace.WEST).getType() == Material.LOG) || (block.getRelative(BlockFace.WEST).getType() == Material.LOG_2)) {
//            return true;
//        }
//
//        return (block.getRelative(BlockFace.NORTH_WEST).getType() == Material.LOG) || (block.getRelative(BlockFace.NORTH_WEST).getType() == Material.LOG_2);
//    }
//}
