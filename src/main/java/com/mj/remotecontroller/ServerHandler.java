package com.mj.remotecontroller;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.mj.remotecontroller.util.Util;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.awt.*;

public class ServerHandler extends SimpleChannelInboundHandler<String>{

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        //打印出客户端地址
        //System.out.println(ctx.channel().remoteAddress()+", "+msg);
        JSONObject message = JSONObject.parseObject(msg);
        switch (message.getString("type")) {
            case "versionAndCheckId":
                break;
            case "getIdAndPin":
                JSONObject jsonObject = Util.getDefaultJSONObject("idAndPin");
                jsonObject.put("id", ChannelSupervise.findHandleContainers(ctx.channel()).getConnectId());
                jsonObject.put("pin", ChannelSupervise.findHandleContainers(ctx.channel()).getConnectPin());
                ctx.channel().writeAndFlush(jsonObject.toString());
                break;
            case "checkIdAndPin":
                HandleContainer handleContainer = ChannelSupervise.findByConnectIdAndConnectPin(message.getString("id"), message.getString("pin"));
                jsonObject = Util.getDefaultJSONObject("checkIdAndPin");
                jsonObject.put("success", handleContainer!=null);
                if(handleContainer!=null) {
                    int id = ChannelSupervise.addConnectContainer(ctx.channel().id().asShortText(), handleContainer.getChannelShortId());
                    jsonObject.put("id", id);
                    ctx.channel().writeAndFlush(jsonObject.toString());
                    new Thread(() -> {
                        Channel channel = ChannelSupervise.findChannel(handleContainer.getChannelShortId());
                        if(channel!=null) {
                            JSONObject jsonObj = Util.getDefaultJSONObject("controlled");
                            jsonObj.put("id", id);
                            channel.writeAndFlush(jsonObj.toString());
                        }
                    }).start();
                } else {
                    ctx.channel().writeAndFlush(jsonObject.toString());
                }
                break;
            case "sendImage": {
                int id = message.getInteger("id");
                ConnectContainer connectContainer = ChannelSupervise.findConnectContainers(id);
                if (connectContainer != null) {
                    Channel channel = ChannelSupervise.findChannel(connectContainer.getControlChannelId());
                    if (channel != null) {
                        JSONObject jsonObj = Util.getDefaultJSONObject("sendImage");
                        jsonObj.put("image", message.getString("image"));
                        jsonObj.put("cursor", message.getString("cursor"));
                        channel.writeAndFlush(jsonObj.toString());
                    } else {
                        ChannelSupervise.removeConnectContainer(connectContainer);
                        ctx.channel().writeAndFlush(Util.getDefaultJSONObject("stopControl").toString());
                    }
                } else {
                    ctx.channel().writeAndFlush(Util.getDefaultJSONObject("stopControl").toString());
                }
            }
            break;
            case "mouse":
            case "mousemove":
            case "wheel":
            case "key": {
                int id = message.getInteger("id");
                ConnectContainer connectContainer = ChannelSupervise.findConnectContainers(id);
                if (connectContainer != null) {
                    Channel channel = ChannelSupervise.findChannel(connectContainer.getControlledChannelId());
                    if (channel != null) {
                        channel.writeAndFlush(message.toString());
                    } else {
                        ChannelSupervise.removeConnectContainer(connectContainer);
                        ctx.channel().writeAndFlush(Util.getDefaultJSONObject("stopControl").toString());
                    }
                } else {
                    ctx.channel().writeAndFlush(Util.getDefaultJSONObject("stopControl").toString());
                }
            }
            break;
            case "stopControl": {
                int id = message.getInteger("id");
                ConnectContainer connectContainer = ChannelSupervise.findConnectContainers(id);
                if (connectContainer != null) {
                    Channel channel = ChannelSupervise.findChannel(connectContainer.getControlledChannelId());
                    if (channel != null) {
                        channel.writeAndFlush(Util.getDefaultJSONObject("stopControl").toString());
                    }
                    ChannelSupervise.removeConnectContainer(connectContainer);
                }
            }
            break;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //添加连接
        ChannelSupervise.addChannel(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //断开连接
        ChannelSupervise.removeChannel(ctx.channel());
        ChannelSupervise.removeConnectContainersByControlledId(ctx.channel().id().asShortText());
        ChannelSupervise.removeConnectContainersByControlId(ctx.channel().id().asShortText());
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}