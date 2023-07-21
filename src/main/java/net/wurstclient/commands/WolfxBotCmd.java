package net.wurstclient.commands;

import net.minecraft.util.math.BlockPos;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.CmdSyntaxError;
import net.wurstclient.command.Command;
import net.wurstclient.events.UpdateListener;
import net.wurstclient.util.MathUtils;

public final class WolfxBotCmd extends Command implements UpdateListener {
    private BlockPos machinePos;
    private int time = 0;
    private boolean enabled;
    private int tick = 0;

    public WolfxBotCmd() {
        super("wolfxbot", "A bot designed for wolfx.jp server to automatically sell items to the shop",
                ".wolfxbot <x> <y> <z>",
                "Turn off: .wolfxbot");
    }

    @Override
    public void call(String[] args) throws CmdException {

        // disable if enabled
        if(enabled) {
            disable();

            WURST.getCmds().goToCmd.call(new String[0]);

            if(args.length == 0) {
                return;
            }
        }

        if (args.length == 0) {
            throw new CmdSyntaxError("Invalid coordinates.");
        }

        machinePos = argsToXyzPos(args);

        WURST.getHax().flightHack.setEnabled(true);

        // start
        enabled = true;
        EVENTS.add(UpdateListener.class, this);

        WURST.getCmds().sayCmd.call(new String[]{"搬砖机器人启动！当前机器坐标: " + (machinePos.getX() + 1) + ", " + machinePos.getY() + ", " + (machinePos.getZ() + 1)});
    }

    private BlockPos argsToXyzPos(String... xyz) throws CmdSyntaxError
    {
        BlockPos playerPos = BlockPos.ofFloored(MC.player.getPos());
        int[] player = {playerPos.getX(), playerPos.getY(), playerPos.getZ()};
        int[] pos = new int[3];

        for(int i = 0; i < 3; i++)
            if(MathUtils.isInteger(xyz[i]))
                pos[i] = Integer.parseInt(xyz[i]);
            else if(xyz[i].equals("~"))
                pos[i] = player[i];
            else if(xyz[i].startsWith("~")
                    && MathUtils.isInteger(xyz[i].substring(1)))
                pos[i] = player[i] + Integer.parseInt(xyz[i].substring(1));
            else
                throw new CmdSyntaxError("Invalid coordinates.");

        return new BlockPos(pos[0], pos[1], pos[2]);
    }

    @Override
    public void onUpdate() {
        try {
            check();
        } catch (CmdException e) {
            disable();
        }

    }

    private void check() throws CmdException {
        switch (time){
            case 0,1 -> goToMachine(machinePos.getX() + "", machinePos.getY() + "", machinePos.getZ() + "");
            case 2,3 -> goToShop("-6884","73","-6287");
            case 4 -> vclipInsideShop("-9");
            case 5 -> clickShop("-6885", "66" ,"-6287");
            case 6 -> waitTick();
            case 7 -> sellItem("/qs amount all");
            case 8 -> vclipOutSideShop("9");
        }
    }

    private void goToMachine(String... machinePos) throws CmdException {
        GoToCmd goToCmd = WURST.getCmds().goToCmd;
        if(time == 0){
            tick = 0;
            goToCmd.call(machinePos);
            time++;
        } else if (time == 1){
            if (!goToCmd.isActive() || tick > 200) time++;
            else {
                tick++;
            }
        }

    }

    private void goToShop(String... shopPos) throws CmdException {
        GoToCmd goToCmd = WURST.getCmds().goToCmd;
        if(time == 2){
            tick = 0;
            goToCmd.call(shopPos);
            time++;
        } else if (time == 3){
            if (!goToCmd.isActive() || tick > 200) time++;
            else {
                tick++;
            }
        }
    }

    private void vclipInsideShop(String... args) throws CmdException {
        WURST.getCmds().vClipCmd.call(args);
        time++;
    }

    private void clickShop(String... shopPos) throws CmdException {
        WURST.getCmds().clickCmd.call(shopPos);
        time++;
    }

    private void waitTick(){
        time++;
    }

    private void sellItem(String... text) throws CmdException {
        WURST.getCmds().sayCmd.call(text);
        time++;
    }

    private void vclipOutSideShop(String... args) throws CmdException {
        WURST.getCmds().vClipCmd.call(args);
        time = 0;
    }


    private void disable()
    {
        EVENTS.remove(UpdateListener.class, this);

        time = 0;
        tick = 0;
        machinePos = null;
        enabled = false;

        try {
            WURST.getCmds().sayCmd.call(new String[]{"搬砖机器人关闭！"});
        } catch (CmdException ignored) {
        }
    }

    public boolean isActive()
    {
        return enabled;
    }
}
