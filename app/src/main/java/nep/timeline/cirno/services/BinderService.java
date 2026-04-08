package nep.timeline.cirno.services;

import android.system.ErrnoException;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import nep.timeline.cirno.GlobalVars;
import nep.timeline.cirno.entity.AppRecord;
import nep.timeline.cirno.log.Log;
import nep.timeline.cirno.netlink.NetlinkClient;
import nep.timeline.cirno.netlink.NetlinkSocketAddress;
import nep.timeline.cirno.threads.Handlers;
import nep.timeline.cirno.utils.StringUtils;

public class BinderService {
    private final static ExecutorService executorService = Executors.newSingleThreadExecutor();
    public static boolean received = false;
    private static boolean isRunning = false;
    private static final int NETLINK_UNIT_DEFAULT = 22;
    private static final int NETLINK_UNIT_MAX = 26;

    private static Map<String, String> parseParams(String message) {
        Map<String, String> map = new HashMap<>();
        for (String keyValue : message.split(",")) {
            String[] split = keyValue.split("=");
            if (split.length == 2)
                map.put(split[0].trim(), split[1].trim());
        }

        return map;
    }

    public static void start(ClassLoader classLoader) {
        if (isRunning)
            return;

        executorService.execute(() -> {
            try {
                int netlinkUnit;
                int configNetlinkUnit = GlobalVars.globalSettings.netlinkUnit;
                if (configNetlinkUnit >= NETLINK_UNIT_DEFAULT && configNetlinkUnit <= NETLINK_UNIT_MAX) {
                    netlinkUnit = configNetlinkUnit;
                } else {
                    File dir = new File("/proc/rekernel");
                    if (dir.exists()) {
                        File[] files = dir.listFiles();
                        if (files == null) {
                            Log.e("找不到ReKernel单元");
                            return;
                        }
                        File unitFile = files[0];
                        netlinkUnit = StringUtils.StringToInteger(unitFile.getName());
                    } else netlinkUnit = NETLINK_UNIT_DEFAULT;
                }

                try (NetlinkClient netlinkClient = new NetlinkClient(classLoader, netlinkUnit)) {
                    if (!netlinkClient.getMDescriptor().valid()) {
                        Log.e("无法连接至ReKernel服务器");
                        return;
                    }

                    netlinkClient.bind((SocketAddress) new NetlinkSocketAddress(100).toInstance());

                    isRunning = true;

                    Log.i("已连接至ReKernel, " + netlinkUnit + "#100");

                    while (true) {
                        try {
                            ByteBuffer byteBuffer = netlinkClient.recvMessage();
                            String data = new String(byteBuffer.array(), byteBuffer.position(), byteBuffer.limit(), StandardCharsets.UTF_8);
                            if (!data.isEmpty()) {
                                Map<String, String> params = parseParams(data.substring(data.indexOf("type"), data.lastIndexOf(";")));
                                if (params.containsKey("type") && !received) {
                                    Log.i("成功接收到来自ReKernel的消息");
                                    received = true;
                                }
                                Handlers.rekernel.post(() -> {
                                    String type = params.get("type");
                                    if (type.equals("Binder")) {
                                        String bindertype = params.get("bindertype");
                                        int oneway = StringUtils.StringToInteger(params.get("oneway"));
                                        int targetUid = StringUtils.StringToInteger(params.get("target"));
                                        if (oneway == 1 && !bindertype.equals("free_buffer_full"))
                                            return;

                                        List<AppRecord> appRecords = AppService.getByUid(targetUid);
                                        if (appRecords == null || appRecords.isEmpty())
                                            return;
                                        for (AppRecord appRecord : appRecords) {
                                            if (appRecord == null)
                                                continue;

                                            FreezerService.temporaryUnfreezeIfNeed(appRecord, "内核Binder(" + (oneway == 1 ? "ASYNC" : "SYNC") + "), 类型: " + bindertype, 3000);
                                        }
                                    }
                                });
                            }
                        } catch (ErrnoException | InterruptedIOException |
                                 NumberFormatException ignored) {

                        } catch (Exception e) {
                            Log.e("ReKernel", e);
                        }
                    }
                }
            } catch (ErrnoException | IOException e) {
                Log.e("无法连接至ReKernel服务器");
            } catch (Throwable throwable) {
                Log.e("ReKernel", throwable);
            }
        });
    }
}
