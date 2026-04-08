package nep.timeline.cirno.netlink;

import android.system.ErrnoException;
import android.system.Os;
import android.system.OsConstants;
import android.system.StructTimeval;

import java.io.Closeable;
import java.io.FileDescriptor;
import java.io.InterruptedIOException;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import lombok.Getter;
import nep.timeline.cirno.log.Log;

public class NetlinkClient implements Closeable {
    private static final int SOCKET_RECV_BUFSIZE = 64 * 1024;
    private static final int DEFAULT_RECV_BUFSIZE = 8 * 1024;
    @Getter
    final private FileDescriptor mDescriptor;
    private long mLastRecvTimeoutMs;
    private long mLastSendTimeoutMs;
    private final ClassLoader classLoader;

    public NetlinkClient(ClassLoader classLoader, int nlProto) throws ErrnoException {
        this.classLoader = classLoader;
        mDescriptor = Os.socket(
                OsConstants.AF_NETLINK, OsConstants.SOCK_DGRAM, nlProto);
        IoUtils.setsockoptInt(
                classLoader, mDescriptor, OsConstants.SOL_SOCKET,
                OsConstants.SO_RCVBUF, SOCKET_RECV_BUFSIZE);
    }

    public void bind(SocketAddress localAddr) throws ErrnoException, SocketException {
        Os.bind(mDescriptor, localAddr);
    }

    /**
     * Wait indefinitely (or until underlying socket error) for a
     * netlink message of at most DEFAULT_RECV_BUFSIZE size.
     */
    public ByteBuffer recvMessage()
            throws ErrnoException, InterruptedIOException {
        return recvMessage(DEFAULT_RECV_BUFSIZE, 0);
    }
    /**
     * Wait up to |timeoutMs| (or until underlying socket error) for a
     * netlink message of at most DEFAULT_RECV_BUFSIZE size.
     */
    public ByteBuffer recvMessage(long timeoutMs) throws ErrnoException, InterruptedIOException {
        return recvMessage(DEFAULT_RECV_BUFSIZE, timeoutMs);
    }
    private void checkTimeout(long timeoutMs) {
        if (timeoutMs < 0) {
            throw new IllegalArgumentException("Negative timeouts not permitted");
        }
    }
    /**
     * Wait up to |timeoutMs| (or until underlying socket error) for a
     * netlink message of at most |bufsize| size.
     *
     * Multi-threaded calls with different timeouts will cause unexpected results.
     */
    public ByteBuffer recvMessage(int bufsize, long timeoutMs)
            throws ErrnoException, IllegalArgumentException, InterruptedIOException {
        checkTimeout(timeoutMs);
        synchronized (mDescriptor) {
            if (mLastRecvTimeoutMs != timeoutMs) {
                Os.setsockoptTimeval(mDescriptor,
                        OsConstants.SOL_SOCKET, OsConstants.SO_RCVTIMEO,
                        StructTimeval.fromMillis(timeoutMs));
                mLastRecvTimeoutMs = timeoutMs;
            }
        }
        ByteBuffer byteBuffer = ByteBuffer.allocate(bufsize);
        int length = Os.read(mDescriptor, byteBuffer);
        if (length == bufsize) {
            Log.w("maximum read");
        }
        byteBuffer.position(0);
        byteBuffer.limit(length);
        byteBuffer.order(ByteOrder.nativeOrder());
        return byteBuffer;
    }

    /**
     * Send a message to a peer to which this socket has previously connected.
     *
     * This blocks until completion or an error occurs.
     */
    public boolean sendMessage(byte[] bytes, int offset, int count)
            throws ErrnoException, InterruptedIOException {
        return sendMessage(bytes, offset, count, 0);
    }
    /**
     * Send a message to a peer to which this socket has previously connected,
     * waiting at most |timeoutMs| milliseconds for the send to complete.
     *
     * Multi-threaded calls with different timeouts will cause unexpected results.
     */
    public boolean sendMessage(byte[] bytes, int offset, int count, long timeoutMs)
            throws ErrnoException, IllegalArgumentException, InterruptedIOException {
        checkTimeout(timeoutMs);
        synchronized (mDescriptor) {
            if (mLastSendTimeoutMs != timeoutMs) {
                Os.setsockoptTimeval(mDescriptor,
                        OsConstants.SOL_SOCKET, OsConstants.SO_SNDTIMEO,
                        StructTimeval.fromMillis(timeoutMs));
                mLastSendTimeoutMs = timeoutMs;
            }
        }
        return (count == Os.write(mDescriptor, bytes, offset, count));
    }

    @Override
    public void close() {
        IoUtils.closeQuietly(classLoader, mDescriptor);
    }
}