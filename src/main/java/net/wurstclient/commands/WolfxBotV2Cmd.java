package net.wurstclient.commands;

import net.minecraft.util.math.Vec3d;
import net.wurstclient.command.CmdException;
import net.wurstclient.command.Command;
import net.wurstclient.events.UpdateListener;

public final class WolfxBotV2Cmd extends Command implements UpdateListener {
    private int time = 0;
    private boolean enabled;
    private int tick = 0;

    public WolfxBotV2Cmd() {
        super("wolfxbot2", "A bot designed for wolfx.jp server to automatically sell items to the shop",
                ".wolfxbot2",
                "Turn off: .wolfxbot2");
    }

    @Override
    public void call(String[] args) throws CmdException {

        // disable if enabled
        if(enabled) {
            disable();

            return;
        }

        // start
        enabled = true;
        EVENTS.add(UpdateListener.class, this);
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
            case 0,1 -> goToMachine();
            case 2 -> waitTicks(120 * 20);
            case 3,4 -> goToShop();
            case 5 -> clickShop("-6727", "68" ,"-6386");
            case 6 -> waitTicks(1);
            case 7 -> sellItem("/qs amount all");
            case 8 -> time = 0;
        }
    }

    private void goToMachine() throws CmdException {
        if(time == 0){
            tick = 0;
            WURST.getCmds().sayCmd.call(new String[]{"/res tp best.leather"});
            time++;
        } else if (time == 1){
            if (MC.player == null) {
                disable();
                return;
            }

            Vec3d pos = MC.player.getPos();

            if ((int) pos.x == -47050 && (int) pos.z == 21272) {
                time++;
                tick = 0;
            } else if(tick > 150) {
                time--;
                tick = 0;
            } else {
                tick++;
            }
        }

    }

    private void goToShop() throws CmdException {
        if(time == 3){
            tick = 0;
            WURST.getCmds().sayCmd.call(new String[]{"/home"});
            time++;
        } else if (time == 4){
            if (MC.player == null) {
                disable();
                return;
            }

            Vec3d pos = MC.player.getPos();

            // A bug in Residence that skips the teleportation wait
            if (tick == 15) {
                WURST.getCmds().sayCmd.call(new String[]{"/res tp best.leather"});
            }

            if ((int) pos.x == -6726 && (int) pos.z == -6385) {
                time++;
                tick = 0;
            } else if(tick > 150) {
                time--;
                tick = 0;
            } else {
                tick++;
            }
        }
    }

    private void clickShop(String... shopPos) throws CmdException {
        WURST.getCmds().clickCmd.call(shopPos);
        time++;
    }

    private void waitTicks(int ticks){
        if (WURST.getHax().timerHack.isEnabled()){
            ticks = (int) (tick * WURST.getHax().timerHack.getTimerSpeed());
        }

        if (tick < ticks) {
            tick++;
        } else {
            time++;
            tick = 0;
        }
    }

    private void sellItem(String... text) throws CmdException {
        WURST.getCmds().sayCmd.call(text);
        time++;
    }


    private void disable()
    {
        EVENTS.remove(UpdateListener.class, this);

        time = 0;
        tick = 0;
        enabled = false;

    }

    public boolean isActive()
    {
        return enabled;
    }
}
