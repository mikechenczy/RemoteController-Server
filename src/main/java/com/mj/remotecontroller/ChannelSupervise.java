package com.mj.remotecontroller;

import com.mj.remotecontroller.dao.UserDao;
import com.mj.remotecontroller.util.Util;
import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ChannelSupervise {
    public static ChannelGroup globalGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    public static ConcurrentMap<String, ChannelId> channelMap =new ConcurrentHashMap();
    public static UserDao userDao = new UserDao();
    public static List<HandleContainer> handleContainers = new ArrayList<>();
    public static List<ConnectContainer> connectContainers = new ArrayList<>();
    public static void addChannel(Channel channel){
        globalGroup.add(channel);
        channelMap.put(channel.id().asShortText(),channel.id());
        HandleContainer handleContainer = new HandleContainer();
        handleContainer.setChannelShortId(channel.id().asShortText());
        handleContainer.setConnectId(String.valueOf(Util.getRandomConnectId()));
        handleContainer.setConnectPin(String.valueOf(Util.getRandomConnectPin()));
        handleContainers.add(handleContainer);
    }
    public static void removeChannel(Channel channel) {
        globalGroup.remove(channel);
        channelMap.remove(channel.id().asShortText());
    }
    public static Channel findChannel(String id) {
        ChannelId channelId = channelMap.get(id);
        if(channelId==null)
            return null;
        return globalGroup.find(channelId);
    }
    public static void sendAll(String s) {
        globalGroup.writeAndFlush(s);
    }
    public static Channel get(String channelShortId) {
        synchronized (globalGroup) {
            synchronized (channelMap) {
                if (channelMap.get(channelShortId) == null)
                    return null;
                return globalGroup.find(channelMap.get(channelShortId));
            }
        }
    }
    public static Channel get(ChannelId channelId) {
        return globalGroup.find(channelId);
    }

    public static HandleContainer findHandleContainers(Channel channel) {
        return findHandleContainers(channel.id().asShortText());
    }

    public static HandleContainer findHandleContainers(String channelShortId) {
        synchronized (handleContainers) {
            for(HandleContainer handleContainer : handleContainers) {
                if(handleContainer.getChannelShortId().equals(channelShortId)) {
                    return handleContainer;
                }
            }
            return null;
        }
    }

    public static HandleContainer findByConnectIdAndConnectPin(String id, String pin) {
        synchronized (handleContainers) {
            for(HandleContainer handleContainer : handleContainers) {
                if(handleContainer.getConnectId().equals(id) && handleContainer.getConnectPin().equals(pin)) {
                    return handleContainer;
                }
            }
            return null;
        }
    }

    public static ConnectContainer findConnectContainers(int id) {
        synchronized (connectContainers) {
            for(ConnectContainer connectContainers : connectContainers) {
                if(connectContainers.getId()==id) {
                    return connectContainers;
                }
            }
            return null;
        }

    }

    public static void removeConnectContainer(int id) {
        synchronized (connectContainers) {
            for(int i=0;i<connectContainers.size();i++) {
                if(connectContainers.get(i).getId()==id) {
                    connectContainers.remove(i);
                    return;
                }
            }
        }
    }

    public static void removeConnectContainer(ConnectContainer connectContainer) {
        connectContainers.remove(connectContainer);
    }

    public static int addConnectContainer(String controlId, String controlledId) {
        ConnectContainer connectContainer = new ConnectContainer();
        connectContainer.setId(Util.getRandomConnectionId());
        connectContainer.setControlChannelId(controlId);
        connectContainer.setControlledChannelId(controlledId);
        connectContainers.add(connectContainer);
        return connectContainer.getId();
    }

    public static void removeConnectContainersByControlledId(String controlledId) {
        synchronized (connectContainers) {
            for(int i=0;i<connectContainers.size();i++) {
                if(connectContainers.get(i).getControlledChannelId().equals(controlledId)) {
                    connectContainers.remove(i);
                    return;
                }
            }
        }
    }

    public static void removeConnectContainersByControlId(String controlId) {
        synchronized (connectContainers) {
            for(int i=0;i<connectContainers.size();i++) {
                if(connectContainers.get(i).getControlChannelId().equals(controlId)) {
                    connectContainers.remove(i);
                    return;
                }
            }
        }
    }
}