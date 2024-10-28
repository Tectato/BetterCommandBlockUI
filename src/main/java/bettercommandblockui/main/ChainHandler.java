package bettercommandblockui.main;

import bettercommandblockui.main.ui.screen.BetterCommandBlockScreen;
import net.minecraft.block.BlockState;
import net.minecraft.block.CommandBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.CommandBlockBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.CommandBlockExecutor;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ChainHandler {
    private BetterCommandBlockScreen screen;
    private BlockState current, next;
    private BlockPos currentPos;
    private List<Pair<BlockState, Direction>> prior;
    private World world;

    public ChainHandler(BetterCommandBlockScreen screen, CommandBlockExecutor executor){
        this.screen = screen;
        assert MinecraftClient.getInstance().player != null;
        this.world = MinecraftClient.getInstance().player.getWorld();
        this.currentPos = BlockPos.ofFloored(executor.getPos());
        this.current = world.getBlockState(currentPos);
        prior = new LinkedList<>();
        scanChain();
    }

    public void scanChain(){
        Direction currentDir = current.get(CommandBlock.FACING);
        BlockState up = getCommandBlock(currentPos.add(new Vec3i(0,1,0)));
        BlockState down = getCommandBlock(currentPos.add(new Vec3i(0,-1,0)));
        BlockState north = getCommandBlock(currentPos.add(new Vec3i(0,0,-1)));
        BlockState south = getCommandBlock(currentPos.add(new Vec3i(0,0,1)));
        BlockState east = getCommandBlock(currentPos.add(new Vec3i(1,0,0)));
        BlockState west = getCommandBlock(currentPos.add(new Vec3i(-1,0,0)));

        switch(currentDir){
            case UP -> {
                if(up != null) next = up;
            }
            case DOWN -> {
                if(down != null) next = down;
            }
            case NORTH -> {
                if(north != null) next = north;
            }
            case SOUTH -> {
                if(south != null) next = south;
            }
            case EAST -> {
                if(east != null) next = east;
            }
            case WEST -> {
                if(west != null) next = west;
            }
        }

        if((up != null) && up.get(CommandBlock.FACING) == Direction.DOWN) prior.add(new Pair<>(up, Direction.UP));
        if((down != null) && down.get(CommandBlock.FACING) == Direction.UP) prior.add(new Pair<>(down, Direction.DOWN));
        if((north != null) && north.get(CommandBlock.FACING) == Direction.SOUTH) prior.add(new Pair<>(north, Direction.NORTH));
        if((south != null) && south.get(CommandBlock.FACING) == Direction.NORTH) prior.add(new Pair<>(south, Direction.SOUTH));
        if((east != null) && east.get(CommandBlock.FACING) == Direction.WEST) prior.add(new Pair<>(east, Direction.EAST));
        if((west != null) && west.get(CommandBlock.FACING) == Direction.EAST) prior.add(new Pair<>(west, Direction.WEST));

        if(!prior.isEmpty()){
            prior.sort(new Comparator<Pair<BlockState,Direction>>() {
                @Override
                public int compare(Pair<BlockState,Direction> o1, Pair<BlockState,Direction> o2) {
                    if(o1.getLeft().get(CommandBlock.FACING) == currentDir) return -1;
                    if(o2.getLeft().get(CommandBlock.FACING) == currentDir) return 1;
                    return 0;
                }
            });
        }

        /*System.out.println("Next: (" + currentDir + ") " + (next == null ? "none" : next));
        System.out.println("Prior:");
        for(Pair<BlockState,Direction> state : prior){
            System.out.println(state.getRight() + ": " + state.getLeft());
        }*/
    }

    public boolean isInChain(){
        return (next != null && next.getBlock().equals(Registries.BLOCK.get(Identifier.of("minecraft","chain_command_block"))))
                || (!prior.isEmpty() && current.getBlock().equals(Registries.BLOCK.get(Identifier.of("minecraft","chain_command_block"))));
    }

    public BlockState getNext(){
        return next;
    }

    public List<Pair<BlockState,Direction>> getPrior(){
        return prior;
    }

    private BlockState getCommandBlock(BlockPos pos){
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof CommandBlockBlockEntity) {
            return world.getBlockState(pos);
        }
        return null;
    }
}
